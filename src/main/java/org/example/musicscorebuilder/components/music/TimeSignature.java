package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class TimeSignature extends Element {
    public enum Type { FRACTIONAL, COMMON, CUT }

    @JsonProperty("nb")
    private final int nominalBeat;

    @JsonProperty("nbt")
    private final int nominalBeatType;

    @JsonProperty("ab")
    private final int actualBeat;

    @JsonProperty("abt")
    private final int actualBeatType;

    @JsonProperty("t")
    private final Type type;

    @JsonCreator
    public TimeSignature(
            @JsonProperty("nb") Integer nominalBeat,
            @JsonProperty("nbt") Integer nominalBeatType,
            @JsonProperty("ab") Integer actualBeat,
            @JsonProperty("abt") Integer actualBeatType,
            @JsonProperty("t") Type type
    ) {
        super(null);
        this.nominalBeat = nominalBeat != null ? nominalBeat : 4;
        this.nominalBeatType = nominalBeatType != null ? nominalBeatType : 4;
        this.actualBeat = actualBeat != null ? actualBeat : this.nominalBeat;
        this.actualBeatType = actualBeatType != null ? actualBeatType : this.nominalBeatType;
        this.type = type != null ? type : Type.FRACTIONAL;
    }

    public TimeSignature(int nominalBeat, int nominalBeatType, int actualBeat, int actualBeatType, Type type, Measure parent) {
        super(parent);
        this.nominalBeat = nominalBeat;
        this.nominalBeatType = nominalBeatType;
        this.actualBeat = actualBeat;
        this.actualBeatType = actualBeatType;
        this.type = type != null ? type : Type.FRACTIONAL;
    }

    public TimeSignature(int beat, int beatType, Type type, Measure parent) {
        this(beat, beatType, beat, beatType, type, parent);
    }

    public TimeSignature(int beat, int beatType, Type type) {
        this(beat, beatType, beat, beatType, type, null);
    }

    public int getNominalBeat() { return nominalBeat; }
    public int getNominalBeatType() { return nominalBeatType; }

    public int getActualBeat() { return actualBeat; }
    public int getActualBeatType() { return actualBeatType; }

    public int getBeat() { return nominalBeat; }
    public int getBeatType() { return nominalBeatType; }

    public Type getType() { return type; }

    @JsonIgnore
    public boolean isCommon() { return type == Type.COMMON; }

    @JsonIgnore
    public boolean isCut() { return type == Type.CUT; }

    @JsonIgnore
    public boolean isFractional() { return type == Type.FRACTIONAL; }

    @JsonIgnore
    public int getTotalTicks() {
        if (actualBeatType == 0) return 0;
        int base = 3840 / actualBeatType;

        if (actualBeatType == 8 && actualBeat % 3 == 0) {
            return (actualBeat / 3) * (base * 3);
        }
        return actualBeat * base;
    }

    public boolean isVisuallyEqual(TimeSignature other) {
        if (other == null) return false;
        return nominalBeat == other.nominalBeat &&
                nominalBeatType == other.nominalBeatType &&
                type == other.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSignature that = (TimeSignature) o;
        return isVisuallyEqual(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nominalBeat, nominalBeatType, type);
    }
}