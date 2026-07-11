package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.BarlineStyle;

public class BarlineLayout {
    public enum Type { START, END }

    private BarlineStyle style;
    private final SMeasureLayout parent;
    private  double heightToPartBelow = 0.0;
    private final double lightLineWidth;
    private final double heavyLineWidth;
    private final double gap = 0.25;
    private final Type type;

    public BarlineLayout(SMeasureLayout parent, Type type, BarlineStyle style) {
        this.style = style;
        this.parent = parent;
        this.lightLineWidth = parent.getStaffLayout().getLineWidth() + 0.04;
        this.heavyLineWidth = lightLineWidth * 4;
        this.type = type;
    }

    public double getX() { return type == Type.START ? 0 : parent.getWidth(); }
    public double getY() { return parent.getStaffLayout().getLineWidth() * 0.5 * -1; }
    public BarlineStyle getStyle() { return style; }
    public double getLightLineWidth() { return lightLineWidth; }
    public double getHeavyLineWidth() { return heavyLineWidth; }
    public double getGap() { return gap; }
    public double getHeight() { return parent.getHeight() + parent.getStaffLayout().getLineWidth() + heightToPartBelow; }

    public void setHeightToPartBelow(double heightToPartBelow) { this.heightToPartBelow = heightToPartBelow; }
}
