package org.example.musicscorebuilder.managers;

import javafx.scene.text.Font;

import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class FontManager {
    private static final String LELAND_PATH = "/fonts/Leland.otf";
    private static final String FREE_SERIF_PATH = "/fonts/FreeSerif.ttf";

    public enum FontType {
        LELAND("/fonts/Leland.otf"),
        FREE_SERIF("/fonts/FreeSerif.ttf");

        private final String resourcePath;

        FontType(String resourcePath) { this.resourcePath = resourcePath; }
        public String getResourcePath() { return resourcePath; }
    }

    private static Font lelandFont;
    private static Font freeSerifFont;

    public static void loadFonts() {
        if (lelandFont == null) lelandFont = loadFontTo(LELAND_PATH);
        if (freeSerifFont == null) freeSerifFont = loadFontTo(FREE_SERIF_PATH);
    }

    public static Font getLelandFont(double size) {
        if (lelandFont == null) loadFonts();
        return new Font(lelandFont.getName(), size);
    }

    public static Font getFreeSerifFont(double size) {
        if (freeSerifFont == null) loadFonts();
        return new Font(freeSerifFont.getName(), size);
    }

    private static final Map<FontType, Font> fxFonts = new EnumMap<>(FontType.class);
    private static final Map<FontType, java.awt.Font> awtFonts = new EnumMap<>(FontType.class);
    private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

    public static void loadFont(FontType type) {
        if (fxFonts.containsKey(type)) return;

        try {
            String path = Objects.requireNonNull(FontManager.class.getResource(type.getResourcePath())).toExternalForm();
            Font fxFont = Font.loadFont(path, 14.0);
            if (fxFont != null) {
                fxFonts.put(type, fxFont);
            }
        } catch (Exception e) {
            System.err.println("BŁĄD: Nie udało się załadować JavaFX Font: " + type);
        }

        try (InputStream is = FontManager.class.getResourceAsStream(type.getResourcePath())) {
            if (is != null) {
                java.awt.Font awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
                awtFonts.put(type, awtFont);
            }
        } catch (Exception e) {
            System.err.println("BŁĄD: Nie udało się załadować AWT Font: " + type);
        }
    }

    public static double getTextWidth(FontType type, String text, double fontSizeInSpatium) {
        if (text == null || text.isEmpty()) return 0.0;
        if (!awtFonts.containsKey(type)) {
            loadFont(type);
        }

        java.awt.Font awtFont = awtFonts.get(type);
        if (awtFont == null) return 0.0;

        java.awt.Font derived = awtFont.deriveFont((float) fontSizeInSpatium);
        Rectangle2D bounds = derived.getStringBounds(text, FRC);
        return bounds.getWidth();
    }

    public static double getTextHeight(FontType type, String text, double fontSizeInSpatium) {
        if (text == null || text.isEmpty()) return 0.0;
        if (!awtFonts.containsKey(type)) {
            loadFont(type);
        }

        java.awt.Font awtFont = awtFonts.get(type);
        if (awtFont == null) return 0.0;

        java.awt.Font derived = awtFont.deriveFont((float) fontSizeInSpatium);
        Rectangle2D bounds = derived.getStringBounds(text, FRC);
        return bounds.getHeight();
    }

    private static Font loadFontTo(String path) {
        String fontPath = Objects.requireNonNull(FontManager.class.getResource(path)).toExternalForm();
        Font font = Font.loadFont(fontPath, 14.0);
        if (font == null) {
            System.err.println("BŁĄD: Nie udało się załadować czcionki " + path + "!");
        }
        return font;
    }
}