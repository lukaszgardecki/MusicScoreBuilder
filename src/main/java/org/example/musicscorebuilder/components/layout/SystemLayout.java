package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;

import java.util.*;

public class SystemLayout {
    private final double DEFAULT_SPACE_BELOW = 10;

    private final List<PartLayout> parts = new ArrayList<>();
    private double spaceBelow = DEFAULT_SPACE_BELOW;
    private double x, y;

    public SystemLayout(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void add(PartLayout partLayout) {
        parts.add(partLayout);
    }

    public List<PartLayout> getParts() { return parts; }
    public double getHeight() {
        if (parts.isEmpty()) return 0.0;
        double totalPartsHeight = parts.stream()
                .mapToDouble(PartLayout::getHeight)
                .sum();
        return totalPartsHeight + spaceBelow;
    }
    public double getWidth() { return parts.stream().mapToDouble(PartLayout::getWidth).max().orElse(0); }
    public double getBraceWidth() { return parts.stream().mapToDouble(PartLayout::getBraceWidth).max().orElse(0); }
    public double getSpaceBelow() { return spaceBelow; }
    public double getX() { return x; }
    public double getY() { return y; }
    public Optional<PartLayout> findPartLayout(Part part) {
        return parts.stream()
                .filter(pl -> pl.getPart() == part)
                .findFirst();
    }
    public void setDefaultSpaceBelow() { spaceBelow = DEFAULT_SPACE_BELOW; }
    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }
}
