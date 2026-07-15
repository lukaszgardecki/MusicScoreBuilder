package org.example.musicscorebuilder.components.music;

public class Staff {
    private final int linesNumber = 5;
    private final int index;
    private Clef defaultClef;

    public Staff(int index, ClefType defaultClef) {
        this.index = index;
        this.defaultClef = new Clef(defaultClef);
    }

    public int getLinesNumber() { return linesNumber; }
    public int getIndex() { return index; }
    public Clef getDefaultClef() { return defaultClef; }
}
