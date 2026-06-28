package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class SystemLayout {
    private final List<PartLayout> parts = new ArrayList<>();
    private double height = 0;
    private double y = 0;
    private double spacing = 0;

    public void add(PartLayout part) {
        height += part.getHeight();
        parts.add(part);
    }

    public List<PartLayout> getParts() {
        return parts;
    }

    public double getY() { return y; }
    public double getHeight() { return height; }
    public double getSpacing() { return spacing; }
}
