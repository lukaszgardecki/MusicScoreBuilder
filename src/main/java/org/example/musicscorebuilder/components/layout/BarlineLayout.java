package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Barline;
import org.example.musicscorebuilder.components.music.BarlineStyle;

public class BarlineLayout extends ElementLayout {
    private final SegmentLayout parent;
    private final Barline barline;
    private final double lightLineWidth;
    private final double heavyLineWidth;
    private final double gap = 0.25;
    private final double dotSpace = 0.2;
    private final double dotRadius = 0.15;
    private double y, width;

    public BarlineLayout(Barline barline, SegmentLayout parent) {
        super(false);
        this.parent = parent;
        this.barline = barline;
        this.lightLineWidth = 0.08 + 0.04;
        this.heavyLineWidth = lightLineWidth * 4;
        this.width = switch (barline.getStyle()) {
            case SINGLE -> lightLineWidth;
            case DOUBLE -> 2 * lightLineWidth + gap;
            case END -> lightLineWidth + gap + heavyLineWidth;
            case REPEAT_LEFT, REPEAT_RIGHT -> heavyLineWidth + gap + lightLineWidth + dotSpace + 2 * dotRadius;
        };
        y = - 0.5 * 0.08;
    }

    @Override public double getY() { return y; }
    @Override public double getBoxY() { return y; }
    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return parent.getHeight() + 0.08; }

    public double getLightLineWidth() { return lightLineWidth; }
    public double getHeavyLineWidth() { return heavyLineWidth; }
    public double getGap() { return gap; }
    public double getDotSpace() { return dotSpace; }
    public double getDotRadius() { return dotRadius; }
    public BarlineStyle getStyle() { return barline.getStyle(); }
    public Barline.Type getType() { return barline.getType(); }
}
