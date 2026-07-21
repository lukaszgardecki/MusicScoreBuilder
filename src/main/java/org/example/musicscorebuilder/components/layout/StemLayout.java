package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Clef;
import org.example.musicscorebuilder.components.music.ClefType;

public class StemLayout implements Selectable {
    private final ScoreStyle style;
    private final NoteLayout parentNote;
    private boolean selected;

    public StemLayout(NoteLayout parentNote) {
        this.parentNote = parentNote;
        this.style = parentNote.getScoreStyle();
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean contains(double segmentX, double segmentY) {
        double lineWidth = getWidth();
        double correctedX = getX() + parentNote.getX() + (lineWidth / 2.0);

        boolean hitX = Math.abs(segmentX - correctedX) <= (lineWidth / 2.0);
        boolean hitY = segmentY >= Math.min(getStartY(), getEndY()) && segmentY <= Math.max(getStartY(), getEndY());

        return hitX && hitY;
    }

    public double getX() {
        double localStemX = (getDirection() == StemDirection.UP) ? parentNote.getWidth() - getWidth() : 0;
        return parentNote.getX() + localStemX;
    }

    public double getStartY() {
        double diff = parentNote.getScoreStyle().getNoteStemHeightDiffFactor();
        double noteY = parentNote.getY();
        return getDirection() == StemDirection.UP ? noteY - diff : noteY + diff;
    }

    public double getEndY() {
        double spacing = style.getStaffLineSpacing();
        double diff = parentNote.getScoreStyle().getNoteStemHeightDiffFactor();
        double standardStemHeight = (3.5 * spacing) - diff;

        double startY = getStartY();
        double middleY = getMiddleLineY();
        StemDirection direction = getDirection();

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

    public double getWidth() {
        return parentNote.getScoreStyle().getNoteStemWidth();
    }

    public ScoreStyle getScoreStyle() { return style; }

    public StemDirection getDirection() {
        int voice = parentNote.getNote().getVoice();
        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaffLayout());

        if (activeVoices > 1) {
            return (voice % 2 == 1)
                    ? StemDirection.UP
                    : StemDirection.DOWN;
        }

        Clef clef = parentNote.getStaffLayout().getStaff().getDefaultClef();
        ClefType clefType = clef.getType();

        int noteStep = parentNote.getNote().getPitch().getAbsoluteDiatonicStep();
        int clefMiddleStep = clefType.getDiatonicStep() + 2;

        return (noteStep >= clefMiddleStep)
                ? StemDirection.DOWN
                : StemDirection.UP;
    }

    private double getMiddleLineY() {
        var style = parentNote.getScoreStyle();
        double spacing = style.getStaffLineSpacing();
        return parentNote.getStaffLayout().getY() + (2.0 * spacing);
    }
}