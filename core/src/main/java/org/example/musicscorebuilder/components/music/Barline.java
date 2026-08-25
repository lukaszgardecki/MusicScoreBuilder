package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonIdentityInfo(generator = ObjectIdGenerators.None.class)
public class Barline extends Element {
    public enum Type { START, END }

    private BarlineStyle style;
    private Barline.Type type;


    @SuppressWarnings("unused")
    private Barline() {
        super(null);
    }

    public Barline(BarlineStyle style, Measure parent) {
        this(style, Type.END, parent);
    }

    public Barline(BarlineStyle style, Barline.Type type, Measure parent) {
        super(parent);
        this.style = style;
        this.type = type;
    }

    public BarlineStyle getStyle() { return style; }
    public Barline.Type getType() { return type; }

    public void setStyle(BarlineStyle style) {
        this.style = style;
        super.hasChanged();
    }
}
