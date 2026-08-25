package org.example.musicscorebuilder.components.views.util;

import javafx.scene.text.Text;
import org.example.musicscorebuilder.components.layout.LyricLayout;

public class TextMeasurer implements LyricLayout.TextMeasurer {
    private static final Text MEASURE_TEXT = new Text();

    @Override
    public TextBounds measure(String text, double fontSize, boolean bold, boolean italic) {
        MEASURE_TEXT.setFont(LyricFontUtils.getFont(bold, italic, fontSize));
        MEASURE_TEXT.setText(text);
        var bounds = MEASURE_TEXT.getLayoutBounds();
        return new TextBounds(bounds.getWidth(), bounds.getHeight());
    }
}
