package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class FooterLayout {
    private final ScoreStyle style;
    private final PageLayout parent;
    private final double x, y, width, height;
    private final int pageNum;
    private boolean isVisible;

    public FooterLayout(PageLayout parent, ScoreStyle style) {
        this.style = style;
        this.parent = parent;
        this.height = parent.getHeight() - parent.getMarginTop() - parent.getEffectiveHeight();
        this.x = parent.getMarginLeft();
        this.y = parent.getHeight() - parent.getMarginBottom();
        this.width = parent.getEffectiveWidth();
        this.pageNum = parent.getNumber();
        this.isVisible = false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public int getPageNum() { return pageNum; }
    public boolean isVisible() { return isVisible; }
    public double getPageNumFontSize() { return style.getFooterDefPageNumFontSize(); }
}