package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.managers.FontType;

public interface TextMeasurer {
    double getTextWidth(FontType type, String text, double fontSizeInSpatium, boolean bold, boolean italic);
    double getTextHeight(FontType type, String text, double fontSizeInSpatium, boolean bold, boolean italic);

    default double getTextWidth(FontType type, String text, double fontSizeInSpatium) {
        return getTextWidth(type, text, fontSizeInSpatium, false, false);
    }

    default double getTextHeight(FontType type, String text, double fontSizeInSpatium) {
        return getTextHeight(type, text, fontSizeInSpatium, false, false);
    }
}
