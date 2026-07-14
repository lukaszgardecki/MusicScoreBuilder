package org.example.musicscorebuilder.components.layout;

public class ElementLayout {
    private double width = 2.0;
    protected boolean hasDynamicWidth;

    public ElementLayout() {
        hasDynamicWidth = true;
    }

    public ElementLayout(boolean hasDynamicWidth) {
        this.hasDynamicWidth = hasDynamicWidth;
    }

    public double getWidth() { return width; }
    public boolean hasDynamicWidth() { return hasDynamicWidth; }
}
