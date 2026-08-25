package org.example.musicscorebuilder.components.music;

public enum PageFormat {
    A4_V(210.0, 297.0),
    A4_H(297.0, 210.0),
    LETTER(215.9, 279.4),
    A5_V(148.0, 210.0),
    A5_H(210.0, 148.0);

    private final double widthMm;
    private final double heightMm;

    PageFormat(double widthMm, double heightMm) {
        this.widthMm = widthMm;
        this.heightMm = heightMm;
    }

    public double getWidthMm() { return widthMm; }
    public double getHeightMm() { return heightMm; }
}
