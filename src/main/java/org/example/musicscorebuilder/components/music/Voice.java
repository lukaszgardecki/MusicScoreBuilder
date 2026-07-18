package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Voice extends Element {
    private final int number;
    private final Staff staff;
    private final List<Chord> chords = new ArrayList<>();

    public Voice(int number, Staff staff) {
        this.number = number;
        this.staff = staff;
    }

    public void add(Chord chord) {
        chords.add(chord);
    }

    public Chord getChord(int number) {
        return chords.get(number);
    }

    public List<Chord> getChords() {
        return chords;
    }
}
