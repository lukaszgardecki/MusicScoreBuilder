package org.example.musicscorebuilder.components.frames;

import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.frames.HeaderFrame;

public class HeaderFrameLayout extends FrameLayout {
    private final double titleFontSize, subtitleFontSize, composerFontSize, numberNewFontSize, numberOldFontSize;
    private final double numBoxMinWidth, numBoxMinHeight, numBoxRadius, numBoxStrokeWidth, numBoxSpacing, numBoxPaddingX, numBoxPaddingY;

    public HeaderFrameLayout(PageLayout parent, ScoreStyle style, HeaderFrame frameData) {
        super(parent, style, frameData);

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

    public HeaderFrame getHeaderFrameData() {
        return (HeaderFrame) frameData;
    }

    public String getTitle() {
        HeaderFrame hf = getHeaderFrameData();
        return hf.getTitle() != null ? hf.getTitle() : "";
    }

    public String getSubtitle() {
        HeaderFrame hf = getHeaderFrameData();
        return hf.getSubtitle() != null ? hf.getSubtitle() : "";
    }

    public String getComposer() {
        HeaderFrame hf = getHeaderFrameData();
        return hf.getComposer() != null ? hf.getComposer() : "";
    }

    public String getNumberNew() {
        HeaderFrame hf = getHeaderFrameData();
        return hf.getNumberNew() != null ? hf.getNumberNew() : "";
    }

    public String getNumberOld() {
        HeaderFrame hf = getHeaderFrameData();
        return hf.getNumberOld() != null ? hf.getNumberOld() : "";
    }

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