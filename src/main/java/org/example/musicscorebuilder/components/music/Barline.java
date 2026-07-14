package org.example.musicscorebuilder.components.music;

public class Barline extends Element {
    public enum Type { START, END }

    private BarlineStyle style;
    private Barline.Type type;

    public Barline(BarlineStyle style, Barline.Type type) {
        this.style = style;
        this.type = type;
    }

    public BarlineStyle getStyle() { return style; }
    public void setStyle(BarlineStyle style) { this.style = style; }
    public Barline.Type getType() { return type; }
}
