package org.example.musicscorebuilder.components.music;

public class Rest extends NoteRestElement {

    public Rest(int voice, NoteType type, Measure parent) {
        super(parent, voice, type);
    }
}