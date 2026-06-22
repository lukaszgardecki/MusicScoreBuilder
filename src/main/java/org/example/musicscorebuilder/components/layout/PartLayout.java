package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;

import java.util.ArrayList;
import java.util.List;

public class PartLayout {
    private final Part part;
    private final List<StaffLayout> staffLayouts = new ArrayList<>();
    private double x, y;

    public PartLayout(Part part, double x, double y) {
        this.part = part;
        this.x = x;
        this.y = y;
    }

    public void add(StaffLayout staffLayout) {
        staffLayouts.add(staffLayout);
    }

    public List<StaffLayout> getStaffLayouts() {
        return staffLayouts;
    }

    public double getHeight() {
        double sum = staffLayouts.stream().mapToDouble(StaffLayout::getHeight).sum();
        double space = part.getStaffSpacing() * (staffLayouts.size() - 1);
        return sum + space;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}