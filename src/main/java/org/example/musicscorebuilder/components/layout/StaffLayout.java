package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

public class StaffLayout {
    private final MeasureLayout parent;
    private final Staff staff;
    private final ClefLayout clefLayout;
    private final double lineSpacing;
    private final double lineWidth;
    private double x = 0, y;

    public StaffLayout(Staff staff, MeasureLayout parent, ScoreStyle scoreStyle) {
        this.parent = parent;
        this.staff = staff;
        this.lineSpacing = scoreStyle.getStaffLineSpacing();
        this.lineWidth = scoreStyle.getStaffLineWidth();
        y = staff.getIndex() * (getHeight() + scoreStyle.getStaffSpacing());
        clefLayout = new ClefLayout(this, scoreStyle);
    }

    public Staff getStaff() { return staff; }
    public ClefLayout getClefLayout() { return clefLayout; }
    public int getLinesNumber() { return staff.getLinesNumber(); }
    public double getLineWidth() { return lineWidth; }
    public double getLineSpacing() { return lineSpacing; }
    public double getHeight() { return (staff.getLinesNumber() - 1) * lineSpacing; }
    public double getWidth() { return parent.getWidth(); }
    public double getX() { return x; }
    public double getY() { return y; }
}
