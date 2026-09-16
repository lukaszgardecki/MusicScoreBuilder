package org.example.musicscorebuilder.palette;

public enum Repeat {

    // Symbole graficzne:
    SEGNO(""),
    SEGNO_SERPENT_1(""),
    CODA(""),
    CODA_SQUARE(""),

    // Tekstowe instrukcje skoku:
    FINE("Fine"),
    TO_CODA("To Coda"),
    DC("D.C."),
    DS("D.S."),
    DC_AL_FINE("D.C. al Fine"),
    DS_AL_FINE("D.S. al Fine"),
    DC_AL_CODA("D.C. al Coda"),
    DS_AL_CODA("D.S. al Coda"),

    //
    VOLTA_1("1."),
    VOLTA_2_OPENED("2."),
    VOLTA_2_CLOSED("2."),
    VOLTA_3_CLOSED("3.");

    private final String text;

    Repeat(String value) {
        text = value;
    }

    public String getText() { return text; }
}
