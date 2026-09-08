package org.example.musicscorebuilder.components.music.frames;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TextFrameVerse {

    @JsonProperty("num")
    private int number;

    @JsonProperty("lines")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TextLine> lines = new ArrayList<>();

    public TextFrameVerse() {}

    @JsonCreator
    public TextFrameVerse(
            @JsonProperty("num") int number,
            @JsonProperty("lines") List<TextLine> lines
    ) {
        this.number = number;
        this.lines = lines != null ? lines : new ArrayList<>();
    }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public List<TextLine> getLines() { return lines; }
    public void setLines(List<TextLine> lines) { this.lines = lines; }
}