package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.SegmentType;

public class SegmentLayout {
    private static final double DEFAULT_WIDTH = 20.0;
    private SegmentType type;
    private double width;
    private boolean isGenerated = true;

    public SegmentLayout(SegmentType type) {
        width = DEFAULT_WIDTH;
        this.type = type;
        isGenerated = type == SegmentType.CHORD_REST;
    }

    public SegmentType getType() { return type; }
    public double getMinWidth() { return isGenerated ? DEFAULT_WIDTH : 0; }
    public double getWidth() { return isGenerated ? width : 0; }
    public boolean isGenerated() { return isGenerated; }

    public void setWidth(double width) { this.width = width; }
    public void setGenerated(boolean isGenerated) { this.isGenerated = isGenerated; }
}