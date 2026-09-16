package org.example.musicscorebuilder.components.music;

public enum JumpType {

    SEGNO("", Position.START_OF_MEASURE, Leland.SEGNO),
    SEGNO_SERPENT_1("", Position.START_OF_MEASURE, 0.7, Leland.SEGNO_SERPENT_1),
    CODA("", Position.START_OF_MEASURE, Leland.CODA),
    CODA_SQUARE("", Position.START_OF_MEASURE, Leland.CODA_SQUARE),

    FINE("Fine", Position.END_OF_MEASURE, null),
    TO_CODA("To Coda", Position.END_OF_MEASURE, null),
    DC("D.C.", Position.END_OF_MEASURE, null),
    DS("D.S.", Position.END_OF_MEASURE, null),
    DC_AL_FINE("D.C. al Fine", Position.END_OF_MEASURE, null),
    DS_AL_FINE("D.S. al Fine", Position.END_OF_MEASURE, null),
    DC_AL_CODA("D.C. al Coda", Position.END_OF_MEASURE, null),
    DS_AL_CODA("D.S. al Coda", Position.END_OF_MEASURE, null);

    private final String defaultText;
    private final Position defaultPosition;
    private final double heightFactor;
    private final Leland fontData;

    public enum Position { START_OF_MEASURE, END_OF_MEASURE }

    JumpType(String defaultText, Position defaultPosition, Leland fontData) {
        this.defaultText = defaultText;
        this.defaultPosition = defaultPosition;
        this.fontData = fontData;
        this.heightFactor = 1.0;
    }

    JumpType(String defaultText, Position defaultPosition, double heightFactor, Leland fontData) {
        this.defaultText = defaultText;
        this.defaultPosition = defaultPosition;
        this.fontData = fontData;
        this.heightFactor = heightFactor;
    }

    public String getDefaultText() { return defaultText; }
    public Position getDefaultPosition() { return defaultPosition; }
    public double getHeightFactor() { return heightFactor; }
    public Leland getFontData() { return fontData; }
    public boolean isSymbol() { return fontData != null; }
}