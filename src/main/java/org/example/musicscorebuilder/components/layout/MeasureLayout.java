package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Measure;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MeasureLayout {
    private final List<SegmentLayout> segments = new ArrayList<>();
    private final Measure measure;
    private final BarlineLayout rightBarline;
    private boolean firstInSystem = false;

    public MeasureLayout(Measure measure) {
        this.measure = measure;
        addSegments(measure);
        setFirstInSystem(firstInSystem);
        this.rightBarline = new BarlineLayout(getWidth());
    }

    public double getMinWidth() { return getActiveSegments().stream().mapToDouble(SegmentLayout::getMinWidth).sum(); }
    public double getWidth() { return getActiveSegments().stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public void setWidth(double w) {
        var chordRestSegments = getActiveSegments().stream()
                .filter(s -> s.getType() == SegmentType.CHORD_REST)
                .toList();
        if (chordRestSegments.isEmpty()) return;

        var staticSegments = getActiveSegments().stream()
                .filter(SegmentLayout::hasStaticWidth)
                .toList();

        double staticWidthSum = staticSegments.stream().mapToDouble(SegmentLayout::getMinWidth).sum();
        double remainingWidthForNotes = w - staticWidthSum;
        if (remainingWidthForNotes < 0) remainingWidthForNotes = 0;

        double widthPerNote = remainingWidthForNotes / chordRestSegments.size();
        chordRestSegments.forEach(segment -> segment.setWidth(widthPerNote));
        rightBarline.setX(w);
    }
    public BarlineLayout getRightBarline() { return rightBarline; }
    public List<SegmentLayout> getSegments() { return segments; }
    public List<SegmentLayout> getActiveSegments() { return segments.stream().filter(SegmentLayout::isGenerated).toList(); }
    public int getNumber() { return measure.getNumber(); }

    public void setFirstInSystem(boolean firstInSystem) {
        this.firstInSystem = firstInSystem;
        segments.get(0).setGenerated(firstInSystem);
        segments.get(1).setGenerated(firstInSystem);
        if (rightBarline != null) {
            rightBarline.setX(getWidth());
        }
    }


    private void addSegments(Measure measure) {
        segments.add(new SegmentLayout(SegmentType.CLEF));
        segments.add(new SegmentLayout(SegmentType.TIME_SIG));
        IntStream.range(0, measure.getTimeSignature().getValue()).forEach(i -> segments.add(new SegmentLayout(SegmentType.CHORD_REST)));
    }
}