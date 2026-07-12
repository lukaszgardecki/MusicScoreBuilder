package org.example.musicscorebuilder.components.music;

public enum ClefType {
    C(Leland.CLEF_C, 2),
    F(Leland.CLEF_F, 1),
    G(Leland.CLEF_G, 3);

    private final Leland fontData;
    private final double offsetY;

    ClefType(Leland fontData, double offsetY) {
        this.fontData = fontData;
        this.offsetY = offsetY;
    }

    public Leland getFontData() { return fontData; }
    public double getOffsetY() { return offsetY; }
}
