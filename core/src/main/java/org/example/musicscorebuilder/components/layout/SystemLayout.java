package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.BraceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SystemLayout implements PageBlockLayout {
    private final PageLayout pageLayout;
    private final Optional<BraceLayout> braceLayout;
    private final List<MeasureLayout> measures = new ArrayList<>();
    private final List<TieLayout> ties = new ArrayList<>();
    private final List<SlurLayout> slurs = new ArrayList<>();
    private double spaceBelow;
    private double x, y;

    public SystemLayout(PageLayout parent, BraceType braceType) {
        this.pageLayout = parent;
        this.x = pageLayout.getMarginLeft();
        this.y = pageLayout.getEffectiveY() + pageLayout.getOccupiedHeight();
        this.braceLayout = switch(braceType) {
            case NONE -> Optional.empty();
            case BRACE, BRACKET -> Optional.of(new BraceLayout(braceType, this));
        };
    }

    @Override
    public double getHeight() {
        if (measures.isEmpty()) return 0.0;
        double totalPartsHeight = measures.stream()
                .mapToDouble(MeasureLayout::getHeight)
                .max().orElse(0.0);
        return totalPartsHeight + spaceBelow;
    }
    @Override public double getWidth() { return measures.stream().mapToDouble(MeasureLayout::getWidth).sum() + getBraceWidth(); }
    @Override public double getX() { return x; }
    @Override public double getY() { return y; }
    @Override public void setY(double y) { this.y = y; }

    public void add(MeasureLayout measureLayout) {
        measures.add(measureLayout);
        measureLayout.setParent(this);
    }
    public void addTie(TieLayout tie) { ties.add(tie); }
    public void addSlur(SlurLayout slur) { slurs.add(slur); }

    public PageLayout getPageLayout() { return pageLayout; }
    public Optional<BraceLayout> getBraceLayout() { return braceLayout; }
    public List<MeasureLayout> getMeasures() { return measures; }
    public double getBraceWidth() { return braceLayout.map(BraceLayout::getWidth).orElse(0.0); }
    public double getSpaceBelow() { return spaceBelow; }
    public List<TieLayout> getTies() { return ties; }
    public List<SlurLayout> getSlurs() { return slurs; }

    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }
}