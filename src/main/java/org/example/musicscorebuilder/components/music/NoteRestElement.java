package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class NoteRestElement extends Element {
    private int voice;
    private NoteType type;
    private int duration;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected int dots;

    protected NoteRestElement() {
        super(null);
    }

    public NoteRestElement(Measure parent, int voice, NoteType type) {
        super(parent);
        this.voice = voice;
        this.type = type;
        this.duration = calculateDuration();
    }

    public int getVoice() { return voice; }
    public NoteType getType() { return type; }
    public int getDots() { return dots; }

    @JsonIgnore
    public boolean isDotted() { return dots > 0; }

    public void setType(NoteType type) {
        this.type = type;
        this.duration = calculateDuration();
    }
    public void setDots(int dots) {
        this.dots = dots;
        this.duration = calculateDuration();
    }

    @JsonIgnore
    protected int calculateDuration() {
        int baseTicks = type.getTicks();
        int totalTicks = baseTicks;
        int addedTicks = baseTicks / 2;

        for (int i = 0; i < dots; i++) {
            totalTicks += addedTicks;
            addedTicks /= 2;
        }
        return totalTicks;
    }
}