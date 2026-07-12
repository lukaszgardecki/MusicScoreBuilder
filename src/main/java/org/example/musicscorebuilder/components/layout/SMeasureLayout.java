package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.BarlineStyle;
import org.example.musicscorebuilder.components.music.Staff;

import java.util.ArrayList;
import java.util.List;

public class SMeasureLayout {
    private PMeasureLayout parent;
    private boolean isVisible = true;
    private StaffLayout staffLayout;
    private BarlineLayout startBarline = null;
    private BarlineLayout endBarline;
    private double x, y;
    private double spaceBelow;
    private List<SegmentLayout> segments = new ArrayList<>();

    public SMeasureLayout(PMeasureLayout parent, Staff staff, double x, double y) {
        this.parent = parent;
        this.staffLayout = new StaffLayout(staff, this);
        this.endBarline = new BarlineLayout(this, BarlineLayout.Type.END, parent.getMeasure().getBarlineStyle());
        this.x = x;
        this.y = y;
    }

    public void add(SegmentType type) {
        this.segments.add(switch(type) {
            case CLEF -> new ClefLayout(staffLayout, getNextSegmentX());
            case KEY_SIG, TIME_SIG, CHORD_REST, BARLINE -> new SegmentLayout(type, getNextSegmentX(), staffLayout.getHeight());
        });
    }

    public double getWidth() { return getActiveSegments().stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public double getHeight() { return staffLayout.getHeight() + spaceBelow; }
    public double getX() { return x; }
    public double getY() { return y; }
    public StaffLayout getStaffLayout() { return staffLayout; }
    public BarlineLayout getStartBarline() { return startBarline; }
    public BarlineLayout getEndBarline() { return endBarline; }
    public List<SegmentLayout> getSegments() { return segments; }
    public List<SegmentLayout> getActiveSegments() { return segments.stream().filter(SegmentLayout::isGenerated).toList(); }

    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }
    public void setSystemStartBarline(double spaceBelowPart) {
        this.startBarline = new BarlineLayout(this, BarlineLayout.Type.START, BarlineStyle.SINGLE);
        this.startBarline.setHeightToPartBelow(spaceBelowPart);
    }
    private double getNextSegmentX() {return this.getActiveSegments().stream().mapToDouble(SegmentLayout::getWidth).sum();}
}
