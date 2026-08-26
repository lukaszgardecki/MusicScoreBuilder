package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.TextMeasurer;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractFontManager implements TextMeasurer {

    protected final Set<FontType> loadedFonts = ConcurrentHashMap.newKeySet();

    public enum FontStyle {
        PLAIN, BOLD, ITALIC, BOLD_ITALIC;

        public static FontStyle from(boolean bold, boolean italic) {
            if (bold && italic) return BOLD_ITALIC;
            if (bold) return BOLD;
            if (italic) return ITALIC;
            return PLAIN;
        }
    }

    public void ensureLoaded(FontType type) {
        if (!loadedFonts.contains(type)) {
            synchronized (this) {
                if (!loadedFonts.contains(type)) {
                    loadNativeFont(type);
                    loadedFonts.add(type);
                }
            }
        }
    }

    protected abstract void loadNativeFont(FontType type);
    protected abstract double computeWidth(FontType type, String text, double fontSize, FontStyle style);
    protected abstract double computeHeight(FontType type, String text, double fontSize, FontStyle style);

    @Override
    public double getTextWidth(FontType type, String text, double fontSizeInSpatium, boolean bold, boolean italic) {
        if (text == null || text.isEmpty()) return 0.0;
        ensureLoaded(type);
        return computeWidth(type, text, fontSizeInSpatium, FontStyle.from(bold, italic));
    }

    @Override
    public double getTextHeight(FontType type, String text, double fontSizeInSpatium, boolean bold, boolean italic) {
        if (text == null || text.isEmpty()) return 0.0;
        ensureLoaded(type);
        return computeHeight(type, text, fontSizeInSpatium, FontStyle.from(bold, italic));
    }
}