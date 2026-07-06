package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;

public class BraceLayout {
    private final Leland fontData = Leland.BRACE;
    private double height;

    public double getHeight() { return height; }
    public double getWidth() { return height * fontData.getRatio() + 2.5; }
    public String getCode() { return fontData.getCode(); }
    public void setHeight(double height) { this.height = height; }
}