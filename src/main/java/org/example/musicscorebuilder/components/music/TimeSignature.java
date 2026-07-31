package org.example.musicscorebuilder.components.music;

public class TimeSignature extends Element {
    public enum Type { FRACTIONAL, COMMON, CUT }

    private int beat;
    private int beatType;
    private Type type;

    public TimeSignature(int beat, int beatType, Type type, Measure parent) {
        super(parent);
        this.beat = beat;
        this.beatType = beatType;
        this.type = type;
    }

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
        int base = 3840 / beatType;

        if (beatType == 8 && beat % 3 == 0) {
            return (beat / 3) * (base * 3);
        }
        return beat * base;
    }
}