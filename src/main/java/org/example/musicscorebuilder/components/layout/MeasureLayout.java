package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class MeasureLayout {
    private final ScoreStyle style;
    private final Measure measure;
    private final List<StaffLayout> staves = new ArrayList<>();
    private final List<SegmentLayout> segments = new ArrayList<>();
    private double x, y;

    public MeasureLayout(Measure measure, double x, ScoreStyle scoreStyle) {
        this.style = scoreStyle;
        this.measure = measure;
        this.x = x;
        this.y = 0;
    }

    public void add(StaffLayout staffLayout) {
        staves.add(staffLayout);
    }

    public void add(SegmentLayout segmentLayout) {
        segments.add(segmentLayout);
    }

    public void addClef() {
        SegmentLayout seg = new SegmentLayout(SegmentType.CLEF, this);
        seg.addClef();
        segments.addFirst(seg);
    }
    public void addStartBarline(Barline barline) {
        SegmentLayout seg = new SegmentLayout(SegmentType.START_BARLINE, this);
        seg.addStartBarline(barline);
        segments.addFirst(seg);
    }

    public void addKeySignature(KeySignature keySignature) {
        SegmentLayout seg = new SegmentLayout(SegmentType.KEY_SIG, this);
        seg.addKeySignature(keySignature);
        segments.addFirst(seg);
    }

    public void addTimeSignature(TimeSignature timeSignature) {
        SegmentLayout seg = new SegmentLayout(SegmentType.TIME_SIG, this);
        seg.addTimeSignature(timeSignature);
        segments.addFirst(seg);
    }

    public ScoreStyle getScoreStyle() { return style; }
    public Measure getMeasure() { return measure; }
    public List<SegmentLayout> getSegments() { return segments; }
    public List<StaffLayout> getStaffs() { return staves; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return segments.stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public double getHeight() {
        if (staves.isEmpty()) return 0.0;
        double totalStavesHeight = staves.stream().mapToDouble(StaffLayout::getHeight).sum();
        double totalSpacing = (staves.size() - 1) * style.getStaffSpacing();
        return totalStavesHeight + totalSpacing;
    }
    public void setX(double x) { this.x = x; }
}
