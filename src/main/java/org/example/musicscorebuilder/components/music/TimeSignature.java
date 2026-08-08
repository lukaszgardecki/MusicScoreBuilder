package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TimeSignature extends Element {
    public enum Type { FRACTIONAL, COMMON, CUT }

    private final int beat;
    private final int beatType;
    private final Type type;

    @JsonCreator
    public TimeSignature(
            @JsonProperty("beat") int beat,
            @JsonProperty("beatType") int beatType,
            @JsonProperty("type") Type type
    ) {
        super(null);
        this.beat = beat;
        this.beatType = beatType;
        this.type = type != null ? type : Type.FRACTIONAL;
    }

    public TimeSignature(int beat, int beatType, Type type, Measure parent) {
        super(parent);
        this.beat = beat;
        this.beatType = beatType;
        this.type = type;
    }

    public int getBeat() { return beat; }
    public int getBeatType() { return beatType; }
    public Type getType() { return type; }

    @JsonIgnore
    public boolean isCommon() { return type == Type.COMMON; }

    @JsonIgnore
    public boolean isCut() { return type == Type.CUT; }

    @JsonIgnore
    public boolean isFractional() { return type == Type.FRACTIONAL; }

    @JsonIgnore
    public int getTotalTicks() {
        if (beatType == 0) return 0;
        int base = 3840 / beatType;

        if (beatType == 8 && beat % 3 == 0) {
            return (beat / 3) * (base * 3);
        }
        return beat * base;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSignature that = (TimeSignature) o;
        return beat == that.beat &&
                beatType == that.beatType &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(beat, beatType, type);
    }
}