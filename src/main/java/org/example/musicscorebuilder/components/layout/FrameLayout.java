package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class FrameLayout implements Selectable {
    private final ScoreStyle style;
    private final PageLayout parent;
    private final double x, y, width, height, marginTop, marginBottom;
    private final double contentY, contentHeight;
    private final double titleFontSize, subtitleFontSize, composerFontSize, numberNewFontSize, numberOldFontSize;
    private final double numBoxMinWidth, numBoxMinHeight, numBoxRadius, numBoxStrokeWidth, numBoxSpacing, numBoxPaddingX, numBoxPaddingY;
    private boolean selected;

    public FrameLayout(PageLayout parent, ScoreStyle style) {
        this.style = style;
        this.parent = parent;
        this.marginTop = style.getHeaderDefMarginTop();
        this.marginBottom = style.getHeaderDefMarginBottom();
        this.x = parent.getMarginLeft();
        this.y = parent.getMarginTop();
        this.contentY = y + marginTop;
        this.width = parent.getEffectiveWidth();
        this.contentHeight = style.toSp(20);
        this.height = contentHeight + marginTop + marginBottom;
        this.numberNewFontSize = style.getHeaderDefNumberNewFontSize();
        this.numberOldFontSize = style.getHeaderDefNumberOldFontSize();
        this.titleFontSize = style.getHeaderDefTitleFontSize();
        this.subtitleFontSize = style.getHeaderDefSubtitleFontSize();
        this.composerFontSize = style.getHeaderDefComposerFontSize();
        this.numBoxMinWidth = style.getHeaderDefNumBoxMinWidth();
        this.numBoxMinHeight = style.getHeaderDefNumBoxMinHeight();
        this.numBoxRadius = style.getHeaderDefNumBoxRadius();
        this.numBoxStrokeWidth = style.getHeaderDefNumBoxStrokeWidth();
        this.numBoxSpacing = style.getHeaderDefNumBoxSpacing();
        this.numBoxPaddingX = style.getHeaderDefNumBoxPaddingX();
        this.numBoxPaddingY = style.getHeaderDefNumBoxPaddingY();
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return 1; }
    @Override
    public boolean contains(double x, double y) {
        return x >= this.x
                && x <= this.x + this.width
                && y >= this.contentY
                && y <= this.contentY + this.contentHeight;
    }

    @Override
    public SegmentLayout getSegment() {
        return null;
    }

    @Override
    public StaffLayout getStaff() {
        return null;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getContentY() { return contentY; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public double getContentHeight() { return contentHeight; }
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
    public ScoreStyle getScoreStyle() { return style; }
}