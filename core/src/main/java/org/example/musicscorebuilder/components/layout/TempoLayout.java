package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Tempo;

public class TempoLayout implements Selectable {
    private final ScoreStyle style;
    private final Tempo tempo;
    private final NoteRestLayout noteRest;
    private boolean selected = false;

    public TempoLayout(Tempo tempo, NoteRestLayout noteRest) {
        this.tempo = tempo;
        this.noteRest = noteRest;
        this.style = noteRest.getScoreStyle();
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public boolean contains(double px, double py) {
        String text = getText();
        if (text.isEmpty()) return false;

        double minX = getXOffset();
        double maxX = minX + getWidth();

        double lineY = -getYOffset();
        double topY = lineY - getHeight();

        double minY = Math.min(lineY, topY);
        double maxY = Math.max(lineY, topY);

        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public SegmentLayout getSegment() { return noteRest.getSegment(); }
    @Override public StaffLayout getStaff() { return noteRest.getStaff(); }
    @Override public int getVoice() { return 1; }

    public ScoreStyle getStyle() { return style; }

    public Tempo getTempo() { return tempo; }

    public NoteRestLayout getNoteRest() { return noteRest; }

    public double getX() { return (noteRest != null ? noteRest.getX() : 0.0) + getXOffset(); }
    public double getY() { return getYOffset(); }
    public double getXOffset() { return (tempo.getXOffset() != null) ? tempo.getXOffset() : style.getTempoTextXOffset(); }
    public double getYOffset() { return (tempo.getYOffset() != null) ? tempo.getYOffset() : style.getTempoTextYOffset(); }
    public String getText() { return (tempo.getText() != null ? tempo.getText() : ""); }
    public double getFontSize() { return (tempo.getFontSize() != null) ? tempo.getFontSize() : style.getTempoTextFontSize(); }
    public double getHeight() { return getFontSize(); }
    public double getWidth() {
        String text = getText();
        if (text.isEmpty()) return 0.0;
        return text.length() * getFontSize() * 0.55;
    }
}