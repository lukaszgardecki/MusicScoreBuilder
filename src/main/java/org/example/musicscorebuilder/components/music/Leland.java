package org.example.musicscorebuilder.components.music;

public enum Leland {
    BRACE("\uE000", 0.248, 3.98, -0.002, 0.019),
    CLEF_G("\uE050", 2.56, 4.449, -0.0, -2.666);

    private final String code;
    private final double NEx;
    private final double NEy;
    private final double SWx;
    private final double SWy;
    private final double ratio;

    Leland(String code, double NEx, double NEy, double SWx, double SWy) {
        this.code = code;
        this.NEx = NEx;
        this.NEy = NEy;
        this.SWx = SWx;
        this.SWy = SWy;
        this.ratio = getWidth() / getHeight();
    }

    public String getCode() { return code; }
    public double getRatio() { return ratio; }
    private double getHeight() { return NEy - SWy; }
    private double getWidth() { return NEx - SWx; }
}