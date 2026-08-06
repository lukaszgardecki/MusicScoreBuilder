package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class KeySignature extends Element {
    private final KeySigType type;

    @JsonCreator
    public KeySignature(@JsonProperty("type") KeySigType type) {
        super(null);
        this.type = type;
    }

    public KeySignature(int value, Measure parent) {
        super(parent);
        this.type = KeySigType.of(value);
    }

    public KeySigType getType() { return type; }

    public int getAlterForStep(PitchStep step) {
        return type == null ? 0 : type.getAlterForStep(step);
    }
}