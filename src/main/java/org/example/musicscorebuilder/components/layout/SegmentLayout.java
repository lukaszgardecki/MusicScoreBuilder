package org.example.musicscorebuilder.components.layout;

public class SegmentLayout {
    private static final double DEFAULT_WIDTH = 2.0;

    private SegmentType type;
    private double width, height;
    private double x, y = 0;
    private boolean isGenerated = true;

    public SegmentLayout(SegmentType type, double x, double height) {
        this.type = type;
        isGenerated = type == SegmentType.CHORD_REST;
        width = switch(type) {
            case KEY_SIG -> 4.0;
            case TIME_SIG -> 2.0;
            default -> DEFAULT_WIDTH;
        };
        this.height = height;
        this.x = x;
    }

    public SegmentType getType() { return type; }
    public double getWidth() { return isGenerated ? width : 0; }
    public double getHeight() { return  isGenerated ? height : 0; }

    public double getX() { return x; }
    public double getY() { return y; }
    public boolean isGenerated() { return isGenerated; }
    public boolean hasStaticWidth() {
        return type == SegmentType.CLEF
            || type == SegmentType.TIME_SIG
            || type == SegmentType.KEY_SIG;
    }
    public boolean hasDynamicWidth() { return !hasStaticWidth(); }

    public void setWidth(double width) { this.width = width; }
    public void setGenerated(boolean isGenerated) { this.isGenerated = isGenerated; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
}