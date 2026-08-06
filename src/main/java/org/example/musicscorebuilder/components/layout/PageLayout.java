package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Page;

import java.util.ArrayList;
import java.util.List;

public class PageLayout {
    private ScoreLayout parent;
    private final FrameLayout header;
    private final FooterLayout footer;
    private final List<SystemLayout> systems = new ArrayList<>();
    private final double height;
    private final double width;
    private final double effectiveHeight;
    private final double effectiveWidth;
    private final double marginTop ;
    private final double marginBottom;
    private final double marginLeft;
    private final double marginRight;
    private double x;
    private final int number;

    public PageLayout(Page page, ScoreLayout parent, int pageIndex) {
        this.parent = parent;
        var style = parent.getStyle();
        this.width = style.toSp(page.getWidthMm());
        this.height = style.toSp(page.getHeightMm());
        this.effectiveWidth = style.toSp(page.getEffectiveWidthMm());
        this.effectiveHeight = style.toSp(page.getEffectiveHeightMm());
        this.marginTop = style.toSp(page.getMarginTopMm());
        this.marginBottom = style.toSp(page.getMarginBottomMm());
        this.marginLeft = style.toSp(page.getMarginLeftMm());
        this.marginRight = style.toSp(page.getMarginRightMm());
        this.x = (width + style.getPageSpacing()) * pageIndex;
        this.number = pageIndex + 1;
        this.header = number == 1 ? new FrameLayout(this, style) : null;
        this.footer = new FooterLayout(this, style);
    }

    public void add(SystemLayout system) {
        systems.add(system);
    }

    public ScoreLayout getParent() { return parent; }
    public List<SystemLayout> getSystems() { return systems; }
    public double getHeight() { return height; }
    public double getWidth() { return width; }
    public double getEffectiveY() {
        var headerHeight = header != null ? header.getHeight() : 0;
        return marginTop + headerHeight;
    }
    public double getEffectiveWidth() { return effectiveWidth; }
    public double getEffectiveHeight() { return effectiveHeight; }
    public double getMarginTop() { return marginTop; }
    public double getMarginBottom() { return marginBottom; }
    public double getMarginLeft() { return marginLeft; }
    public double getMarginRight() { return marginRight; }
    public double getRemainingWidth() { return  effectiveWidth - getOccupiedWidth(); }
    public double getRemainingHeight() {
        var headerHeight = header != null ? header.getHeight() : 0;
        return effectiveHeight - headerHeight - footer.getHeight() - getOccupiedHeight(); }
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
    public int getNumber() { return number; }
    public FrameLayout getHeader() { return header; }
    public FooterLayout getFooter() { return footer; }

    public void setLastSystemSpaceBelow(double spaceBelow) {
        if (systems.isEmpty()) return;
        systems.getLast().setSpaceBelow(spaceBelow);
    }
}
