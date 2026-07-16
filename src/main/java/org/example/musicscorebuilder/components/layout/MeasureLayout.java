package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.Mode;

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

    public ElementLayout findClickedElement(double systemX, double systemY) {
        double measureMusicX = systemX - this.getX();
        double measureMusicY = systemY - this.getY();

        for (SegmentLayout segment : segments) {
            ElementLayout hit = segment.findClickedElement(measureMusicX, measureMusicY);
            if (hit != null) {
                return hit;
            }
        }
        return null;
    }

    public void add(StaffLayout staffLayout) {
        staves.add(staffLayout);
    }

    public void add(SegmentLayout segmentLayout) {
        segments.add(segmentLayout);
    }

    public void add1stMeasureAttributes(Mode mode) {
        SegmentLayout seg1 = new SegmentLayout(this);
        staves.forEach(staff -> seg1.add(new TimeSigLayout(mode.getTimeSignature(), staff)));
        segments.addFirst(seg1);

        SegmentLayout seg2 = new SegmentLayout(this);
        staves.forEach(staff -> seg2.add(new KeySigLayout(mode.getKeySignature(), staff, style)));
        segments.addFirst(seg2);

        SegmentLayout seg3 = new SegmentLayout(this);
        staves.forEach(staff -> seg3.add(staff.getClefLayout()));
        segments.addFirst(seg3);

        if (mode.getStartBarline() == null) return;
        SegmentLayout seg4 = new SegmentLayout(this);
        seg4.add(new BarlineLayout(mode.getStartBarline(), seg4, style));
        segments.addFirst(seg4);
    }

    public ScoreStyle getScoreStyle() { return style; }
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
