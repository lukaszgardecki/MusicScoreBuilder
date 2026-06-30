package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Measure;

public class MeasureLayout {
    private static final double PIXELS_PER_DURATION = 20.0;
    private final Measure measure;
    private final double width;

    public MeasureLayout(Measure measure) {
        this.measure = measure;
        this.width = measure.getDuration() * PIXELS_PER_DURATION;
    }

    public double getWidth() { return width; }
}