package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Staff;

import java.util.ArrayList;
import java.util.List;

public class PartLayout {
    private final Part part;
    private final BraceLayout braceLayout = new BraceLayout();;
    private final List<StaffLayout> staffLayouts = new ArrayList<>();
    private double staffSpacing = 25;

    public PartLayout(Part part) {
        this.part = part;
    }

    public Part getPart() { return part; }
    public BraceLayout getBraceLayout() { return braceLayout; }
    public List<StaffLayout> getStaffLayouts() { return staffLayouts; }

    public double getHeight() {
        double sum = staffLayouts.stream().mapToDouble(StaffLayout::getHeight).sum();
        double space = staffSpacing * (staffLayouts.size() - 1);
        return sum + space;
    }

    public double getWidth() {
        double staves = staffLayouts.stream().mapToDouble(StaffLayout::getOccupiedWidth).max().orElse(0);
        double braceWidth = braceLayout.getWidth();
        return staves + braceWidth;
    }
    public double getStaffSpacing() { return staffSpacing; }
    public double getBraceWidth() { return braceLayout.getWidth(); }

    public StaffLayout getOrCreateStaff(Staff staff) {
        for (StaffLayout sl : this.getStaffLayouts()) {
            if (sl.getStaff() == staff) return sl;
        }
        StaffLayout staffLayout = new StaffLayout(staff);
        staffLayouts.add(staffLayout);
        braceLayout.setHeight(getHeight());
        return staffLayout;
    }
}