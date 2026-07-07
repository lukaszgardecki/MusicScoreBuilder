package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Chord {
    private final List<Note> notes = new ArrayList<>();

    public void addNote(Note note) {
        notes.add(note);
    }

    public List<Note> getNotes() {
        return notes;
    }
}
