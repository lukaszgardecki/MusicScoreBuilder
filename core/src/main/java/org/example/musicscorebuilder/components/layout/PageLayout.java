package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PageLayout {
    private ScoreLayout parent;
    private final FooterLayout footer;
    private final List<PageBlockLayout> blocks = new ArrayList<>();
    private final double height;
    private final double width;
    private final double effectiveHeight;
    private final double effectiveWidth;
    private final double marginTop;
    private final double marginBottom;
    private final double marginLeft;
    private final double marginRight;
    private double x, y = 0;
    private final int number;

    public PageLayout(ScoreLayout parent, int pageIndex) {
        this.parent = parent;
        var style = parent.getStyle();
        var page = parent.getScore().getPage();
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
        this.footer = new FooterLayout(this, style);
    }

    public void addBlock(PageBlockLayout block) {
        double currentY = getEffectiveY() + getOccupiedHeight();
        block.setY(currentY);
        blocks.add(block);
    }

    public ScoreLayout getParent() { return parent; }
    public FooterLayout getFooter() { return footer; }
    public List<PageBlockLayout> getBlocks() { return blocks; }
    public double getHeight() { return height; }
    public double getWidth() { return width; }
    public double getEffectiveY() { return marginTop; }
    public double getEffectiveWidth() { return effectiveWidth; }
    public double getEffectiveHeight() { return effectiveHeight; }
    public double getMarginTop() { return marginTop; }
    public double getMarginBottom() { return marginBottom; }
    public double getMarginLeft() { return marginLeft; }
    public double getMarginRight() { return marginRight; }
    public double getRemainingWidth() { return effectiveWidth - getOccupiedWidth(); }
    public double getRemainingHeight() {
        return effectiveHeight - footer.getHeight() - getOccupiedHeight();
    }

    public double getOccupiedWidth() {
        double max = 0.0;
        for (PageBlockLayout block : blocks) {
            double w = block.getWidth();
            if (w > max) max = w;
        }
        return max;
    }

    public double getOccupiedHeight() {
        double sum = 0.0;
        for (PageBlockLayout block : blocks) {
            sum += block.getHeight();
        }
        return sum;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public int getNumber() { return number; }
    public List<SystemLayout> getSystems() {
        return blocks.stream()
                .filter(SystemLayout.class::isInstance)
                .map(SystemLayout.class::cast)
                .collect(Collectors.toList());
    }

    public List<FrameLayout> getFrames() {
        return blocks.stream()
                .filter(FrameLayout.class::isInstance)
                .map(FrameLayout.class::cast)
                .collect(Collectors.toList());
    }

    public void setLastSystemSpaceBelow(double spaceBelow) {
        List<SystemLayout> systems = getSystems();
        if (systems.isEmpty()) return;
        systems.get(systems.size() - 1).setSpaceBelow(spaceBelow);
    }
}