package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout {
    private final Leland fontData;
    private double height = 7;

    public ClefLayout(ClefType clefType) {
        fontData = switch (clefType) {
            case G -> Leland.CLEF_G;
            default -> null;
        };
    }

    public double getHeight() { return height; }
    public double getWidth() { return height * fontData.getRatio(); }
    public String getCode() { return fontData.getCode(); }
    public void setHeight(double height) { this.height = height; }
}
