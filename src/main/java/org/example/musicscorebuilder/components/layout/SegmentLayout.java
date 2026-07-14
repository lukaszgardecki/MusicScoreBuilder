package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class SegmentLayout {
    private final MeasureLayout parent;
    private final List<ElementLayout> elements = new ArrayList<>();
    private double x, y;
    private double width, height;

    public SegmentLayout(MeasureLayout parent) {
        this.parent = parent;
        this.y = 0;
        this.width = 0;
        this.height = parent.getHeight();
    }

    public void add(ElementLayout elementLayout) {
        if (elementLayout.getWidth() > width) { width = elementLayout.getWidth(); }
        elements.add(elementLayout);
    }

    public List<ElementLayout> getElements() { return elements; }
    public double getX() {
        var segments = parent.getSegments();
        int i = segments.indexOf(this);
        if (i == 0) return 0;
        SegmentLayout prevSeg = segments.get(i - 1);
        return prevSeg.getX() + prevSeg.getWidth();
    }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public boolean hasDynamicWidth() { return elements.stream().anyMatch(ElementLayout::hasDynamicWidth); }

    public void setWidth(double width) { this.width = width; }
}
