package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Measure;

import java.util.ArrayList;
import java.util.List;

public class PMeasureLayout {
    private PartLayout parent;
    private final Measure measure;
    private final List<SMeasureLayout> sMeasures = new ArrayList<>();
    private double x, y;

    public PMeasureLayout(PartLayout parent, Measure measure, double x,  double y) {
        this.parent = parent;
        this.measure = measure;
        this.x = x;
        this.y = y;
    }

    public void add(SMeasureLayout sMeasureLayout) {
        sMeasures.add(sMeasureLayout);
    }

    public Measure getMeasure() { return measure; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return sMeasures.stream().mapToDouble(SMeasureLayout::getWidth).max().orElse(0); }
    public double getHeight() { return sMeasures.stream().mapToDouble(SMeasureLayout::getHeight).sum(); }
    public List<SMeasureLayout> getSMeasures() { return sMeasures; }

    public void setX(double x) { this.x = x; }
    public void setFirstInSystem() {
        if (!sMeasures.isEmpty()) {
            SMeasureLayout lastMeasure = sMeasures.getLast();
            sMeasures.forEach(sm -> {
                if (sm == lastMeasure) sm.setSystemStartBarline(parent.getSpaceBelow());
                else sm.setSystemStartBarline(0);

                var segments = sm.getSegments();
                for (int i = 0; i < segments.size(); i++) {
                    if (i == 0 || i == 1 || i == 2) {
                        SegmentLayout segment = segments.get(i);
                        segment.setGenerated(true);
                    }
                    double segmentX = 0.0;
                    for (SegmentLayout segment : segments) {
                        segment.setX(segmentX);
                        segmentX += segment.getWidth();
                    }
                }
            });
        }
    }
}
