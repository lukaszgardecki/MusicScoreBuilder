package org.example.musicscorebuilder.components.layout;

public class SegmentLayout {
    private static final double DEFAULT_WIDTH = 20.0;
    private double width;

    public SegmentLayout() {
        width = DEFAULT_WIDTH;
    }

    public double getMinWidth() { return DEFAULT_WIDTH; }
    public double getWidth() { return width; }
    public void setWidth(double width) { this.width = width; }
}