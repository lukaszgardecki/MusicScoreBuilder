package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;

import java.util.ArrayList;
import java.util.List;

public class PartLayout {
    private final Part part;
    private final BraceLayout braceLayout;
    private double spaceBelow;
    private double x, y;
    private final List<PMeasureLayout> partMeasures = new ArrayList<>();

    public PartLayout(Part part, double x, double y) {
        this.part = part;
        this.x = x;
        this.y = y;
        this.braceLayout = new BraceLayout(part.getBraceType(), this);
    }

    public void add(PMeasureLayout pMeasureLayout) { partMeasures.add(pMeasureLayout); }
    public void removeLastPMeasureLayout() {
        if (partMeasures.isEmpty()) return;
        partMeasures.removeLast();
    }

    public Part getPart() { return part; }
    public BraceLayout getBraceLayout() { return braceLayout; }
    public List<PMeasureLayout> getPartMeasures() { return partMeasures; }

    public double getHeight() {
        return partMeasures.stream().mapToDouble(PMeasureLayout::getHeight).max().orElse(0) + spaceBelow;
    }

    public double getWidth() {
        double measures = partMeasures.stream().mapToDouble(PMeasureLayout::getWidth).sum();
        double braceWidth = braceLayout.getWidth();
        return measures + braceWidth;
    }
    public double getSpaceBelow() { return spaceBelow; }
    public double getBraceWidth() { return braceLayout.getWidth(); }
    public double getX() { return x; }
    public double getY() { return y; }

    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }
}