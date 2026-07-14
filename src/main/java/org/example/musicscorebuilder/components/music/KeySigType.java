package org.example.musicscorebuilder.components.music;

public enum KeySigType {
    F1(Leland.FLAT, new double[] { 2.0 }),
    F2(Leland.FLAT, new double[] { 2.0, 0.5 }),
    F3(Leland.FLAT, new double[] { 2.0, 0.5, 2.5 }),
    F4(Leland.FLAT, new double[] { 2.0, 0.5, 2.5, 1.0 }),
    F5(Leland.FLAT, new double[] { 2.0, 0.5, 2.5, 1.0, 3.0 }),
    F6(Leland.FLAT, new double[] { 2.0, 0.5, 2.5, 1.0, 3.0, 1.5 }),
    F7(Leland.FLAT, new double[] { 2.0, 0.5, 2.5, 1.0, 3.0, 1.5, 3.5 }),

    S1(Leland.SHARP, new double[] { 0.0 }),
    S2(Leland.SHARP, new double[] { 0.0, 1.5 }),
    S3(Leland.SHARP, new double[] { 0.0, 1.5, -0.5 }),
    S4(Leland.SHARP, new double[] { 0.0, 1.5, -0.5, 1.0 }),
    S5(Leland.SHARP, new double[] { 0.0, 1.5, -0.5, 1.0, 2.5 }),
    S6(Leland.SHARP, new double[] { 0.0, 1.5, -0.5, 1.0, 2.5, 0.5 }),
    S7(Leland.SHARP, new double[] { 0.0, 1.5, -0.5, 1.0, 2.5, 0.5, 2.0 });

    private final Leland fontData;
    private final double[] offsetsY;

    KeySigType(Leland fontData, double[] offsetY) {
        this.fontData = fontData;
        this.offsetsY = offsetY;
    }

    public Leland getFontData() { return fontData; }
    public double[] getOffsetsY(ClefType type) {
        double[] result = new double[offsetsY.length];
        double shift = 0.0;

        if (type == ClefType.F) {
            shift = 1.0;
        }else if (type == ClefType.C) {
            shift = 0.5;
        }

        for (int i = 0; i < offsetsY.length; i++) {
            result[i] = offsetsY[i] + shift;
        }

        return result;
    }
}