package org.example.musicscorebuilder.managers;

import javafx.scene.text.Font;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

public class FontManager extends AbstractFontManager {
    private static final Map<FontType, Font> fxFonts = new EnumMap<>(FontType.class);
    private static final Map<FontType, java.awt.Font> awtFonts = new EnumMap<>(FontType.class);
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
    private static final FontManager INSTANCE = new FontManager();

    public static FontManager getInstance() {
        return INSTANCE;
    }

    public static void loadFonts() {
        for (FontType type : FontType.values()) {
            INSTANCE.ensureLoaded(type);
        }
    }

    public static Font getLelandFont(double size) {
        INSTANCE.ensureLoaded(FontType.LELAND);
        Font font = fxFonts.get(FontType.LELAND);
        return font != null ? new Font(font.getName(), size) : new Font(size);
    }

    public static Font getFreeSerifFont(double size) {
        INSTANCE.ensureLoaded(FontType.FREE_SERIF);
        Font font = fxFonts.get(FontType.FREE_SERIF);
        return font != null ? new Font(font.getName(), size) : new Font(size);
    }

    @Override
    protected void loadNativeFont(FontType type) {
        try {
            var resource = FontManager.class.getResource(type.getResourcePath());
            if (resource != null) {
                Font fxFont = Font.loadFont(resource.toExternalForm(), 14.0);
                if (fxFont != null) fxFonts.put(type, fxFont);
            }
        } catch (Exception ignored) {}

        try (InputStream is = FontManager.class.getResourceAsStream(type.getResourcePath())) {
            if (is != null) {
                java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
                awtFonts.put(type, awtFont);
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected double computeWidth(FontType type, String text, double fontSize, FontStyle style) {
        java.awt.Font derived = getDerivedAwtFont(type, fontSize, style);
        if (derived == null) return 0.0;
        Rectangle2D bounds = derived.getStringBounds(text, FRC);
        return bounds.getWidth();
    }

    @Override
    protected double computeHeight(FontType type, String text, double fontSize, FontStyle style) {
        java.awt.Font derived = getDerivedAwtFont(type, fontSize, style);
        if (derived == null) return 0.0;
        Rectangle2D bounds = derived.getStringBounds(text, FRC);
        return bounds.getHeight();
    }

    private java.awt.Font getDerivedAwtFont(FontType type, double fontSize, FontStyle style) {
        java.awt.Font awtFont = awtFonts.get(type);
        if (awtFont == null) return null;

        int awtStyle = java.awt.Font.PLAIN;
        if (style == FontStyle.BOLD_ITALIC) awtStyle = java.awt.Font.BOLD | java.awt.Font.ITALIC;
        else if (style == FontStyle.BOLD) awtStyle = java.awt.Font.BOLD;
        else if (style == FontStyle.ITALIC) awtStyle = java.awt.Font.ITALIC;

        return awtFont.deriveFont(awtStyle, (float) fontSize);
    }
}