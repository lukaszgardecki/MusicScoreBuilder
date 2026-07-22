package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StemDirection;

public final class StemLengthCalculator {

    private StemLengthCalculator() {}

    public static double calculate(NoteLayout parentNote, double middleY, double spacing) {
        if (parentNote.getBeamGroup() != null) {
            return calculateBeamedFactor(parentNote, parentNote.getBeamGroup(), middleY, spacing);
        }

        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaffLayout());

        if (activeVoices == 1) {
            return calculateSingleVoiceFactor(parentNote, middleY, spacing);
        }

        int stepsFromMiddle = calculateStepsFromMiddle(parentNote.getY(), middleY, spacing);
        StemDirection direction = parentNote.getStem() != null ? parentNote.getStem().getDirection() : StemDirection.UP;

        if (parentNote.getNote().getType().isEighth()) {
            return calculateEighthFactor(direction, stepsFromMiddle);
        }

        return calculateMultiVoiceStandardFactor(direction, stepsFromMiddle);
    }

    public static double calculateBeamYAtNote(NoteLayout parentNote, double middleY, double spacing) {
        BeamGroupLayout beamGroup = parentNote.getBeamGroup();
        if (beamGroup == null) return parentNote.getY();

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();
        if (first == null || last == null) return parentNote.getY();

        double stemWidth = resolveStemWidth(first);
        double x1 = resolveNoteX(first, stemWidth);
        double y1 = resolveInitialBeamY(first);

        double x2 = resolveNoteX(last, stemWidth);
        double y2 = resolveInitialBeamY(last);

        BeamEndpoints clamped = clampBeamSlope(x1, y1, x2, y2, beamGroup.size(), spacing);
        y1 = clamped.y1();
        y2 = clamped.y2();

        BeamEndpoints adjusted = enforceConstraints(first, last, x1, y1, x2, y2, middleY, spacing);
        y1 = adjusted.y1();
        y2 = adjusted.y2();

        double noteX = resolveNoteX(parentNote, stemWidth);

        return interpolateBeamY(x1, y1, x2, y2, noteX);
    }

    public static double calculateEndY(NoteLayout parentNote, double middleY, double startY, double spacing) {
        BeamGroupLayout beamGroup = parentNote.getBeamGroup();
        if (beamGroup == null) {
            double diff = parentNote.getScoreStyle().getNoteStemHeightDiffFactor();
            double stemLengthFactor = calculate(parentNote, middleY, spacing);
            double standardStemHeight = (stemLengthFactor * spacing) - diff;
            StemDirection direction = parentNote.getStem() != null ? parentNote.getStem().getDirection() : StemDirection.UP;

            double stemHeight = standardStemHeight;
            double distanceToMiddle = Math.abs(middleY - startY);

            if (direction == StemDirection.UP) {
                if (startY > middleY && distanceToMiddle > standardStemHeight) {
                    stemHeight = distanceToMiddle;
                }
            } else {
                if (startY < middleY && distanceToMiddle > standardStemHeight) {
                    stemHeight = distanceToMiddle;
                }
            }

            return direction == StemDirection.UP
                    ? startY - stemHeight
                    : startY + stemHeight;
        }

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();
        boolean isUp = isStemUp(parentNote);

        if (first == null || last == null) {
            double stemLengthFactor = calculate(parentNote, middleY, spacing);
            double stemHeight = stemLengthFactor * spacing;
            return isUp ? startY - stemHeight : startY + stemHeight;
        }

        double beamYAtNote = calculateBeamYAtNote(parentNote, middleY, spacing);
        double noteY = parentNote.getY();
        boolean crossed = (isUp && beamYAtNote > noteY) || (!isUp && beamYAtNote < noteY);

        if (crossed) {
            return beamYAtNote;
        }

        return beamYAtNote;
    }

    private static double calculateBeamedFactor(NoteLayout parentNote, BeamGroupLayout beamGroup, double middleY, double spacing) {
        var noteStemMinHeight = parentNote.getScoreStyle().getNoteStemMinHeight();
        double beamYAtNoteX = calculateBeamYAtNote(parentNote, middleY, spacing);
        double noteHeadY = parentNote.getY();
        boolean isUp = isStemUp(parentNote);

        double rawPixelLength = calculateRawPixelLength(noteHeadY, beamYAtNoteX, isUp);
        double calculatedFactor = Math.abs(rawPixelLength) / spacing;

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();
        if (parentNote == first || parentNote == last) {
            return Math.max(calculatedFactor, noteStemMinHeight);
        }

        return calculatedFactor;
    }

    private static double resolveStemWidth(NoteLayout note) {
        return note.getStem() != null ? note.getStem().getWidth() : note.getScoreStyle().getNoteStemWidth();
    }

    private static boolean isStemUp(NoteLayout note) {
        return note.getStem() != null && note.getStem().getDirection() == StemDirection.UP;
    }

    private static double resolveNoteX(NoteLayout note, double stemWidth) {
        boolean isUp = isStemUp(note);
        return note.getParent().getX() + note.getX() + (isUp ? note.getBoxWidth() - stemWidth : 0);
    }

    private static double resolveInitialBeamY(NoteLayout note) {
        boolean isUp = isStemUp(note);
        var defaultStemLength = note.getScoreStyle().getNoteStemBeamedDefaultHeight();
        return note.getParent().getY() + note.getY() + (isUp ? -defaultStemLength : defaultStemLength);
    }

    private static BeamEndpoints clampBeamSlope(double x1, double y1, double x2, double y2, int noteCount, double spacing) {
        double maxSlopeInSpaces = (noteCount <= 2) ? 0.35 : (noteCount == 3 ? 0.75 : 1.0);
        double maxPixelDelta = maxSlopeInSpaces * spacing;

        double currentDelta = y2 - y1;
        if (Math.abs(currentDelta) > maxPixelDelta) {
            double clampedDelta = Math.copySign(maxPixelDelta, currentDelta);
            double centerY = (y1 + y2) / 2.0;
            y1 = centerY - clampedDelta / 2.0;
            y2 = centerY + clampedDelta / 2.0;
        }
        return new BeamEndpoints(y1, y2);
    }

    private static BeamEndpoints enforceConstraints(NoteLayout first, NoteLayout last, double x1, double y1, double x2, double y2, double middleY, double spacing) {
        boolean firstIsUp = isStemUp(first);
        var noteStemMinHeight = first.getScoreStyle().getNoteStemMinHeight();
        double minStemPixels = noteStemMinHeight * spacing;

        double targetY1 = firstIsUp ? first.getY() - minStemPixels : first.getY() + minStemPixels;
        double shift1 = firstIsUp ? Math.min(0, targetY1 - y1) : Math.max(0, targetY1 - y1);

        double targetY2 = lastIsUpHelper(last) ? last.getY() - minStemPixels : last.getY() + minStemPixels;
        double shift2 = lastIsUpHelper(last) ? Math.min(0, targetY2 - y2) : Math.max(0, targetY2 - y2);

        double totalShift = firstIsUp ? Math.min(shift1, shift2) : Math.max(shift1, shift2);
        y1 += totalShift;
        y2 += totalShift;

        int activeVoices = first.getParent().getVoiceCountForStaff(first.getStaffLayout());
        if (activeVoices == 1) {
            for (NoteLayout note : first.getBeamGroup().getNotes()) {
                boolean noteIsUp = isStemUp(note);
                double noteX = resolveNoteX(note, resolveStemWidth(note));
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
        }

        return new BeamEndpoints(y1, y2);
    }

    private static boolean lastIsUpHelper(NoteLayout last) {
        return isStemUp(last);
    }

    private static double interpolateBeamY(double x1, double y1, double x2, double y2, double targetX) {
        if (Math.abs(x2 - x1) < 0.0001) {
            return y1;
        }
        return y1 + ((y2 - y1) / (x2 - x1)) * (targetX - x1);
    }

    private static double calculateRawPixelLength(double noteHeadY, double beamYAtNoteX, boolean isUp) {
        return isUp ? noteHeadY - beamYAtNoteX : beamYAtNoteX - noteHeadY;
    }

    private static double calculateSingleVoiceFactor(NoteLayout parentNote, double middleY, double spacing) {
        long stepsFromMiddle = Math.round(Math.abs(middleY - parentNote.getY()) / (spacing / 2.0));
        if (stepsFromMiddle == 0) return 3.0;
        if (stepsFromMiddle == 1) return 3.25;
        return 3.5;
    }

    private static int calculateStepsFromMiddle(double noteY, double middleY, double spacing) {
        double pixelOffset = middleY - noteY;
        double stepSize = spacing / 2.0;
        return (int) Math.round(pixelOffset / stepSize);
    }

    private static double calculateEighthFactor(StemDirection direction, int stepsFromMiddle) {
        boolean isUpperDirectionCondition = (direction == StemDirection.UP && stepsFromMiddle >= 0);
        boolean isLowerDirectionCondition = (direction == StemDirection.DOWN && stepsFromMiddle <= 0);

        if (isUpperDirectionCondition || isLowerDirectionCondition) {
            return 3.25;
        }
        return 3.5;
    }

    private static double calculateMultiVoiceStandardFactor(StemDirection direction, int stepsFromMiddle) {
        if (direction == StemDirection.UP && stepsFromMiddle > 1) return 2.5;
        if (direction == StemDirection.DOWN && stepsFromMiddle < -1) return 2.5;

        if (stepsFromMiddle == 1) return 2.75;
        if (stepsFromMiddle == 0) return 3.0;
        if (stepsFromMiddle == -1) return 3.25;
        return 3.5;
    }

    private record BeamEndpoints(double y1, double y2) {}
}