package org.example.musicscorebuilder.components.frames;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.frames.Frame;

public abstract class FrameLayout implements Selectable, PageBlockLayout {
    protected final Frame frameData;
    protected final ScoreStyle style;
    protected final PageLayout parent;
    protected double marginTop, marginBottom;
    protected double x, y;
    protected double width, height;
    protected double contentY;
    protected double contentHeight;
    protected boolean selected;

    public FrameLayout(PageLayout parent, ScoreStyle style, Frame frameData) {
        this.parent = parent;
        this.style = style;
        this.frameData = frameData;

        this.marginTop = frameData.getMarginTop() != null ? frameData.getMarginTop() : style.getFrameDefMarginTop();
        this.marginBottom = frameData.getMarginBottom() != null ? frameData.getMarginBottom() : style.getFrameDefMarginBottom();
        this.width = frameData.getWidth() != null ? frameData.getWidth() : parent.getEffectiveWidth();
        this.contentHeight = frameData.getHeight() != null ? frameData.getHeight() : style.getFrameDefHeight();

        this.x = parent.getMarginLeft();
        this.y = parent.getMarginTop() + parent.getOccupiedHeight();
        this.contentY = y + marginTop;
        this.height = this.contentHeight + this.marginTop + this.marginBottom;
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
    public double getMarginTop() { return marginTop; }
    public double getMarginBottom() { return marginBottom; }
    public ScoreStyle getScoreStyle() { return style; }
    public Frame getFrameData() { return frameData; }
    public PageLayout getParent() { return parent; }

    public void setWidth(double width) {
        this.width = width;
        this.frameData.setWidth(width);
    }

    public void setHeight(double height) {
        this.height = height;
        this.contentHeight = Math.max(0, height - marginTop - marginBottom);
        this.frameData.setHeight(contentHeight);
    }

    public void setContentHeight(double newContentHeight) {
        this.contentHeight = Math.max(0, newContentHeight);
        this.height = this.contentHeight + this.marginTop + this.marginBottom;
        this.frameData.setHeight(this.contentHeight);
    }

    public void setMarginTop(double marginTop) {
        this.marginTop = Math.max(0, marginTop);
        this.contentY = this.y + this.marginTop;
        this.height = this.contentHeight + this.marginTop + this.marginBottom;
        this.frameData.setMarginTop(this.marginTop);
    }

    public void setMarginBottom(double marginBottom) {
        this.marginBottom = Math.max(0, marginBottom);
        this.height = this.contentHeight + this.marginTop + this.marginBottom;
        this.frameData.setMarginBottom(this.marginBottom);
    }
}