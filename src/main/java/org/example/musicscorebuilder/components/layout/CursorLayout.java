package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class CursorLayout {
    private final ScoreStyle style;
    private final SegmentLayout parent;
    private final StaffLayout staff;
    private final String color;

    public CursorLayout(Selectable element) {
        this.parent = element.getSegment();
        this.staff = element.getStaff();
        this.style = parent.getScoreStyle();
        this.color = style.getSelectColor(element);
    }

    public double getX() { return parent.getX() - getThickness(); }
    public double getY() { return staff == null ? 0 : staff.getY() - style.getEditCursorAboveBelowLength(); }
    public double getWidth() { return parent.getWidth(); }
    public double getHeight() {
        var dY = style.getEditCursorAboveBelowLength();
        return staff == null ? 0 : staff.getHeight() + 2*dY;
    }
    public double getThickness() { return style.getEditCursorWidth(); }
    public String getColor() { return color; }
    public SegmentLayout getSegment() { return parent; }
    public StaffLayout getStaff() { return staff; }
}