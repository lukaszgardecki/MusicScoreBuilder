package org.example.musicscorebuilder.components.music;

public enum Leland {
    BRACE("\uE000", 0.248, 3.98, -0.002, 0.019),
    BRACKET("\uE002", 2.111, 5.724, 0.0, -1.724),

    CLEF_G("\uE050", 2.56, 4.449, -0.0, -2.666),
    CLEF_F("\uE062", 2.656, 1.004, 0.001, -2.468),
    CLEF_C("\uE05C", 2.508, 1.928, 0.0, -1.92),

    FLAT("\uE260", 0.812, 1.812, 0.0, -0.704),
    SHARP("\uE262", 0.976, 1.336, 0.0, -1.332),

    TIME_0("\uE080", 1.556, 1.02, 0.06, -1.016),
    TIME_1("\uE081", 1.344, 0.98, 0.06, -0.972),
    TIME_2("\uE082", 1.508, 0.98, 0.06, -0.972),
    TIME_3("\uE083", 1.457, 0.976, 0.06, -0.976),
    TIME_4("\uE084", 1.768, 0.996, 0.055, -0.992),
    TIME_5("\uE085", 1.448, 0.984, 0.06, -0.976),
    TIME_6("\uE086", 1.548, 0.98, 0.06, -0.976),
    TIME_7("\uE087", 1.462, 1.004, 0.058, -1.0),
    TIME_8("\uE088", 1.572, 0.984, 0.06, -0.992),
    TIME_9("\uE089", 1.548, 0.98, 0.06, -0.976),
    TIME_COMMON("\uE08A", 1.852, 1.032, 0.0, -1.024),
    TIME_CUT("\uE08B", 1.852, 1.552, 0.0, -1.504);

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