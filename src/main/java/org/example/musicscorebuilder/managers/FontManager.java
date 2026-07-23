package org.example.musicscorebuilder.managers;

import javafx.scene.text.Font;

import java.util.Objects;

public class FontManager {
    private static final String lelandFontPath = "/fonts/Leland.otf";
    private static Font lelandFont;

    public static void loadFonts() {
        if (lelandFont == null) {
            String fontPath = Objects.requireNonNull(FontManager.class.getResource(lelandFontPath)).toExternalForm();
            lelandFont = Font.loadFont(fontPath, 24.0);

            if (lelandFont == null) {
                System.err.println("BŁĄD: Nie udało się załadować czcionki z zasobów!");
            }
        }
    }

    public static Font getLelandFont(double size) {
        if (lelandFont == null) {
            loadFonts();
        }
        return new Font(lelandFont.getName(), size);
    }
}