package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class SegmentLayout {
    private final ScoreStyle style;
    private final MeasureLayout parent;
    private final List<ElementLayout> elements = new ArrayList<>();
    private double x, y;
    private double width, height;

    public SegmentLayout(MeasureLayout parent) {
        this.style = parent.getScoreStyle();
        this.parent = parent;
        this.y = 0;
        this.width = 0;
        this.height = parent.getHeight();
    }

    public ElementLayout findClickedElement(double measureMusicX, double measureMusicY) {
        double segmentMusicX = measureMusicX - this.getX();
        double segmentMusicY = measureMusicY - this.getY();

        for (ElementLayout element : elements) {
            if (element instanceof EmptyElement) continue;
            if (element.contains(segmentMusicX, segmentMusicY)) return element;
        }
        return null;
    }

    public void add(ElementLayout elementLayout) {
        elements.add(elementLayout);
        double currentMargin = getLeftMargin();
        for (ElementLayout el : elements) el.setX(currentMargin);

        double maxElementWidth = 0.0;
        for (ElementLayout el : elements) {
            if (el.getWidth() > maxElementWidth) {
                maxElementWidth = el.getWidth();
            }
        }

        this.width = currentMargin + maxElementWidth;
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

    public void setWidth(double width) { this.width = Math.max(width, getLeftMargin()); }

    private double getLeftMargin() {
        if (elements.isEmpty()) return 0.0;

        boolean hasVisibleElements = false;
        for (ElementLayout element : elements) {
            if (element instanceof BarlineLayout) return 0.0;
            if (element.getWidth() > 0.0) hasVisibleElements = true;
        }
        return hasVisibleElements ? (style.getSegmentLeftMargin() * style.getStaffLineSpacing()) : 0.0;
    }
}
