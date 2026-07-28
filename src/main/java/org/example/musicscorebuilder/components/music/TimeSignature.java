package org.example.musicscorebuilder.components.music;

public class TimeSignature extends Element {
    public enum Type { FRACTIONAL, COMMON, CUT }

    private int beat;
    private int beatType;
    private Type type;

    public TimeSignature(int beat, int beatType, Measure parent) {
        super(parent);
        this.beat = beat;
        this.beatType = beatType;
        this.type = Type.FRACTIONAL;
    }

    public int getBeat() { return beat; }
    public int getBeatType() { return beatType; }
    public TimeSignature.Type getType() { return type; }
    public boolean isCommon() { return type == Type.COMMON; }
    public boolean isCut() { return type == Type.CUT; }
    public boolean isFractional() { return type == Type.FRACTIONAL; }

    public void update(int beat, int beatType, Type type) {
        this.beat = beat;
        this.beatType = beatType;
        this.type = type;
        super.hasChanged();
    }

    public int getTotalTicks() {
        // Bazujemy na standardowej rozdzielczości, gdzie ćwierćnuta = 960 ticków
        // Wzór: (beat * (3840 / beatType))
        return beat * (3840 / beatType);
    }
}
