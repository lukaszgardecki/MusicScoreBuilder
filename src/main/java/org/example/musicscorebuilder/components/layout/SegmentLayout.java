package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.ArrayList;
import java.util.List;

public class SegmentLayout {
    private final ScoreStyle style;
    private final MeasureLayout parent;
    private final SegmentType type;
    private final List<ElementLayout> elements = new ArrayList<>();
    private double y = 0;
    private double  height;

    public SegmentLayout(SegmentType type, MeasureLayout parent) {
        this.style = parent.getScoreStyle();
        this.parent = parent;
        this.type = type;
        this.height = parent.getHeight();
    }

    public void add(ElementLayout elementLayout) {
        elementLayout.setX(style.getSegmentLeftMargin());
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
    public double getWidth() {
        if (elements.isEmpty()) return 0;
        if (type == SegmentType.START_BARLINE) return 0;
        return elements.stream().mapToDouble(ElementLayout::getWidth).max().orElse(0) + style.getSegmentLeftMargin();
    }
    public double getHeight() { return height; }
    public boolean hasDynamicWidth() { return elements.stream().anyMatch(ElementLayout::hasDynamicWidth); }
    public ScoreStyle getScoreStyle() { return style; }
}
