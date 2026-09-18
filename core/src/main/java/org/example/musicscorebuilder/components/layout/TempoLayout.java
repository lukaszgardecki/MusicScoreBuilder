package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Tempo;
import org.example.musicscorebuilder.managers.FontType;

public class TempoLayout implements Selectable {

    private static TextMeasurer defaultMeasurer = new TextMeasurer() {
        @Override
        public double getTextWidth(FontType type, String text, double fontSizeInSpatium, boolean bold, boolean italic) {
            if (text == null || text.isEmpty()) return 0.0;
            return text.length() * fontSizeInSpatium * 0.55;
        }

        @Override
        public double getTextHeight(FontType type, String text, double fontSizeInSpatium, boolean bold, boolean italic) {
            return fontSizeInSpatium;
        }
    };

    public static void setDefaultMeasurer(TextMeasurer measurer) {
        if (measurer != null) {
            defaultMeasurer = measurer;
        }
    }

    private final ScoreStyle style;
    private final Tempo tempo;
    private final NoteRestLayout noteRest;
    private final TextMeasurer textMeasurer;
    private boolean selected = false;

    public TempoLayout(Tempo tempo, NoteRestLayout noteRest) {
        this(tempo, noteRest, defaultMeasurer);
    }

    public TempoLayout(Tempo tempo, NoteRestLayout noteRest, TextMeasurer measurer) {
        this.tempo = tempo;
        this.noteRest = noteRest;
        this.style = (noteRest != null) ? noteRest.getScoreStyle() : null;
        this.textMeasurer = (measurer != null) ? measurer : defaultMeasurer;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public boolean contains(double px, double py) {
        String text = getText();
        if (text.isEmpty()) return false;

        double minX = getXOffset();
        double maxX = minX + getWidth();

        double lineY = -getY();
        double topY = lineY - getHeight();

        double minY = Math.min(lineY, topY);
        double maxY = Math.max(lineY, topY);

        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public SegmentLayout getSegment() { return (noteRest != null) ? noteRest.getSegment() : null; }
    @Override public StaffLayout getStaff() { return (noteRest != null) ? noteRest.getStaff() : null; }
    @Override public int getVoice() { return 1; }

    public ScoreStyle getStyle() { return style; }
    public Tempo getTempo() { return tempo; }
    public NoteRestLayout getNoteRest() { return noteRest; }

    public double getX() { return (noteRest != null ? noteRest.getX() : 0.0) + getXOffset(); }
    public double getY() { return getYOffset(); }
    public double getXOffset() { return (tempo != null && tempo.getXOffset() != null) ? tempo.getXOffset() : (style != null ? style.getTempoTextXOffset() : 0.0); }
    public double getYOffset() { return (tempo != null && tempo.getYOffset() != null) ? tempo.getYOffset() : (style != null ? style.getTempoTextYOffset() : 0.0); }
    public String getText() { return (tempo != null && tempo.getText() != null) ? tempo.getText() : ""; }
    public double getFontSize() { return (tempo != null && tempo.getFontSize() != null) ? tempo.getFontSize() : (style != null ? style.getTempoTextFontSize() : 12.0); }

    public boolean isBold() {
        return (tempo != null && tempo.getBold() != null) ? tempo.getBold() : true;
    }

    public boolean isItalic() {
        return (tempo != null && tempo.getItalic() != null) ? tempo.getItalic() : false;
    }

    public double getHeight() {
        String text = getText();
        if (text.isEmpty()) return getFontSize();
        return textMeasurer.getTextHeight(FontType.FREE_SERIF, text, getFontSize(), isBold(), isItalic());
    }

    public double getWidth() {
        String text = getText();
        if (text.isEmpty()) return 0.0;
        return textMeasurer.getTextWidth(FontType.FREE_SERIF, text, getFontSize(), isBold(), isItalic());
    }
}