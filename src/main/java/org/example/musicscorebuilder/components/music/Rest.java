package org.example.musicscorebuilder.components.music;

public class Rest extends Element implements NoteRestElement {
    private int voice;
    private NoteType type;
    private int duration;

    public Rest(int voice, NoteType type, Measure parent) {
        super(parent);
        this.voice = voice;
        this.type = type;
        this.duration = type.getTicks();
    }

    @Override public NoteType getType() { return type; }
    @Override public int getVoice() { return voice; }
    @Override public int getDuration() { return duration; }

    public void setType(NoteType type) {
        this.type = type;
        this.duration = type.getTicks();
    }
}