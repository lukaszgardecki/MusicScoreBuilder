package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class SystemLayout {
    private final List<PartLayout> parts = new ArrayList<>();
    private double partSpacing = 60;

    public void add(PartLayout part) {
        parts.add(part);
    }

    public List<PartLayout> getParts() {
        return parts;
    }

    public double getHeight() { return parts.stream().mapToDouble(PartLayout::getHeight).sum(); }
    public double getPartSpacing() { return partSpacing; }
}
