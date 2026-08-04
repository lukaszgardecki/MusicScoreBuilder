package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class HeaderLayout {
    private final ScoreStyle style;
    private final PageLayout parent;
    private final double x, y, width, height;
    private final double titleFontSize, subtitleFontSize, composerFontSize, numberNewFontSize, numberOldFontSize;
    private final double numBoxMinWidth, numBoxMinHeight, numBoxRadius, numBoxStrokeWidth, numBoxSpacing, numBoxPaddingX, numBoxPaddingY;

    public HeaderLayout(PageLayout parent, ScoreStyle style) {
        this.style = style;
        this.parent = parent;
        this.x = parent.getMarginLeft();
        this.y = parent.getMarginTop();
        this.width = parent.getEffectiveWidth();
        this.height = style.toSp(30);
        this.numberNewFontSize = style.getHeaderDefNumberNewFontSize();
        this.numberOldFontSize = style.getHeaderDefNumberOldFontSize();
        this.titleFontSize = style.getHeaderDefTitleFontSize();
        this.subtitleFontSize = style.getHeaderDefSubtitleFontSize();
        this.composerFontSize = style.getHeaderDefComposerFontSize();
        this.numBoxMinWidth = style.getHeaderDefMinWidth();
        this.numBoxMinHeight = style.getHeaderDefMinHeight();
        this.numBoxRadius = style.getHeaderDefNumBoxRadius();
        this.numBoxStrokeWidth = style.getHeaderDefNumBoxStrokeWidth();
        this.numBoxSpacing = style.getHeaderDefNumBoxSpacing();
        this.numBoxPaddingX = style.getHeaderDefNumBoxPaddingX();
        this.numBoxPaddingY = style.getHeaderDefNumBoxPaddingY();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public String getTitle() { return parent.getParent().getScore().getTitle(); }
    public String getSubtitle() { return parent.getParent().getScore().getSubtitle(); }
    public String getComposer() { return parent.getParent().getScore().getComposer(); }
    public String getNumberNew() { return parent.getParent().getScore().getNumberNew(); }
    public String getNumberOld() { return parent.getParent().getScore().getNumberOld(); }
    public double getTitleFontSize() { return titleFontSize; }
    public double getSubtitleFontSize() { return subtitleFontSize; }
    public double getComposerFontSize() { return composerFontSize; }
    public double getNumberNewFontSize() { return numberNewFontSize; }
    public double getNumberOldFontSize() { return numberOldFontSize; }
    public double getNumBoxMinWidth() { return numBoxMinWidth; }
    public double getNumBoxMinHeight() { return numBoxMinHeight; }
    public double getNumBoxRadius() { return numBoxRadius; }
    public double getNumBoxStrokeWidth() { return numBoxStrokeWidth; }
    public double getNumBoxSpacing() { return numBoxSpacing; }
    public double getNumBoxPaddingX() { return numBoxPaddingX; }
    public double getNumBoxPaddingY() { return numBoxPaddingY; }
}