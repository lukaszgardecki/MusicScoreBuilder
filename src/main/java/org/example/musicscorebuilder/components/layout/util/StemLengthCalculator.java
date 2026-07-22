package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StemDirection;

public final class StemLengthCalculator {

    private StemLengthCalculator() {}

    public static double calculate(NoteLayout parentNote, double middleY, double spacing) {
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