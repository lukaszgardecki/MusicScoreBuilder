package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Page;

import java.util.ArrayList;
import java.util.List;

public class PageLayout {
    private Page page;
    private final List<SystemLayout> systems = new ArrayList<>();
    private final double height;
    private final double width;
    private final double effectiveHeight;
    private final double effectiveWidth;
    private double x;

    public PageLayout(double width, double height, double x, double effectiveWidth, double effectiveHeight) {
        this.width = width;
        this.height = height;
        this.effectiveWidth = effectiveWidth;
        this.effectiveHeight = effectiveHeight;
        this.x = x;
    }

    public void add(SystemLayout system) {
        systems.add(system);
    }

    public List<SystemLayout> getSystems() { return systems; }
    public double getHeight() { return height; }
    public double getWidth() { return width; }
    public double getEffectiveWidth() { return effectiveWidth; }
    public double getEffectiveHeight() { return effectiveHeight; }
    public double getRemainingWidth() { return  effectiveWidth - getOccupiedWidth(); }
    public double getRemainingHeight() { return effectiveHeight - getOccupiedHeight(); }
    public double getOccupiedWidth() {
        return systems.stream().mapToDouble(SystemLayout::getWidth).max().orElse(0.0);
    }
    public double getOccupiedHeight() {
        if (systems.isEmpty()) return 0.0;
        return systems.stream()
                .mapToDouble(SystemLayout::getHeight)
                .sum();
    }
    public double getX() { return x; }
}
