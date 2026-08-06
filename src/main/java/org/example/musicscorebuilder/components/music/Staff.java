package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Staff {
    public static final int LINES_NUMBER = 5;

    private final int index;
    private final Clef defaultClef;

    @JsonCreator
    public Staff(
            @JsonProperty("index") int index,
            @JsonProperty("defaultClef") Clef defaultClef
    ) {
        this.index = index;
        this.defaultClef = defaultClef;
    }

    @JsonIgnore
    public int getLinesNumber() { return LINES_NUMBER; }

    public int getIndex() { return index; }
    public Clef getDefaultClef() { return defaultClef; }
}
