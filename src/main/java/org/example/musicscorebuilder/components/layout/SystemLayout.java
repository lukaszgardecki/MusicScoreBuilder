package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Part;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SystemLayout {
    private BarlineLayout startBarline = new BarlineLayout();
    private final List<PartLayout> parts = new ArrayList<>();
    private double partSpacing = 60;

    public BarlineLayout getStartBarline() { return startBarline; }
    public List<PartLayout> getParts() { return parts; }
    public double getHeight() { return parts.stream().mapToDouble(PartLayout::getHeight).sum() + partSpacing * (parts.size() - 1); }
    public double getOccupiedWidth() {
        return parts.stream().mapToDouble(PartLayout::getWidth).max().orElse(0) + getBraceWidth();
    }
    public double getPartSpacing() { return partSpacing; }
    public double getBraceWidth() { return parts.stream().mapToDouble(PartLayout::getBraceWidth).max().orElse(0); }

    public PartLayout getOrCreatePart(Part part) {
        for (PartLayout pl : this.getParts()) {
            if (pl.getPart() == part) return pl;
        }
        PartLayout newPl = new PartLayout(part);
        parts.add(newPl);
        startBarline.addHeight(newPl.getHeight());
        return newPl;
    }
    public List<MeasureLayout> getMeasures() {
        if (parts.isEmpty() || parts.get(0).getStaffLayouts().isEmpty()) {
            return Collections.emptyList();
        }
        return parts.get(0).getStaffLayouts().get(0).getMeasures();
    }
}
