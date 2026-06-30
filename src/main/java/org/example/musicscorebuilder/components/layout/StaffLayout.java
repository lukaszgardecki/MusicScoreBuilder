package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

import java.util.ArrayList;
import java.util.List;

public class StaffLayout {
    private final Staff staff;
    private final double width;
    private final List<MeasureLayout> measures = new ArrayList<>();

    public StaffLayout(Staff staff, double width) {
        this.staff = staff;
        this.width = width;
    }

    public Staff getStaff() {
        return staff;
    }

    public double getWidth() {
        return width;
    }

    public double getOccupiedWidth() {
        return measures.stream().mapToDouble(MeasureLayout::getWidth).sum();
    }

    public double getHeight() {
        return staff.getHeight();
    }

    public List<MeasureLayout> getMeasures() {
        return measures;
    }

    public void add(MeasureLayout measureLayout) {
        measures.add(measureLayout);
    }
}
