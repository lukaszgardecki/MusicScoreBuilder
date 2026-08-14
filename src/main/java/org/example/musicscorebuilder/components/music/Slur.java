package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Slur {
    @JsonIgnore
    private Note startNote;

    @JsonIgnore
    private Note endNote;

    private String startNoteId;
    private String endNoteId;

    @JsonCreator
    public Slur(
            @JsonProperty("startNoteId") String startNoteId,
            @JsonProperty("endNoteId") String endNoteId
    ) {
        this.startNoteId = startNoteId;
        this.endNoteId = endNoteId;
    }

    public Slur(Note startNote, Note endNote) {
        this.startNote = startNote;
        this.endNote = endNote;
    }

    public Note getStartNote() { return startNote; }
    public void setStartNote(Note startNote) { this.startNote = startNote; }

    public Note getEndNote() { return endNote; }
    public void setEndNote(Note endNote) { this.endNote = endNote; }

    public String getStartNoteId() { return startNoteId; }
    public void setStartNoteId(String startNoteId) { this.startNoteId = startNoteId; }

    public String getEndNoteId() { return endNoteId; }
    public void setEndNoteId(String endNoteId) { this.endNoteId = endNoteId; }
}