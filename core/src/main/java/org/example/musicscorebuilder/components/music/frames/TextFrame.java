package org.example.musicscorebuilder.components.music.frames;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("text")
public class TextFrame extends Frame {

    @JsonProperty("text") private String text;

    public TextFrame() {
        super();
    }

    public TextFrame(int measureIndex) {
        super(measureIndex);
    }

    public TextFrame(int measureIndex, String text) {
        super(measureIndex);
        this.text = text;
    }

    @JsonCreator
    public TextFrame(
            @JsonProperty("w") Double width,
            @JsonProperty("h") Double height,
            @JsonProperty("mTop") Double marginTop,
            @JsonProperty("mBot") Double marginBottom,
            @JsonProperty("mIdx") int measureIndex,
            @JsonProperty("text") String text
    ) {
        super(width, height, marginTop, marginBottom, measureIndex);
        this.text = text;
    }

    @Override
    public boolean isBeforeMeasure() { return false; }

    @JsonProperty("text") public String getText() { return text; }
    @JsonProperty("text") public void setText(String text) { this.text = text; }
}