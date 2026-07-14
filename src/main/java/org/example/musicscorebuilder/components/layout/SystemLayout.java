package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.BraceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SystemLayout {
    private final PageLayout pageLayout;
    private final Optional<BraceLayout> braceLayout;
    private final List<MeasureLayout> measures = new ArrayList<>();
    private double spaceBelow;
    private double x, y;

    public SystemLayout(PageLayout parent, BraceType braceType) {
        this.pageLayout = parent;
        this.x = pageLayout.getMarginLeft();
        this.y = pageLayout.getMarginTop() + pageLayout.getOccupiedHeight();
        this.braceLayout = switch(braceType) {
            case NONE -> Optional.empty();
            case BRACE, BRACKET -> Optional.of(new BraceLayout(braceType, this));
        };
    }

    public void add(MeasureLayout measureLayout) {
        measures.add(measureLayout);
    }

    public PageLayout getPageLayout() { return pageLayout; }
    public Optional<BraceLayout> getBraceLayout() { return braceLayout; }
    public List<MeasureLayout> getMeasures() { return measures; }
    public double getHeight() {
        if (measures.isEmpty()) return 0.0;
        double totalPartsHeight = measures.stream()
                .mapToDouble(MeasureLayout::getHeight)
                .max().orElse(0.0);
        return totalPartsHeight + spaceBelow;
    }
    public double getWidth() { return measures.stream().mapToDouble(MeasureLayout::getWidth).sum() + getBraceWidth(); }
    public double getBraceWidth() { return braceLayout.map(BraceLayout::getWidth).orElse(0.0); }
    public double getSpaceBelow() { return spaceBelow; }
    public double getX() { return x; }
    public double getY() { return y; }
    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }
}
