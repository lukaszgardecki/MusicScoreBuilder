package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StemDirection;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public final class StemLengthCalculator {

    private StemLengthCalculator() {}

    public static double calculate(NoteLayout parentNote, double middleY, double spacing) {
        if (parentNote.getBeamGroup() != null) {
            return calculateBeamedFactor(parentNote, parentNote.getBeamGroup(), middleY, spacing);
        }

        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaff());

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
        return beamYAtNote;
    }

    private static double calculateBeamedFactor(NoteLayout parentNote, BeamGroupLayout beamGroup, double middleY, double spacing) {
        double beamYAtNoteX = calculateBeamYAtNote(parentNote, middleY, spacing);
        double noteHeadY = parentNote.getY();
        boolean isUp = isStemUp(parentNote);

        double rawPixelLength = calculateRawPixelLength(noteHeadY, beamYAtNoteX, isUp);
        double calculatedFactor = Math.abs(rawPixelLength) / spacing;

        double minRequiredFactor = calculateRequiredStemLength(parentNote, spacing) / spacing;

        return Math.max(calculatedFactor, minRequiredFactor);
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
        BeamGroupLayout beamGroup = first.getBeamGroup();
        double shiftNeeded = 0.0;

        // 1. Sprawdzamy WSZYSTKIE nuty w grupie i wyznaczamy największe potrzebne przesunięcie belki
        if (beamGroup != null && beamGroup.getNotes() != null) {
            double stemWidth = resolveStemWidth(first);

            for (NoteLayout note : beamGroup.getNotes()) {
                double requiredStem = calculateRequiredStemLength(note, spacing);
                double noteX = resolveNoteX(note, stemWidth);
                double currentBeamY = interpolateBeamY(x1, y1, x2, y2, noteX);

                if (firstIsUp) {
                    double targetY = note.getY() - requiredStem;
                    double shift = targetY - currentBeamY;
                    if (shift < shiftNeeded) {
                        shiftNeeded = shift; // Przesunięcie w górę (ujemne Y)
                    }
                } else {
                    double targetY = note.getY() + requiredStem;
                    double shift = targetY - currentBeamY;
                    if (shift > shiftNeeded) {
                        shiftNeeded = shift; // Przesunięcie w dół (dodatnie Y)
                    }
                }
            }
        }

        y1 += shiftNeeded;
        y2 += shiftNeeded;

        // 2. Korekta dla pojedynczego głosu przekraczającego środkową linię
        int activeVoices = first.getParent().getVoiceCountForStaff(first.getStaff());
        if (activeVoices == 1 && beamGroup != null && beamGroup.getNotes() != null) {
            double stemWidth = resolveStemWidth(first);
            for (NoteLayout note : beamGroup.getNotes()) {
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
        }

        return new BeamEndpoints(y1, y2);
    }

    /**
     * Oblicza minimalną wymaganą długość laseczki w pikselach dla konkretnej nuty,
     * uwzględniając ilość belek (np. 16-tka ma 2 belki, 32-jka ma 3 belki).
     */
    private static double calculateRequiredStemLength(NoteLayout note, double spacing) {
        ScoreStyle style = note.getScoreStyle();
        double minStemPixels = style.getNoteStemMinHeight() * spacing;

        int beamCount = note.getNote().getType().getBeamCount();
        if (beamCount > 1) {
            double beamThickness = style.getNoteBeamThickness() * spacing;
            double beamGap = style.getNoteBeamGap() * spacing;
            double beamStep = beamThickness + beamGap;

            // Każda dodatkowa belka wymaga zwiększenia odległości od główki o `beamStep`
            minStemPixels += (beamCount - 1) * beamStep;
        }

        return minStemPixels;
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