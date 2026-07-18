package org.example.musicscorebuilder.components.music;

public enum ClefType {
    C(Leland.CLEF_C, 2, 28),
    F(Leland.CLEF_F, 1, 24),
    G(Leland.CLEF_G, 3, 32);

    private final Leland fontData;
    private final double offsetY;
    private final int diatonicStep;

    ClefType(Leland fontData, double offsetY,  int diatonicStep) {
        this.fontData = fontData;
        this.offsetY = offsetY;
        this.diatonicStep = diatonicStep;
    }

    public Leland getFontData() { return fontData; }
    public double getOffsetY() { return offsetY; }
    public int getDiatonicStep() { return diatonicStep; }
}
