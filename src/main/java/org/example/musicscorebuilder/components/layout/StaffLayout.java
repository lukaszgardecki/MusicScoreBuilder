package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Staff;

public class StaffLayout {
    private final MeasureLayout parent;
    private final Staff staff;
    private final ClefLayout clefLayout;
    private final KeySigLayout keySigLayout;
    double lineSpacing = 1;
    private double lineWidth = lineSpacing * 0.08;
    private double x = 0, y;

    public StaffLayout(Staff staff, MeasureLayout parent) {
        this.staff = staff;
        this.parent = parent;
        y = staff.getIndex() * (getHeight() + parent.getStaffSpacing());
        clefLayout = new ClefLayout(this);
        keySigLayout = new KeySigLayout(this);
    }

    public Staff getStaff() { return staff; }
    public ClefLayout getClefLayout() { return clefLayout; }
    public KeySigLayout getKeySigLayout() { return keySigLayout; }
    public int getLinesNumber() { return staff.getLinesNumber(); }
    public double getLineWidth() { return lineWidth; }
    public double getLineSpacing() { return lineSpacing; }
    public double getHeight() { return (staff.getLinesNumber() - 1) * lineSpacing; }
    public double getWidth() { return parent.getWidth(); }
    public double getX() { return x; }
    public double getY() { return y; }
}
