package org.example.musicscorebuilder.components.frames;

import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.frames.TextFrame;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;

import java.util.List;

public class TextFrameLayout extends FrameLayout {
    private final TextFrame textFrame;
    private final double padding, verseSpacing, versePaddingX, versePaddingY, verseCornerRadius, verseFontSize;

    public TextFrameLayout(PageLayout parent, ScoreStyle style, TextFrame frameData) {
        super(parent, style, frameData);
        this.textFrame = frameData;
        this.marginTop = frameData.getMarginTop() != null ? frameData.getMarginTop() : style.getTextFrameDefMarginTop();
        this.contentHeight = frameData.getHeight() != null ? frameData.getHeight() : style.getTextFrameDefHeight();
        this.padding = style.getTextFramePadding();
        this.verseSpacing = style.getTextFrameVerseSpacing();
        this.versePaddingX = style.getTextFrameVersePaddingX();
        this.versePaddingY = style.getTextFrameVersePaddingY();
        this.verseCornerRadius = style.getTextFrameVerseCornerRadius();
        this.verseFontSize = style.getTextFrameVerseFontSize();
    }

    public List<TextFrameVerse> getVerses() { return textFrame.getVerses(); }
    public double getPadding() { return padding; }
    public double getVerseSpacing() { return verseSpacing; }
    public double getVersePaddingX() { return versePaddingX; }
    public double getVersePaddingY() { return versePaddingY; }
    public double getVerseCornerRadius() { return verseCornerRadius; }
    public double getVerseFontSize() { return verseFontSize; }
}