package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;

import java.util.ArrayList;
import java.util.List;

public class SystemLayout {
    private final List<PartLayout> parts = new ArrayList<>();
    private double partSpacing = 60;

    public List<PartLayout> getParts() {
        return parts;
    }

    public double getHeight() { return parts.stream().mapToDouble(PartLayout::getHeight).sum() + partSpacing * (parts.size() - 1); }
    public double getOccupiedWidth() { return parts.stream().mapToDouble(PartLayout::getWidth).max().orElse(0); }
    public double getPartSpacing() { return partSpacing; }

    public PartLayout getOrCreatePart(Part part) {
        for (PartLayout pl : this.getParts()) {
            if (pl.getPart() == part) return pl;
        }
        PartLayout newPl = new PartLayout(part);
        parts.add(newPl);
        return newPl;
    }
}
