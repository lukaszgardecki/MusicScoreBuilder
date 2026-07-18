package org.example.musicscorebuilder.components.music;

public enum PitchStep {
    C(0), D(1), E(2), F(3), G(4), A(5), H(6);

    private final int value;

    PitchStep(int value) {
        this.value = value;
    }

    public int getValue() { return value; }
}