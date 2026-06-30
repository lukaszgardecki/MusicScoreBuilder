package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Page;

import java.util.ArrayList;
import java.util.List;

public class PageLayout {
    private Page page;
    private final List<SystemLayout> systems = new ArrayList<>();
    private final double height;
    private final double width;
    private final double marginTop;
    private final double marginBottom;
    private final double marginLeft;
    private final double marginRight;
    private final double effectiveHeight;
    private final double effectiveWidth;
    private final double systemSpacing = 70;

    public PageLayout(Page page) {
        this.page = page;
        this.height = page.getHeight();
        this.width = page.getWidth();
        this.effectiveHeight = page.getEffectiveHeight();
        this.effectiveWidth = page.getEffectiveWidth();
        this.marginTop = page.getMarginTop();
        this.marginBottom = page.getMarginBottom();
        this.marginLeft = page.getMarginLeft();
        this.marginRight = page.getMarginRight();
    }

    public void addSystem(SystemLayout system) {
        systems.add(system);
    }

    public List<SystemLayout> getSystems() { return systems; }
    public double getHeight() { return height; }
    public double getEffectiveHeight() { return effectiveHeight; }
    public double getWidth() { return width; }
    public double getEffectiveWidth() { return effectiveWidth; }
    public double getOccupiedHeight() {
        return systems.stream()
                .mapToDouble(SystemLayout::getHeight)
                .sum() + (systemSpacing * (systems.size()-1));
    }
    public double getMarginTop() { return marginTop; }
    public double getMarginBottom() { return marginBottom; }
    public double getMarginLeft() { return marginLeft; }
    public double getMarginRight() { return marginRight; }
    public double getSystemSpacing() { return systemSpacing; }
}
