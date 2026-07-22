package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.SegmentType;

public class MeasureStaffSelection implements Selectable {
    private final MeasureLayout measure;
    private final StaffLayout staff;
    private boolean selected = false;

    public MeasureStaffSelection(MeasureLayout measure, StaffLayout staff) {
        this.measure = measure;
        this.staff = staff;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return 1; }
    @Override
    public boolean contains(double measureX, double measureY) {
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

    public MeasureLayout getMeasure() { return measure; }
    public StaffLayout getStaff() { return staff; }

    public double getElementsX() {
        double currentX = 0.0;
        for (SegmentLayout seg : measure.getSegments()) {
            if (seg.getType() == SegmentType.CHORDREST) {
                break;
            }
            currentX += seg.getWidth();
        }
        return currentX;
    }

    public double getElementsWidth() {
        return measure.getSegments().stream()
                .filter(seg -> seg.getType() == SegmentType.CHORDREST)
                .mapToDouble(SegmentLayout::getWidth)
                .sum();
    }
}