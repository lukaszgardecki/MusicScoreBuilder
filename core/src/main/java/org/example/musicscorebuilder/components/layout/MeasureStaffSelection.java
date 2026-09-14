package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MeasureStaffSelection implements Selectable {
    private final MeasureLayout measure;
    private final List<MeasureLayout> measures = new ArrayList<>();
    private final StaffLayout staff;
    private boolean selected = false;

    public MeasureStaffSelection(MeasureLayout measure, StaffLayout staff) {
        this.measure = measure;
        if (measure != null) {
            this.measures.add(measure);
        }
        this.staff = staff;
    }

    public MeasureStaffSelection(List<MeasureLayout> measures, StaffLayout staff) {
        if (measures != null && !measures.isEmpty()) {
            this.measures.addAll(measures);
            this.measure = measures.get(0);
        } else {
            this.measure = null;
        }
        this.staff = staff;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return 1; }

    @Override
    public boolean contains(double measureX, double measureY) {
        if (staff == null || measure == null) return false;

        double staffY = staff.getY();
        double staffHeight = staff.getHeight();
        boolean yMatches = measureY >= staffY && measureY <= (staffY + staffHeight);

        double startX = getElementsX();
        double width = getElementsWidth();

        if (width <= 0) {
            width = measure.getWidth() - startX;
        }

        double endX = startX + width;
        boolean xMatches = measureX >= startX && measureX <= endX;

        return xMatches && yMatches;
    }

    @Override
    public SegmentLayout getSegment() {
        return (measure != null && measure.getSegments() != null && !measure.getSegments().isEmpty())
                ? measure.getSegments().get(0)
                : null;
    }

    @Override public StaffLayout getStaff() { return staff; }
    public MeasureLayout getMeasure() { return measure; }

    public List<MeasureLayout> getAllMeasures() {
        return measures;
    }

    public MeasureLayout getFirstMeasure() {
        return getMeasure();
    }

    public MeasureLayout getLastMeasure() {
        return measures.isEmpty() ? null : measures.get(measures.size() - 1);
    }

    public Map<SystemLayout, List<MeasureLayout>> getMeasuresBySystem() {
        Map<SystemLayout, List<MeasureLayout>> map = new LinkedHashMap<>();
        for (MeasureLayout m : measures) {
            if (m != null && m.getParent() != null) {
                map.computeIfAbsent(m.getParent(), k -> new ArrayList<>()).add(m);
            }
        }
        return map;
    }

    public double getElementsX() {
        return getElementsX(this.measure);
    }

    public double getElementsX(MeasureLayout targetMeasure) {
        if (targetMeasure == null || targetMeasure.getSegments() == null) return 0.0;
        double currentX = 0.0;
        for (SegmentLayout seg : targetMeasure.getSegments()) {
            if (seg.getType() == SegmentType.NOTEREST) {
                break;
            }
            currentX += seg.getWidth();
        }
        return currentX - targetMeasure.getScoreStyle().getSegmentBarlineRightMargin();
    }

    public double getElementsWidth() {
        return getElementsWidth(this.measure);
    }

    public double getElementsWidth(MeasureLayout targetMeasure) {
        if (targetMeasure == null || targetMeasure.getSegments() == null) return 0.0;
        return targetMeasure.getSegments().stream()
                .filter(seg -> seg.getType() == SegmentType.NOTEREST)
                .mapToDouble(SegmentLayout::getWidth)
                .sum() + targetMeasure.getScoreStyle().getSegmentBarlineRightMargin();
    }
}