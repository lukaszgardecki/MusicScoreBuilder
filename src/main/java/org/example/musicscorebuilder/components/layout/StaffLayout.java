package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

public class StaffLayout {
    private final Staff staff;
    private final double width;

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

    public double getHeight() {
        return staff.getHeight();
    }
}
