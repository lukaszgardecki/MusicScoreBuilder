package org.example.musicscorebuilder.components.frames;

import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.frames.TextFrame;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;
import org.example.musicscorebuilder.components.music.frames.TextLine;

import java.util.List;

public class TextFrameLayout extends FrameLayout {

    private final TextFrame textFrame;

    public TextFrameLayout(PageLayout parent, ScoreStyle style, TextFrame frameData) {
        super(parent, style, frameData);
        this.textFrame = frameData;

        if (frameData.getHeight() == null || frameData.getHeight() <= 0) {
            calculateAutoHeight();
        }
    }

    public List<TextFrameVerse> getVerses() {
        return textFrame.getVerses();
    }

    public TextFrame getTextFrame() {
        return textFrame;
    }

    private void calculateAutoHeight() {
        if (getVerses() == null || getVerses().isEmpty()) {
            setContentHeight(2.0);
            return;
        }

        double defaultFontSizeSp = 1.8;
        double verseSpacingSp = 1.0;
        double totalHeight = 0;

        for (TextFrameVerse verse : getVerses()) {
            if (verse.getLines() != null) {
                for (TextLine line : verse.getLines()) {
                    double fontSizeSp = line.getFontSize() != null ? line.getFontSize() : defaultFontSizeSp;
                    totalHeight += fontSizeSp * 1.3;
                }
            }
            totalHeight += verseSpacingSp;
        }

        if (totalHeight > 0) {
            totalHeight -= verseSpacingSp;
        }

        setContentHeight(Math.max(2.0, totalHeight));
    }
}