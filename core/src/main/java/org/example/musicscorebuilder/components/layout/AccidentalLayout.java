package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;

public class AccidentalLayout implements Selectable {
    private final ScoreStyle style;
    private final NoteLayout parent;
    private boolean selected;

    public AccidentalLayout(NoteLayout parent) {
        this.parent = parent;
        this.style = parent.getScoreStyle();
    }

    private Pitch getPitch() {
        return (parent != null && parent.getNote() != null) ? parent.getNote().getPitch() : null;
    }

    private Leland getFontData() {
        Pitch pitch = getPitch();
        if (pitch == null) return Leland.ACC_NATURAL;

        return switch (pitch.getAlter()) {
            case -2 -> Leland.ACC_DOUBLE_FLAT;
            case -1 -> Leland.ACC_FLAT;
            case 1  -> Leland.ACC_SHARP;
            case 2  -> Leland.ACC_DOUBLE_SHARP;
            default -> Leland.ACC_NATURAL;
        };
    }

    public boolean isVisible() {
        Pitch pitch = getPitch();
        if (pitch == null) return false;

        int noteAlter = pitch.getAlter();
        int activeAlter = calculateEffectiveAlterBefore(parent, pitch);

        return noteAlter != activeAlter;
    }

    private int calculateEffectiveAlterBefore(NoteLayout targetNoteLayout, Pitch pitch) {
        SegmentLayout targetSeg = targetNoteLayout.getParent();
        if (targetSeg == null) return 0;

        MeasureLayout measureLayout = targetSeg.getParent();
        if (measureLayout == null) return 0;

        Measure measure = targetSeg.getSegment() != null ? targetSeg.getSegment().getParent() : null;
        int activeAlter = (measure != null) ? measure.getKeySignatureAlterForStep(pitch.getStep()) : 0;

        for (SegmentLayout segLayout : measureLayout.getSegments()) {
            if (segLayout == targetSeg) break;

            for (ElementLayout el : segLayout.getElements()) {
                if (el instanceof NoteLayout prevNote) {
                    Pitch prevPitch = prevNote.getNote().getPitch();
                    if (prevPitch != null
                            && prevPitch.getStep() == pitch.getStep()
                            && prevPitch.getOctave() == pitch.getOctave()) {
                        activeAlter = prevPitch.getAlter();
                    }
                }
            }
        }
        return activeAlter;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return parent.getVoice(); }

    @Override
    public boolean contains(double x, double y) {
        if (!isVisible()) return false;

        double minX = parent.getX() + getX();
        double maxX = minX + getWidth();

        Leland fontData = getFontData();
        double h = fontData.getHeight();
        double minY = parent.getY() - (h / 2.0);
        double maxY = minY + h;

        return x >= minX && x <= maxX && y >= minY && y <= maxY;
    }

    @Override public SegmentLayout getSegment() { return parent.getParent(); }
    @Override public StaffLayout getStaff() { return parent.getStaff(); }

    public double getX() { return -getWidth() - style.getNoteAccSpacing(); }
    public double getY() { return 0; }
    public double getWidth() {
        if (!isVisible()) return 0.0;
        Leland fontData = getFontData();
        return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing();
    }
    public NoteLayout getParent() { return parent; }
    public ScoreStyle getScoreStyle() { return style; }
    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return getFontData().getCode(); }
}