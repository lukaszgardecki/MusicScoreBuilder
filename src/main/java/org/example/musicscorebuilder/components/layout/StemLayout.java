package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.layout.util.StemLengthCalculator;
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

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return parentNote.getVoice(); }
    @Override
    public boolean contains(double segmentX, double segmentY) {
        double lineWidth = getWidth();
        double correctedX = getX() + (lineWidth / 2.0);

        boolean hitX = Math.abs(segmentX - correctedX) <= (lineWidth / 2.0);
        boolean hitY = segmentY >= Math.min(getStartY(), getEndY()) && segmentY <= Math.max(getStartY(), getEndY());

        return hitX && hitY;
    }
    @Override public SegmentLayout getParentSegment() { return parentNote.getParentSegment(); }

    public double getX() {
        boolean isUp = (getDirection() == StemDirection.UP);
        double stemWidth = getWidth();
        double localStemX = isUp ? parentNote.getBoxWidth() - stemWidth : 0;

        if (parentNote.getBeamGroup() != null) {
            double spacing = style.getStaffLineSpacing();
            double beamYAtNote = StemLengthCalculator.calculateBeamYAtNote(parentNote, getMiddleLineY(), spacing);
            double noteCenterY = parentNote.getY() + (parentNote.getHeight() / 2.0);

            if (isUp && noteCenterY < beamYAtNote) {
                localStemX = 0;
            } else if (!isUp && noteCenterY > beamYAtNote) {
                localStemX = parentNote.getBoxWidth() - stemWidth;
            }
        }

        return parentNote.getX() + localStemX;
    }

    public double getStartY() {
        double diff = parentNote.getScoreStyle().getNoteStemHeightDiffFactor();
        double noteY = parentNote.getY();
        StemDirection direction = getDirection();
        boolean isUp = (direction == StemDirection.UP);

        if (parentNote.getBeamGroup() != null) {
            double spacing = style.getStaffLineSpacing();
            double beamYAtNote = StemLengthCalculator.calculateBeamYAtNote(parentNote, getMiddleLineY(), spacing);
            double noteCenterY = noteY + (parentNote.getHeight() / 2.0);

            boolean crossed = (isUp && beamYAtNote > noteCenterY) || (!isUp && beamYAtNote < noteCenterY);
            if (crossed) {
                return isUp ? noteY + diff : noteY - diff;
            }
        }

        return isUp ? noteY - diff : noteY + diff;
    }

    public double getEndY() {
        double spacing = style.getStaffLineSpacing();
        double middleY = getMiddleLineY();
        double startY = getStartY();

        return StemLengthCalculator.calculateEndY(parentNote, middleY, startY, spacing);
    }

    public double getWidth() {
        return parentNote.getScoreStyle().getNoteStemWidth();
    }

    public ScoreStyle getScoreStyle() { return style; }

    public StemDirection getDirection() {
        int voice = parentNote.getNote().getVoice();
        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaffLayout());

        if (activeVoices > 1) {
            return (voice % 2 == 1) ? StemDirection.UP : StemDirection.DOWN;
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