package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.TextMeasurer;

public class TextMeasurerService {
    private static TextMeasurer instance;

    public static void setInstance(TextMeasurer measurer) {
        instance = measurer;
    }

    public static TextMeasurer getInstance() {
        return instance;
    }
}
