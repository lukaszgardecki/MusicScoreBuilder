package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Frame;

public class FrameLayout implements Selectable, PageBlockLayout {
    private final Frame frameData;
    private final ScoreStyle style;
    private final PageLayout parent;
    private final double marginTop, marginBottom;
    private double x, y;
    private double width, height;
    private double contentY;
    private double contentHeight;
    private final double titleFontSize, subtitleFontSize, composerFontSize, numberNewFontSize, numberOldFontSize;
    private final double numBoxMinWidth, numBoxMinHeight, numBoxRadius, numBoxStrokeWidth, numBoxSpacing, numBoxPaddingX, numBoxPaddingY;
    private boolean selected;

    public FrameLayout(PageLayout parent, ScoreStyle style, Frame frameData) {
        this.parent = parent;
        this.style = style;
        this.frameData = frameData;

        this.marginTop = style.getHeaderDefMarginTop();
        this.marginBottom = style.getHeaderDefMarginBottom();
        this.x = parent.getMarginLeft();
        this.y = parent.getMarginTop() + parent.getOccupiedHeight();
        this.contentY = y + marginTop;

        this.width = frameData.getWidth() > 0 ? frameData.getWidth() : parent.getEffectiveWidth();

        double defaultHeight = style.toSp(20) + marginTop + marginBottom;
        this.height = frameData.getHeight() > 0 ? frameData.getHeight() : defaultHeight;
        this.contentHeight = Math.max(0, this.height - marginTop - marginBottom);

        if (frameData.getWidth() <= 0) {
            frameData.setWidth(this.width);
        }
        if (frameData.getHeight() <= 0) {
            frameData.setHeight(this.height);
        }

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

    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return height; }
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

    @Override public SegmentLayout getSegment() { return null; }
    @Override public StaffLayout getStaff() { return null; }

    @Override public double getX() { return x; }
    @Override public double getY() { return y; }

    @Override
    public void setY(double y) {
        this.y = y;
        this.contentY = y + marginTop;
    }

    public double getContentY() { return contentY; }
    public double getContentHeight() { return contentHeight; }
    public String getTitle() { return frameData.getTitle() != null ? frameData.getTitle() : ""; }
    public String getSubtitle() { return frameData.getSubtitle() != null ? frameData.getSubtitle() : ""; }
    public String getComposer() { return frameData.getComposer() != null ? frameData.getComposer() : ""; }
    public String getNumberNew() { return frameData.getNumberNew() != null ? frameData.getNumberNew() : ""; }
    public String getNumberOld() { return frameData.getNumberOld() != null ? frameData.getNumberOld() : ""; }
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
    public Frame getFrameData() { return frameData; }

    public void setWidth(double width) {
        this.width = width;
        this.frameData.setWidth(width);
    }

    public void setHeight(double height) {
        this.height = height;
        this.contentHeight = Math.max(0, height - marginTop - marginBottom);
        this.frameData.setHeight(height);
    }
}