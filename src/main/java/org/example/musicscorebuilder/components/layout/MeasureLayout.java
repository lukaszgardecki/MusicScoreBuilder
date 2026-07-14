package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Barline;
import org.example.musicscorebuilder.components.music.Measure;

import java.util.ArrayList;
import java.util.List;

public class MeasureLayout {
    private final Measure measure;
    private final List<StaffLayout> staves = new ArrayList<>();
    private final List<SegmentLayout> segments = new ArrayList<>();
    private double staffSpacing;
    private double x, y;

    public MeasureLayout(Measure measure, double x) {
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

    public void addAtBegin(Barline barline) {
        SegmentLayout seg3 = new SegmentLayout(this);
        staves.forEach(staff -> seg3.add(staff.getKeySigLayout()));
        segments.addFirst(seg3);

        SegmentLayout seg2 = new SegmentLayout(this);
        staves.forEach(staff -> seg2.add(staff.getClefLayout()));
        segments.addFirst(seg2);

        if (barline == null) return;
        SegmentLayout seg1 = new SegmentLayout(this);
        seg1.add(new BarlineLayout(barline, seg1));
        segments.addFirst(seg1);
    }

    public List<SegmentLayout> getSegments() { return segments; }
    public List<StaffLayout> getStaffs() { return staves; }
    public double getStaffSpacing() { return staffSpacing; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return segments.stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public double getHeight() {
        if (staves.isEmpty()) return 0.0;
        double totalStavesHeight = staves.stream().mapToDouble(StaffLayout::getHeight).sum();
        double totalSpacing = (staves.size() - 1) * staffSpacing;
        return totalStavesHeight + totalSpacing;
    }
    public void setStaffSpacing(double staffSpacing) { this.staffSpacing = staffSpacing; }
    public void setX(double x) { this.x = x; }
}
