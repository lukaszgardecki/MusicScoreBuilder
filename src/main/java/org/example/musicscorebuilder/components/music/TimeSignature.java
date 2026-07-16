package org.example.musicscorebuilder.components.music;

public class TimeSignature extends Element {
    public enum Type { FRACTIONAL, COMMON, CUT }

    private int beat;
    private int beatType;
    private Type type;

    public TimeSignature(int beat, int beatType) {
        this.beat = beat;
        this.beatType = beatType;
        this.type = Type.FRACTIONAL;
    }

    public static TimeSignature commonTime() {
        TimeSignature ts = new TimeSignature(4, 4);
        ts.type = Type.COMMON;
        return ts;
    }

    public static TimeSignature cutTime() {
        TimeSignature ts = new TimeSignature(2, 2);
        ts.type = Type.CUT;
        return ts;
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
    }
}
