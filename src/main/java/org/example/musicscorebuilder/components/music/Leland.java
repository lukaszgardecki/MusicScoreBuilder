package org.example.musicscorebuilder.components.music;

public enum Leland {
    BRACE("\uE000", 0.248, 3.98, -0.002, 0.019),
    BRACKET("\uE002", 2.111, 5.724, 0.0, -1.724),

    CLEF_G("\uE050", 2.56, 4.449, -0.0, -2.666),
    CLEF_F("\uE062", 2.656, 1.004, 0.001, -2.468),
    CLEF_C("\uE05C", 2.508, 1.928, 0.0, -1.92),

    FLAT("\uE260", 0.812, 1.812, 0.0, -0.704),
    SHARP("\uE262", 0.976, 1.336, 0.0, -1.332);

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
    public double getHeight() { return NEy - SWy; }
    public double getWidth() { return NEx - SWx; }
    public double getNEy() { return NEy; }
    public double getSWx() { return SWx; }
    public double getSWy() { return SWy; }
}