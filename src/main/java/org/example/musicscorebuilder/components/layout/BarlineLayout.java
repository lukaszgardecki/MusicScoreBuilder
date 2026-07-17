package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Barline;
import org.example.musicscorebuilder.components.music.BarlineStyle;

public class BarlineLayout extends ElementLayout {
    private final ScoreStyle style;
    private final SegmentLayout parent;
    private final Barline barline;
    private final double lightLineWidth;
    private final double heavyLineWidth;
    private final double gap;
    private final double dotSpace;
    private final double dotRadius;
    private double width;

    public BarlineLayout(Barline barline, SegmentLayout parent, ScoreStyle scoreStyle) {
        super(false);
        this.style = scoreStyle;
        this.parent = parent;
        this.barline = barline;
        this.lightLineWidth = style.getBarlineLightWidth();
        this.heavyLineWidth = style.getBarlineHeavyWidth();
        this.gap = style.getBarlineGap();
        this.dotSpace = style.getBarlineDotSpace();
        this.dotRadius = style.getBarlineDotRadius();
        this.width = switch (barline.getStyle()) {
            case SINGLE -> lightLineWidth;
            case DOUBLE -> 2 * lightLineWidth + gap;
            case END -> lightLineWidth + gap + heavyLineWidth;
            case REPEAT_LEFT, REPEAT_RIGHT -> heavyLineWidth + gap + lightLineWidth + dotSpace + 2 * dotRadius;
        };
    }

    @Override public double getX() { return parent.getWidth() - getWidth(); }
    @Override public double getY() { return -0.5 * style.getStaffLineWidth(); }
    @Override public double getBoxY() { return getY(); }
    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return parent.getHeight() + style.getStaffLineWidth(); }

    public double getLightLineWidth() { return lightLineWidth; }
    public double getHeavyLineWidth() { return heavyLineWidth; }
    public double getGap() { return gap; }
    public double getDotSpace() { return dotSpace; }
    public double getDotRadius() { return dotRadius; }
    public BarlineStyle getStyle() { return barline.getStyle(); }
    public Barline.Type getType() { return barline.getType(); }
}
