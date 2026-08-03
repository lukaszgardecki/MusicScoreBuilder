package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.layout.util.StemLengthCalculator;
import org.example.musicscorebuilder.components.music.Clef;
import org.example.musicscorebuilder.components.music.ClefType;

public class StemLayout implements Selectable {
    private final ScoreStyle style;
    private final NoteLayout parentNote;
    private boolean selected;
    private enum StemDirection { UP, DOWN }

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
    @Override public SegmentLayout getSegment() { return parentNote.getSegment(); }
    @Override public StaffLayout getStaff() { return parentNote.getStaff(); }

    public double getX() {
        boolean isUp = isUp();
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
        boolean isUp = isUp();

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
    public NoteLayout getParent()  { return parentNote; }
    public boolean isUp() { return getDirection() == StemDirection.UP; }
    public boolean isDown() {  return getDirection() == StemDirection.DOWN; }
    public StemDirection getDirection() {
        int voice = parentNote.getNote().getVoice();
        int activeVoices = parentNote.getParent().getVoiceCountForStaff(parentNote.getStaff());

        // 1. Wielogłosowość: nieparzyste głosy w górę (UP), parzyste w dół (DOWN)
        if (activeVoices > 1) {
            return (voice % 2 == 1) ? StemDirection.UP : StemDirection.DOWN;
        }

        // 2. Jeśli nuta leży w grupie belkowej – spójny kierunek dla całej grupy
        if (parentNote.getBeamGroup() != null && !parentNote.getBeamGroup().isEmpty()) {
            return calculateBeamGroupDirection(parentNote.getBeamGroup());
        }

        // 3. Pojedyncza nuta (bez belki)
        return calculateSingleNoteDirection(parentNote);
    }

    private StemDirection calculateSingleNoteDirection(NoteLayout note) {
        Clef clef = note.getStaff().getStaff().getDefaultClef();
        ClefType clefType = clef.getType();

        int noteStep = note.getNote().getPitch().getAbsoluteDiatonicStep();
        int clefMiddleStep = clefType.getDiatonicStep() + 2;

        return (noteStep >= clefMiddleStep) ? StemDirection.DOWN : StemDirection.UP;
    }

    private StemDirection calculateBeamGroupDirection(BeamGroupLayout beamGroup) {
        Clef clef = parentNote.getStaff().getStaff().getDefaultClef();
        ClefType clefType = clef.getType();
        int clefMiddleStep = clefType.getDiatonicStep() + 2;

        int maxDistAbove = 0;
        int maxDistBelow = 0;
        int totalStepOffset = 0;

        for (NoteLayout note : beamGroup.getNotes()) {
            int noteStep = note.getNote().getPitch().getAbsoluteDiatonicStep();
            int offset = noteStep - clefMiddleStep;

            totalStepOffset += offset;

            if (offset > 0) {
                maxDistAbove = Math.max(maxDistAbove, offset);
            } else if (offset < 0) {
                maxDistBelow = Math.max(maxDistBelow, Math.abs(offset));
            }
        }

        // Zasada nuty najbardziej oddalonej od środka pięciolinii
        if (maxDistAbove > maxDistBelow) {
            return StemDirection.DOWN;
        } else if (maxDistBelow > maxDistAbove) {
            return StemDirection.UP;
        }

        // W przypadku remisu decyduje wypadkowa suma pozycji (średnia wysokość)
        return (totalStepOffset >= 0) ? StemDirection.DOWN : StemDirection.UP;
    }

    private double getMiddleLineY() {
        var style = parentNote.getScoreStyle();
        double spacing = style.getStaffLineSpacing();
        return parentNote.getStaff().getY() + (2.0 * spacing);
    }
}