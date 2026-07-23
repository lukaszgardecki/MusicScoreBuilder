package org.example.musicscorebuilder.components.layout;

public interface Selectable {
    boolean isSelected();
    void setSelected(boolean selected);
    int getVoice();
    boolean contains(double x, double y);
    SegmentLayout getParentSegment();
}