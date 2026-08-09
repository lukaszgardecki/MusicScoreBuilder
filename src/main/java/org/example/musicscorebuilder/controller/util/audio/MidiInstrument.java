package org.example.musicscorebuilder.controller.util.audio;

public enum MidiInstrument {

    ACOUSTIC_GRAND_PIANO(0),
    BRIGHT_ACOUSTIC_PIANO(1),
    ELECTRIC_GRAND_PIANO(2),
    HONKY_TONK_PIANO(3),
    ELECTRIC_PIANO_1(4),
    ELECTRIC_PIANO_2(5),
    HARPSICHORD(6),
    CLAVINET(7),

    DRAWBAR_ORGAN(16),
    PERCUSSIVE_ORGAN(17),
    ROCK_ORGAN(18),
    CHURCH_ORGAN(19),
    REED_ORGAN(20),

    ACOUSTIC_GUITAR(24),
    VIOLIN(40),
    TRUMPET(56),
    FLUTE(73),
    BANJO(105);

    private final int programNumber;

    MidiInstrument(int programNumber) {
        this.programNumber = programNumber;
    }

    public int getProgramNumber() {
        return programNumber;
    }
}