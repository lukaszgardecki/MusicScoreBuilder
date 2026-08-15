package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StemLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.List;

public final class StemLengthCalculator {

    private StemLengthCalculator() {}

    public static double calculate(NoteLayout parentNote, double middleY, double spacing) {
        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaff().getStaffIndex());
        BeamGroupLayout beamGroup = parentNote.getBeamGroup();

        if (activeVoices == 1) {
            if (beamGroup != null) {
                return calculateSingleVoiceBeamedFactor(parentNote, middleY, spacing);
            }
            return calculateSingleVoiceUnbeamedFactor(parentNote, middleY, spacing);
        } else {
            if (beamGroup != null) {
                return calculateMultiVoiceBeamedFactor(parentNote, spacing);
            }
            return calculateMultiVoiceUnbeamedFactor(parentNote, middleY, spacing);
        }
    }

    public static double calculateBeamYAtNote(NoteLayout parentNote, double middleY, double spacing) {
        if (parentNote.getBeamGroup() == null) return parentNote.getY();

        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaff().getStaffIndex());

        if (activeVoices == 1) {
            return calculateSingleVoiceBeamYAtNoteInternal(parentNote, middleY, spacing);
        } else {
            return calculateMultiVoiceBeamYAtNoteInternal(parentNote, spacing);
        }
    }

    public static double calculateEndY(NoteLayout parentNote, double middleY, double startY, double spacing) {
        BeamGroupLayout beamGroup = parentNote.getBeamGroup();

        if (beamGroup == null) {
            double diff = parentNote.getScoreStyle().getNoteStemHeightDiffFactor();
            double stemLengthFactor = calculate(parentNote, middleY, spacing);
            double standardStemHeight = (stemLengthFactor * spacing) - diff;
            boolean stemIsUp = isStemUp(parentNote);

            double stemHeight = standardStemHeight;
            double distanceToMiddle = Math.abs(middleY - startY);

            if (stemIsUp) {
                if (startY > middleY && distanceToMiddle > standardStemHeight) stemHeight = distanceToMiddle;
            } else {
                if (startY < middleY && distanceToMiddle > standardStemHeight) stemHeight = distanceToMiddle;
            }
            return stemIsUp ? startY - stemHeight : startY + stemHeight;
        }

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();

        if (first == null || last == null) {
            double stemLengthFactor = calculate(parentNote, middleY, spacing);
            double stemHeight = stemLengthFactor * spacing;
            boolean isUp = isStemUp(parentNote);
            return isUp ? startY - stemHeight : startY + stemHeight;
        }

        return calculateBeamYAtNote(parentNote, middleY, spacing);
    }

    private static double calculateSingleVoiceUnbeamedFactor(NoteLayout parentNote, double middleY, double spacing) {
        long stepsFromMiddle = Math.round(Math.abs(middleY - parentNote.getY()) * (2.0 / spacing));
        if (stepsFromMiddle == 0) return 3.0;
        if (stepsFromMiddle == 1) return 3.25;
        return 3.5;
    }

    private static double calculateSingleVoiceBeamedFactor(NoteLayout parentNote, double middleY, double spacing) {
        double beamYAtNote = calculateSingleVoiceBeamYAtNoteInternal(parentNote, middleY, spacing);
        return resolveFinalFactorFromBeamY(parentNote, beamYAtNote, spacing);
    }

    private static double calculateSingleVoiceBeamYAtNoteInternal(NoteLayout parentNote, double middleY, double spacing) {
        BeamGroupLayout beamGroup = parentNote.getBeamGroup();
        if (beamGroup == null) return parentNote.getY();

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();

        if (first == null || last == null) return parentNote.getY();

        double stemWidth = resolveStemWidth(first);
        double x1 = resolveNoteX(first, stemWidth);
        double x2 = resolveNoteX(last, stemWidth);

        double y1 = resolveSingleVoiceInitialBeamY(first);
        double y2 = resolveSingleVoiceInitialBeamY(last);

        int noteCount = beamGroup.size();
        double maxSlopeInSpaces = (noteCount <= 2) ? 0.35 : (noteCount == 3 ? 0.75 : 1.0);
        double maxPixelDelta = maxSlopeInSpaces * spacing;
        double currentDelta = y2 - y1;

        if (Math.abs(currentDelta) > maxPixelDelta) {
            double clampedDelta = Math.copySign(maxPixelDelta, currentDelta);
            double centerY = (y1 + y2) * 0.5;
            y1 = centerY - clampedDelta * 0.5;
            y2 = centerY + clampedDelta * 0.5;
        }

        boolean firstIsUp = isStemUp(first);
        double shiftNeeded = 0.0;
        List<NoteLayout> notes = beamGroup.getNotes();
        int numNotes = notes.size();

        for (int i = 0; i < numNotes; i++) {
            NoteLayout note = notes.get(i);
            double requiredStem = calculateRequiredStemLength(note, spacing);
            double noteX = resolveNoteX(note, stemWidth);
            double currentBeamY = interpolateBeamY(x1, y1, x2, y2, noteX);

            if (firstIsUp) {
                double targetY = note.getY() - requiredStem;
                double shift = targetY - currentBeamY;
                if (shift < shiftNeeded) shiftNeeded = shift;
            } else {
                double targetY = note.getY() + requiredStem;
                double shift = targetY - currentBeamY;
                if (shift > shiftNeeded) shiftNeeded = shift;
            }
        }
        y1 += shiftNeeded;
        y2 += shiftNeeded;

        for (int i = 0; i < numNotes; i++) {
            NoteLayout note = notes.get(i);
            boolean noteIsUp = isStemUp(note);
            double noteX = resolveNoteX(note, stemWidth);
            double currentBeamY = interpolateBeamY(x1, y1, x2, y2, noteX);

            if (noteIsUp && currentBeamY > middleY) {
                double diff = middleY - currentBeamY;
                y1 += diff;
                y2 += diff;
                break;
            } else if (!noteIsUp && currentBeamY < middleY) {
                double diff = middleY - currentBeamY;
                y1 += diff;
                y2 += diff;
                break;
            }
        }

        double noteX = resolveNoteX(parentNote, stemWidth);
        return interpolateBeamY(x1, y1, x2, y2, noteX);
    }

    private static double resolveSingleVoiceInitialBeamY(NoteLayout note) {
        boolean isUp = isStemUp(note);
        double stemLength = note.getScoreStyle().getNoteStemBeamedDefaultHeight();
        return note.getParent().getY() + note.getY() + (isUp ? -stemLength : stemLength);
    }

    private static double calculateMultiVoiceUnbeamedFactor(NoteLayout parentNote, double middleY, double spacing) {
        int stepsFromMiddle = calculateStepsFromMiddle(parentNote.getY(), middleY, spacing);
        StemLayout stem = parentNote.getStem();

        if (parentNote.getNote().getType().isEighth()) {
            boolean isUpper = (stem != null && stem.isUp()) && stepsFromMiddle >= 0;
            boolean isLower = (stem == null || !stem.isUp()) && stepsFromMiddle <= 0;
            return isUpper || isLower ? 3.25 : 3.5;
        }

        boolean stemIsUp = stem == null || stem.isUp();
        if (stemIsUp && stepsFromMiddle > 1) return 2.5;
        if (!stemIsUp && stepsFromMiddle < -1) return 2.5;
        if (stepsFromMiddle == 1) return 2.75;
        if (stepsFromMiddle == 0) return 3.0;
        if (stepsFromMiddle == -1) return 3.25;
        return 3.5;
    }

    private static double calculateMultiVoiceBeamedFactor(NoteLayout parentNote, double spacing) {
        double beamYAtNote = calculateMultiVoiceBeamYAtNoteInternal(parentNote, spacing);
        return resolveFinalFactorFromBeamY(parentNote, beamYAtNote, spacing);
    }

    private static double calculateMultiVoiceBeamYAtNoteInternal(NoteLayout parentNote, double spacing) {
        BeamGroupLayout beamGroup = parentNote.getBeamGroup();
        if (beamGroup == null) return parentNote.getY();

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();

        if (first == null || last == null) return parentNote.getY();

        double stemWidth = resolveStemWidth(first);
        double x1 = resolveNoteX(first, stemWidth);
        double x2 = resolveNoteX(last, stemWidth);

        double y1 = resolveMultiVoiceInitialBeamY(first);
        double y2 = resolveMultiVoiceInitialBeamY(last);

        int noteCount = beamGroup.size();
        double maxSlopeInSpaces = (noteCount <= 2) ? 0.35 : (noteCount == 3 ? 0.75 : 1.0);
        double maxPixelDelta = maxSlopeInSpaces * spacing;
        double currentDelta = y2 - y1;

        if (Math.abs(currentDelta) > maxPixelDelta) {
            double clampedDelta = Math.copySign(maxPixelDelta, currentDelta);
            double centerY = (y1 + y2) * 0.5;
            y1 = centerY - clampedDelta * 0.5;
            y2 = centerY + clampedDelta * 0.5;
        }

        boolean firstIsUp = isStemUp(first);
        double shiftNeeded = 0.0;
        List<NoteLayout> notes = beamGroup.getNotes();
        int numNotes = notes.size();

        for (int i = 0; i < numNotes; i++) {
            NoteLayout note = notes.get(i);
            double requiredStem = calculateRequiredStemLength(note, spacing);
            double noteX = resolveNoteX(note, stemWidth);
            double currentBeamY = interpolateBeamY(x1, y1, x2, y2, noteX);

            if (firstIsUp) {
                double targetY = note.getY() - requiredStem;
                double shift = targetY - currentBeamY;
                if (shift < shiftNeeded) shiftNeeded = shift;
            } else {
                double targetY = note.getY() + requiredStem;
                double shift = targetY - currentBeamY;
                if (shift > shiftNeeded) shiftNeeded = shift;
            }
        }
        y1 += shiftNeeded;
        y2 += shiftNeeded;

        double noteX = resolveNoteX(parentNote, stemWidth);
        return interpolateBeamY(x1, y1, x2, y2, noteX);
    }

    private static double resolveMultiVoiceInitialBeamY(NoteLayout note) {
        boolean isUp = isStemUp(note);
        double stemLength = note.getScoreStyle().getNoteStemMinHeight();
        return note.getParent().getY() + note.getY() + (isUp ? -stemLength : stemLength);
    }

    private static double resolveFinalFactorFromBeamY(NoteLayout parentNote, double beamYAtNoteX, double spacing) {
        double noteHeadY = parentNote.getY();
        boolean isUp = isStemUp(parentNote);
        double rawPixelLength = isUp ? noteHeadY - beamYAtNoteX : beamYAtNoteX - noteHeadY;

        double calculatedFactor = Math.abs(rawPixelLength) / spacing;
        double minRequiredFactor = calculateRequiredStemLength(parentNote, spacing) / spacing;

        return Math.max(calculatedFactor, minRequiredFactor);
    }

    private static double calculateRequiredStemLength(NoteLayout note, double spacing) {
        ScoreStyle style = note.getScoreStyle();
        double minStemPixels = style.getNoteStemMinHeight();

        int beamCount = note.getNote().getType().getBeamCount();
        if (beamCount > 1) {
            double beamThickness = style.getNoteBeamThickness();
            double beamGap = style.getNoteBeamGap();
            minStemPixels += (beamCount - 1) * (beamThickness + beamGap);
        }
        return minStemPixels;
    }

    private static double interpolateBeamY(double x1, double y1, double x2, double y2, double targetX) {
        double dx = x2 - x1;
        if (Math.abs(dx) < 0.0001) return y1;
        return y1 + ((y2 - y1) / dx) * (targetX - x1);
    }

    private static int calculateStepsFromMiddle(double noteY, double middleY, double spacing) {
        return (int) Math.round((middleY - noteY) * (2.0 / spacing));
    }

    private static double resolveStemWidth(NoteLayout note) {
        StemLayout stem = note.getStem();
        return stem != null ? stem.getWidth() : note.getScoreStyle().getNoteStemWidth();
    }

    private static boolean isStemUp(NoteLayout note) {
        StemLayout stem = note.getStem();
        return stem != null && stem.isUp();
    }

    private static double resolveNoteX(NoteLayout note, double stemWidth) {
        StemLayout stem = note.getStem();
        boolean isUp = stem != null && stem.isUp();
        return note.getParent().getX() + note.getX() + (isUp ? note.getBoxWidth() - stemWidth : 0.0);
    }
}