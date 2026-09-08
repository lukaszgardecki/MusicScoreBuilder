package org.example.musicscorebuilder.components.music.frames;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("text")
public class TextFrame extends Frame {

    @JsonProperty("verses")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TextFrameVerse> verses = new ArrayList<>();

    public TextFrame() {
        super();
    }

    public TextFrame(int measureIndex) {
        super(measureIndex);
    }

    @JsonCreator
    public TextFrame(
            @JsonProperty("w") Double width,
            @JsonProperty("h") Double height,
            @JsonProperty("mTop") Double marginTop,
            @JsonProperty("mBot") Double marginBottom,
            @JsonProperty("mIdx") int measureIndex,
            @JsonProperty("verses") List<TextFrameVerse> verses
    ) {
        super(width, height, marginTop, marginBottom, measureIndex);
        this.verses = verses != null ? verses : new ArrayList<>();
    }

    @Override
    public boolean isBeforeMeasure() { return false; }

    @JsonProperty("verses")
    public List<TextFrameVerse> getVerses() { return verses; }

    @JsonProperty("verses")
    public void setVerses(List<TextFrameVerse> verses) { this.verses = verses; }

    public void addVerse(TextFrameVerse verse) {
        this.verses.add(verse);
    }
}