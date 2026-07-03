package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Measure;

public class MeasureLayout {
    private static final double PIXELS_PER_DURATION = 20.0;
    private final Measure measure;
    private final BarlineLayout barlineLayout;
    private final double minWidth;
    private double finalWidth;

    public MeasureLayout(Measure measure) {
        this.measure = measure;
        this.minWidth = measure.getDuration() * PIXELS_PER_DURATION;
        this.finalWidth = minWidth;
        this.barlineLayout = new BarlineLayout(finalWidth);
    }

    public double getMinWidth() { return minWidth; }
    public double getFinalWidth() { return finalWidth; }
    public void setFinalWidth(double w) {
        this.finalWidth = w;
        barlineLayout.setX(w);
    }
    public BarlineLayout getBarlineLayout() { return barlineLayout; }
}