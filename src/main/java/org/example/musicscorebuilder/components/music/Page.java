package org.example.musicscorebuilder.components.music;

public class Page {
    private final PageFormat format;
    private final double marginTop = 25.0;
    private final double marginBottom = 25.0;
    private final double marginLeft = 25.0;
    private final double marginRight = 25.0;

    public Page(PageFormat format) {
        this.format = format;
    }

    public double getWidthMm() { return format.getWidthMm(); }
    public double getHeightMm() { return format.getHeightMm(); }
    public double getEffectiveWidth() { return format.getWidthMm() - marginLeft - marginRight; }
    public double getEffectiveHeight() { return format.getHeightMm() - marginTop - marginBottom; }
    public double getMarginTopMm() { return marginTop; }
    public double getMarginBottomMm() { return marginBottom; }
    public double getMarginLeftMm() { return marginLeft; }
    public double getMarginRightMm() { return marginRight; }
}
