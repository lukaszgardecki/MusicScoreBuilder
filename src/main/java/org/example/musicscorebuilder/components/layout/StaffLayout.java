package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

public class StaffLayout {
    private final SMeasureLayout parent;
    private final Staff staff;
    double lineSpacing = 1;
    private double lineWidth = lineSpacing * 0.08;

    public StaffLayout(Staff staff, SMeasureLayout parent) {
        this.staff = staff;
        this.parent = parent;
    }

    public Staff getStaff() { return staff; }
    public double getHeight() { return (staff.getLinesNumber() - 1) * lineSpacing; }
    public double getWidth() { return parent.getWidth(); }
    public int getLinesNumber() { return staff.getLinesNumber(); }
    public double getLineWidth() { return lineWidth; }
    public double getLineSpacing() { return lineSpacing; }
}
