package org.example.musicscorebuilder.components.layout;

public interface Selectable {
    boolean isSelected();
    void setSelected(boolean selected);
    boolean contains(double x, double y);
}