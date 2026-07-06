package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Measure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MeasureLayout {
    private final List<SegmentLayout> segments = new ArrayList<>();
    private final Measure measure;
    private final BarlineLayout rightBarline;

    public MeasureLayout(Measure measure) {
        this.measure = measure;
        IntStream.range(0, measure.getDuration()).forEach(i -> segments.add(new SegmentLayout()));
        double width = segments.stream().mapToDouble(SegmentLayout::getWidth).sum();
        this.rightBarline = new BarlineLayout(width);
    }

    public double getMinWidth() { return segments.stream().mapToDouble(SegmentLayout::getMinWidth).sum(); }
    public double getWidth() { return segments.stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public void setWidth(double w) {
        double widthPerSegment = w / segments.size();
        segments.forEach(segment -> segment.setWidth(widthPerSegment));
        rightBarline.setX(w);
    }
    public BarlineLayout getRightBarline() { return rightBarline; }
    public int getSegments() { return segments.size(); }
}