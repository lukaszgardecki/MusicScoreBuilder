package org.example.musicscorebuilder.components.layout.edit;

import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.managers.ModeManager;

public class CursorLayout {
    private final SegmentLayout parent;
    private final StaffLayout staff;
    private final String color;
    private final Selectable element;
    private final double y, height, thickness, width;

    public CursorLayout(Selectable element) {
        this.element = element;
        this.parent = element.getSegment();
        this.staff = element.getStaff();
        this.element.setSelected(true);

        ScoreStyle style = parent.getScoreStyle();
        int activeVoice = ModeManager.getInstance().getCurrentVoice();
        this.color = style.getEditInsertColor(activeVoice);
        this.height = staff.getHeight() + 2 * style.getEditCursorPadding();
        this.width = style.getEditCursorBoxWidth();
        this.thickness = style.getEditCursorLineThickness();
        this.y = staff.getY() - style.getEditCursorPadding();
    }

    public double getX() { return parent.getX() + parent.getMarginLeft() - getThickness(); }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getThickness() { return thickness; }
    public String getColor() { return color; }
    public Selectable getElement() { return element; }
    public SegmentLayout getSegment() { return parent; }
    public StaffLayout getStaff() { return staff; }
}