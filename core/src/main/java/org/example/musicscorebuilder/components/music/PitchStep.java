package org.example.musicscorebuilder.components.music;

public enum PitchStep {
    C(0, 0),
    D(1, 2),
    E(2, 4),
    F(3, 5),
    G(4, 7),
    A(5, 9),
    B(6, 11);

    private final int value;
    private final int baseSemitones;

    PitchStep(int value, int baseSemitones) {
        this.value = value;
        this.baseSemitones = baseSemitones;
    }

    public int getValue() { return value; }
    public int getBaseSemitones() { return baseSemitones; }
}