package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

public class StaffLayout {
    private final Staff staff;
    private final double x, y, width;

    public StaffLayout(Staff staff, double x, double y, double width) {
        this.staff = staff;
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public Staff getStaff() {
        return staff;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }
}
