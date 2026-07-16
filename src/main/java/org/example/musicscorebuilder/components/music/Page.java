package org.example.musicscorebuilder.components.music;

public class Page {
    private final PageFormat format;
    private final double marginTop;
    private final double marginBottom;
    private final double marginLeft;
    private final double marginRight;

    public Page(PageFormat format) {
        this.format = format;
        this.marginTop = 25.0;
        this.marginBottom = 25.0;
        this.marginLeft = 25.0;
        this.marginRight = 25.0;
    }
    public Page(PageFormat format, double marginTop, double marginBottom, double marginLeft, double marginRight) {
        this.format = format;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    public double getWidthMm() { return format.getWidthMm(); }
    public double getHeightMm() { return format.getHeightMm(); }
    public double getEffectiveWidthMm() { return format.getWidthMm() - marginLeft - marginRight; }
    public double getEffectiveHeightMm() { return format.getHeightMm() - marginTop - marginBottom; }
    public double getMarginTopMm() { return marginTop; }
    public double getMarginBottomMm() { return marginBottom; }
    public double getMarginLeftMm() { return marginLeft; }
    public double getMarginRightMm() { return marginRight; }
}
