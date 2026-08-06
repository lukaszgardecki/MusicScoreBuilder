package org.example.musicscorebuilder.components.music;

public class Slur {
    private Note startNote;
    private Note endNote;

    public Slur(Note startNote, Note endNote) {
        this.startNote = startNote;
        this.endNote = endNote;
    }

    public Note getStartNote() { return startNote; }
    public void setStartNote(Note startNote) { this.startNote = startNote; }

    public Note getEndNote() { return endNote; }
    public void setEndNote(Note endNote) { this.endNote = endNote; }
}