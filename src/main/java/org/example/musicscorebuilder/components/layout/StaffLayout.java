package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

import java.util.ArrayList;
import java.util.List;

public class StaffLayout {
    private final Staff staff;
    private final List<MeasureLayout> measures = new ArrayList<>();

    public StaffLayout(Staff staff) {
        this.staff = staff;
    }

    public Staff getStaff() {
        return staff;
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
        measureLayout.setFirstInSystem(measures.isEmpty());
        measures.add(measureLayout);
    }
}
