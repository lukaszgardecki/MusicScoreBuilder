package org.example.musicscorebuilder.components.layout;

public abstract class ElementLayout {
    protected final ScoreStyle style;
    private final boolean hasDynamicWidth;
    private double x = 0.0;
    private double y = 0.0;
    private boolean selected = false;

    public ElementLayout(boolean hasDynamicWidth,  ScoreStyle style) {
        this.hasDynamicWidth = hasDynamicWidth;
        this.style = style;
    }

    public ScoreStyle getScoreStyle() { return style; }
    public boolean hasDynamicWidth() { return hasDynamicWidth; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }

    public abstract double getWidth();
    public abstract double getHeight();
    public abstract double getBoxY();

    public boolean contains(double segmentMusicX, double segmentMusicY) {
        return segmentMusicX >= getX() && segmentMusicX <= (getX() + getWidth()) &&
                segmentMusicY >= getBoxY() && segmentMusicY <= (getBoxY() + getHeight());
    }
}
