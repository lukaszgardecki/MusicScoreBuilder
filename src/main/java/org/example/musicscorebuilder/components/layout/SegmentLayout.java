package org.example.musicscorebuilder.components.layout;

public class SegmentLayout {
    private static final double DEFAULT_WIDTH = 20.0;
    private SegmentType type;
    private double width;
    private boolean isGenerated = true;

    public SegmentLayout(SegmentType type) {
        this.type = type;
        isGenerated = type == SegmentType.CHORD_REST;
        width = switch(type) {
            case CLEF -> 15;
            default -> DEFAULT_WIDTH;
        };
    }

    public SegmentType getType() { return type; }
    public double getMinWidth() { return isGenerated ? DEFAULT_WIDTH : 0; }
    public double getWidth() { return isGenerated ? width : 0; }
    public boolean isGenerated() { return isGenerated; }
    public boolean hasStaticWidth() {
        return type == SegmentType.CLEF
            || type == SegmentType.TIME_SIG
            || type == SegmentType.KEY_SIG;
    }
    public boolean hasDynamicWidth() { return !hasStaticWidth(); }

    public void setWidth(double width) { this.width = width; }
    public void setGenerated(boolean isGenerated) { this.isGenerated = isGenerated; }
}