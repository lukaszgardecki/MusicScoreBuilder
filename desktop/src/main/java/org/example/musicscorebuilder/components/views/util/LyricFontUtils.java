package org.example.musicscorebuilder.components.views.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.managers.FontManager;

import java.util.HashMap;
import java.util.Map;

public class LyricFontUtils {
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    public static Font getFont(boolean bold, boolean italic, double size) {
        FontWeight weight = bold ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = italic ? FontPosture.ITALIC : FontPosture.REGULAR;

        double rounded = Math.round(size * 10.0) / 10.0;
        String key = weight + "_" + posture + "_" + rounded;

        return FONT_CACHE.computeIfAbsent(key, k -> {
            Font baseFont = FontManager.getFreeSerifFont(rounded);
            if (bold || italic) {
                return Font.font(baseFont.getFamily(), weight, posture, rounded);
            }
            return baseFont;
        });
    }

    public static Font getFont(LyricLayout.FragmentLayout fragment, double sp) {
        return getFont(fragment.bold(), fragment.italic(), fragment.fontSizeSp() * sp);
    }

    public static Font getFont(LyricLayout.HyphenLayout hyphen, double sp) {
        return getFont(false, false, hyphen.fontSizeSp() * sp);
    }
}
