package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;

import java.util.ArrayList;
import java.util.List;

public class PartLayout {
    private final Part part;
    private final List<StaffLayout> staffLayouts = new ArrayList<>();
    private double staffSpacing = 40;

    public PartLayout(Part part) {
        this.part = part;
    }

    public void add(StaffLayout staffLayout) {
        staffLayouts.add(staffLayout);
    }

    public Part getPart() { return part; }
    public List<StaffLayout> getStaffLayouts() { return staffLayouts; }

    public double getHeight() {
        double sum = staffLayouts.stream().mapToDouble(StaffLayout::getHeight).sum();
        double space = staffSpacing * (staffLayouts.size() - 1);
        return sum + space;
    }

    public double getWidth() { return staffLayouts.stream().mapToDouble(StaffLayout::getOccupiedWidth).max().orElse(0); }
    public double getStaffSpacing() { return staffSpacing; }

    public StaffLayout getOrCreateStaff(StaffLayout staffLayout) {
        for (StaffLayout sl : this.getStaffLayouts()) {
            if (sl.getStaff() == staffLayout.getStaff()) return sl;
        }
        this.add(staffLayout);
        return staffLayout;
    }
}