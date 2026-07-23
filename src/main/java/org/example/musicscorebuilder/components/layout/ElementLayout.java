package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public abstract class ElementLayout implements Selectable {
    protected final ScoreStyle style;
    protected final SegmentLayout parent;
    private final boolean hasDynamicWidth;
    private double x = 0.0;
    private double y = 0.0;
    private boolean selected = false;

    public ElementLayout(boolean hasDynamicWidth, SegmentLayout parent) {
        this.hasDynamicWidth = hasDynamicWidth;
        this.style = parent.getScoreStyle();
        this.parent = parent;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override
    public boolean contains(double segmentMusicX, double segmentMusicY) {
        return segmentMusicX >= getX() && segmentMusicX <= (getX() + getWidth()) &&
                segmentMusicY >= getBoxY() && segmentMusicY <= (getBoxY() + getHeight());
    }
    @Override public SegmentLayout getParentSegment() { return  parent; }

    public SegmentLayout getParent() { return parent; }
    public ScoreStyle getScoreStyle() { return style; }
    public boolean hasDynamicWidth() { return hasDynamicWidth; }

    public double getX() { return x; }
    public double getY() { return y; }
    public abstract double getWidth();
    public abstract double getHeight();
    public abstract double getBoxY();

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}
