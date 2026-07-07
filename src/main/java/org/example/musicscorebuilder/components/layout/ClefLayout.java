package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout {
    private final Leland fontData = Leland.CLEF_G;
    private double height = 20;

    public double getHeight() { return height; }
    public double getWidth() { return height * fontData.getRatio(); }
    public String getCode() { return fontData.getCode(); }
    public void setHeight(double height) { this.height = height; }
}
