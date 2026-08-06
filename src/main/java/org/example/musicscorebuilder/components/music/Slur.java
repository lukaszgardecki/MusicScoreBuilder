package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Slur {
    private final Note startNote;
    private final Note endNote;

    @JsonCreator
    public Slur(
            @JsonProperty("startNote") Note startNote,
            @JsonProperty("endNote") Note endNote
    ) {
        this.startNote = startNote;
        this.endNote = endNote;
    }

    public Note getStartNote() { return startNote; }
    public Note getEndNote() { return endNote; }
}