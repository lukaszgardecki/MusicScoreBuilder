package org.example.musicscorebuilder.components.music;

public enum ModeType {
    SOLO("Melodia"),
    HARMONY("Harmonizacja"),
    TEXT("Tekst");

    private final String name;

    ModeType(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public String toString() { return name; }
}
