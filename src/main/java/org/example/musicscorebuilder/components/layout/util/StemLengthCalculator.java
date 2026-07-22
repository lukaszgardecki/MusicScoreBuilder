package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StemDirection;

public final class StemLengthCalculator {

    private StemLengthCalculator() {}

    public static double calculate(NoteLayout parentNote, double middleY, double spacing) {
        if (parentNote.getBeamGroup() != null) {
            return calculateBeamedFactor(parentNote, parentNote.getBeamGroup(), spacing);
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

    private static double calculateBeamedFactor(NoteLayout parentNote, BeamGroupLayout beamGroup, double spacing) {
        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();

        if (first == null || last == null) {
            return 3.25;
        }

        double firstStemLocalX = (first.getStem().getDirection() == StemDirection.UP) ? first.getBoxWidth() - first.getStem().getWidth() : 0;
        double x1 = first.getParent().getX() + first.getX() + firstStemLocalX;

        double lastStemLocalX = (last.getStem().getDirection() == StemDirection.UP) ? last.getBoxWidth() - last.getStem().getWidth() : 0;
        var stemWidth = first.getScoreStyle().getNoteStemWidth();
        double x2 = last.getParent().getX() + last.getX() + lastStemLocalX + stemWidth;

        double defaultStemLength = 3.25 * spacing;

        boolean firstIsUp = first.getStem().getDirection() == StemDirection.UP;
        double y1 = first.getParent().getY() + first.getY() + (firstIsUp ? -defaultStemLength : defaultStemLength);

        boolean lastIsUp = last.getStem().getDirection() == StemDirection.UP;
        double y2 = last.getParent().getY() + last.getY() + (lastIsUp ? -defaultStemLength : defaultStemLength);

        double noteStemLocalX = (parentNote.getStem().getDirection() == StemDirection.UP) ? parentNote.getBoxWidth() - parentNote.getStem().getWidth() : 0;
        double noteX = parentNote.getParent().getX() + parentNote.getX() + noteStemLocalX;

        double beamYAtNoteX;
        if (Math.abs(x2 - x1) < 0.0001) {
            beamYAtNoteX = y1;
        } else {
            double slope = (y2 - y1) / (x2 - x1);
            beamYAtNoteX = y1 + slope * (noteX - x1);
        }

        double noteHeadY = parentNote.getY();
        double rawPixelLength = Math.abs(beamYAtNoteX - noteHeadY);

        return rawPixelLength / spacing;
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
}