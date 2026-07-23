package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class MeasureLayout {
    private final ScoreStyle style;
    private final Measure measure;
    private final List<StaffLayout> staves = new ArrayList<>();
    private final List<SegmentLayout> segments = new ArrayList<>();
    private List<BeamGroupLayout> beams = new ArrayList<>();
    private double x, y;

    public MeasureLayout(Measure measure, double x, ScoreStyle scoreStyle) {
        this.style = scoreStyle;
        this.measure = measure;
        this.x = x;
        this.y = 0;
    }

    public void add(StaffLayout staffLayout) { staves.add(staffLayout); }
    public void add(SegmentLayout segmentLayout) { segments.add(segmentLayout); }

    public void addSystemClef() {
        SegmentLayout seg = new SegmentLayout(SegmentType.CLEF, this);
        seg.addClef();
        seg.setSystemGenerated(true);
        segments.addFirst(seg);
    }
    public void addSystemStartBarline(Barline barline) {
        SegmentLayout seg = new SegmentLayout(SegmentType.START_BARLINE, this);
        seg.addStartBarline(barline);
        seg.setSystemGenerated(true);
        segments.addFirst(seg);
    }

    public void addSystemKeySignature(KeySignature keySignature) {
        SegmentLayout seg = new SegmentLayout(SegmentType.KEY_SIG, this);
        seg.addKeySignature(keySignature);
        seg.setSystemGenerated(true);
        segments.addFirst(seg);
    }

    public void addSystemTimeSignature(TimeSignature timeSignature) {
        SegmentLayout seg = new SegmentLayout(SegmentType.TIME_SIG, this);
        seg.addTimeSignature(timeSignature);
        seg.setSystemGenerated(true);
        segments.addFirst(seg);
    }

    public void remove1stMeasureAttributes() {
        segments.removeIf(SegmentLayout::isSystemGenerated);
    }

    public void resetLayoutState() {
        this.x = 0.0;
        for (SegmentLayout segment : segments) {
            segment.setExtraWidth(0.0);
        }
    }

    public MeasureStaffSelection getElementsRegionAt(double measureX, double measureY) {
        for (StaffLayout staff : staves) {
            double staffY = staff.getY();
            double staffHeight = staff.getHeight();

            if (measureY >= staffY && measureY <= (staffY + staffHeight)) {
                return new MeasureStaffSelection(this, staff);
            }
        }
        return staves.isEmpty() ? null : new MeasureStaffSelection(this, staves.getFirst());
    }

    public ScoreStyle getScoreStyle() { return style; }
    public Measure getMeasure() { return measure; }
    public List<SegmentLayout> getSegments() { return segments; }
    public List<StaffLayout> getStaffs() { return staves; }
    public List<BeamGroupLayout> getBeamGroups() { return beams; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return segments.stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public double getHeight() {
        if (staves.isEmpty()) return 0.0;
        double totalStavesHeight = staves.stream().mapToDouble(StaffLayout::getHeight).sum();
        double totalSpacing = (staves.size() - 1) * style.getStaffSpacing();
        return totalStavesHeight + totalSpacing;
    }

    public int getVoiceCountForStaff(StaffLayout staffLayout) {
        Staff staff = staffLayout.getStaff();
        return measure.countVoicesByStaff(staff);
    }

    public void setX(double x) { this.x = x; }
    public void setBeamGroups(List<BeamGroupLayout> beamGroups) { this.beams = beamGroups; }
}
