package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.BarlineStyle;
import org.example.musicscorebuilder.components.music.Staff;

import java.util.ArrayList;
import java.util.List;

public class SMeasureLayout {
    private final double DEFAULT_SPACE_BELOW = 5;

    private PMeasureLayout parent;
    private boolean isVisible = true;
    private StaffLayout staffLayout;
    private BarlineLayout startBarline = null;
    private BarlineLayout endBarline;
    private double x, y;
    private double spaceBelow = DEFAULT_SPACE_BELOW;
    private List<SegmentLayout> segments = new ArrayList<>();

    public SMeasureLayout(PMeasureLayout parent, Staff staff, double x, double y) {
        this.parent = parent;
        this.staffLayout = new StaffLayout(staff, this);
        this.endBarline = new BarlineLayout(this, BarlineLayout.Type.END, parent.getMeasure().getBarlineStyle());
        this.x = x;
        this.y = y;
    }

    public void add(SegmentType type, double height) {
        double currentTailX = 0.0;
        for (SegmentLayout segment : this.segments) {
            currentTailX += segment.getWidth();
        }
        var newSegment = new SegmentLayout(type, currentTailX, 0, height);
        this.segments.add(newSegment);
    }

    public double getWidth() { return getActiveSegments().stream().mapToDouble(SegmentLayout::getWidth).sum(); }
    public double getHeight() { return staffLayout.getHeight() + spaceBelow; }
    public double getMinWidth() { return segments.stream().mapToDouble(SegmentLayout::getMinWidth).sum(); }
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
}
