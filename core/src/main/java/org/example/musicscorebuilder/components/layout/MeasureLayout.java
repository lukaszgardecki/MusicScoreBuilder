package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class MeasureLayout {
    private final ScoreStyle style;
    private SystemLayout parent;
    private final Measure measure;
    private final List<StaffLayout> staves = new ArrayList<>();
    private final List<SegmentLayout> segments = new ArrayList<>();
    private List<BeamGroupLayout> beams = new ArrayList<>();
    private VoltaSliceLayout voltaSlice = null;
    private List<JumpMarkLayout> jumpMarks = new ArrayList<>();
    private double x, y;

    public MeasureLayout(Measure measure, SystemLayout parent, ScoreStyle scoreStyle) {
        this.style = scoreStyle;
        this.parent = parent;
        this.measure = measure;
        this.x = parent.getWidth();
        this.y = 0;
    }

    public MeasureLayout(Measure measure, double x, ScoreStyle scoreStyle) {
        this.style = scoreStyle;
        this.parent = null;
        this.measure = measure;
        this.x = x;
        this.y = 0;
    }

    public void add(StaffLayout staffLayout) { staves.add(staffLayout); }
    public void add(SegmentLayout segmentLayout) { segments.add(segmentLayout); }
    public void add(JumpMarkLayout jumpMarkLayout) { jumpMarks.add(jumpMarkLayout); }

    public void addSystemClef() {
        SegmentLayout seg = new SegmentLayout(SegmentType.CLEF, this);
        seg.addClef();
        seg.setSystemGenerated(true);
        segments.add(0, seg);
    }
    public void addSystemStartBarline(Barline barline) {
        SegmentLayout seg = new SegmentLayout(SegmentType.START_BARLINE, this);
        seg.addStartBarline(barline);
        seg.setSystemGenerated(true);
        segments.add(0, seg);
    }

    public void addSystemKeySignature(KeySignature keySignature) {
        SegmentLayout seg = new SegmentLayout(SegmentType.KEY_SIG, this);
        seg.addKeySignature(keySignature);
        seg.setSystemGenerated(true);
        segments.add(0, seg);
    }

    public void addSystemTimeSignature(TimeSignature timeSignature) {
        SegmentLayout seg = new SegmentLayout(SegmentType.TIME_SIG, this);
        seg.addTimeSignature(timeSignature);
        seg.setSystemGenerated(true);
        segments.add(0, seg);
    }

    public void remove1stMeasureAttributes() {
        segments.removeIf(SegmentLayout::isSystemGenerated);
    }

    public void clearJumpMarks() { this.jumpMarks.clear(); }

    public void resetLayoutState() {
        this.x = 0.0;
        for (int i = 0; i < segments.size(); i++) {
            SegmentLayout segment = segments.get(i);
            segment.setExtraWidth(0.0);
            if (segment.getType() == SegmentType.END_BARLINE) {
                segment.setType(SegmentType.BARLINE);
            }
        }
    }

    public MeasureStaffSelection getElementsRegionAt(double measureX, double measureY) {
        for (int i = 0; i < staves.size(); i++) {
            StaffLayout staff = staves.get(i);
            double staffY = staff.getY();
            double staffHeight = staff.getHeight();

            if (measureY >= staffY && measureY <= (staffY + staffHeight)) {
                return new MeasureStaffSelection(this, staff);
            }
        }
        return staves.isEmpty() ? null : new MeasureStaffSelection(this, staves.get(0));
    }

    public ScoreStyle getScoreStyle() { return style; }
    public SystemLayout getParent() { return parent; }
    public Measure getMeasure() { return measure; }
    public List<SegmentLayout> getSegments() { return segments; }
    public List<StaffLayout> getStaffs() { return staves; }
    public List<BeamGroupLayout> getBeamGroups() { return beams; }
    public VoltaSliceLayout getVoltaSlice() { return voltaSlice; }
    public List<JumpMarkLayout> getJumpMarks() { return jumpMarks; }
    public double getX() { return x; }
    public double getY() { return y; }

    public double getWidth() {
        double totalWidth = 0.0;
        for (int i = 0; i < segments.size(); i++) {
            totalWidth += segments.get(i).getWidth();
        }
        return totalWidth;
    }

    public double getNominalHeight() {
        if (staves.isEmpty()) return 0.0;

        StaffLayout lastStaff = staves.get(staves.size() - 1);
        return lastStaff.getY() + lastStaff.getHeight();
    }

    public double getTopOverflow() {
        double minRelY = 0.0;

        for (SegmentLayout segment : segments) {
            for (ElementLayout element : segment.getElements()) {
                double elementTopY = element instanceof NoteLayout n ? n.getMinY() : element.getY();
                if (elementTopY < minRelY) {
                    minRelY = elementTopY;
                }
            }
        }

        if (voltaSlice != null && -voltaSlice.getYOffset() < minRelY) {
            minRelY = -voltaSlice.getYOffset();
        }

        for (JumpMarkLayout mark : jumpMarks) {
            if (mark.getBoxY() < minRelY) {
                minRelY = mark.getBoxY();
            }
        }

        return Math.abs(minRelY);
    }

    public double getBottomOverflow() {
        if (staves.isEmpty()) return 0.0;
        StaffLayout lastStaff = staves.get(staves.size() - 1);
        return lastStaff.getBottomOverflow();
    }

    public double getHeight() {
        return getTopOverflow() + getNominalHeight() + getBottomOverflow();
    }

    public int getVoiceCountForStaff(int staffId) {
        return measure.countVoicesByStaff(staffId);
    }

    public void setX(double x) { this.x = x; }
    public void setBeamGroups(List<BeamGroupLayout> beamGroups) { this.beams = beamGroups; }
    public void setParent(SystemLayout parent) { this.parent = parent; }
    public void setVoltaSlice(VoltaSliceLayout voltaSlice) { this.voltaSlice = voltaSlice; }
}