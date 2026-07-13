package org.example.musicscorebuilder.components.music;

public class Staff {
    private final int linesNumber;
    private final Clef defaultClef;

    public Staff(ClefType clefType) {
        this.defaultClef = new Clef(clefType);
        this.linesNumber = 5;
    }

    public Staff(int linesNumber, Clef defaultClef) {
        this.defaultClef = defaultClef;
        this.linesNumber = linesNumber;
    }

    public int getLinesNumber() { return linesNumber; }
    public Clef getDefaultClef() { return defaultClef; }
}