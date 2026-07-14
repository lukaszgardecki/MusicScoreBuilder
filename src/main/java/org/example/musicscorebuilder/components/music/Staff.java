package org.example.musicscorebuilder.components.music;

public class Staff {
    private final int linesNumber = 5;
    private final int index;
    private Clef defaultClef;
    private KeySig defaultKeySig;

    public Staff(int index, ClefType defaultClef, KeySigType defaultKeySig) {
        this.index = index;
        this.defaultClef = new Clef(defaultClef);
        this.defaultKeySig = new KeySig(defaultKeySig);
    }

    public int getLinesNumber() { return linesNumber; }
    public int getIndex() { return index; }
    public Clef getDefaultClef() { return defaultClef; }
    public KeySig getDefaultKeySig() { return defaultKeySig; }
}
