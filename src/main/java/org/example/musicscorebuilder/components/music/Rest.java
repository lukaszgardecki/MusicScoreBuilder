package org.example.musicscorebuilder.components.music;

public class Rest extends NoteRestElement {

    @SuppressWarnings("unused")
    private Rest() {
        super(null, 0, null);
    }

    public Rest(int voice, NoteType type, Measure parent) {
        super(parent, voice, type);
    }
}