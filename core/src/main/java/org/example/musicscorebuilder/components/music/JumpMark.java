package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JumpMark {
    private JumpType type;
    private String text;

    @JsonIgnore
    private Measure measure;

    @JsonProperty("mIdx")
    private int measureIndex = -1;

    public JumpMark(JumpType type, Measure measure) {
        this.type = type;
        this.text = type.getDefaultText();
        this.measure = measure;
    }

    @JsonCreator
    public JumpMark(
            @JsonProperty("type") JumpType type,
            @JsonProperty("text") String text,
            @JsonProperty("mIdx") int measureIndex
    ) {
        this.type = type;
        this.text = text;
        this.measureIndex = measureIndex;
    }

    public JumpType getType() { return type; }
    public void setType(JumpType type) { this.type = type; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Measure getMeasure() { return measure; }
    public void setMeasure(Measure measure) { this.measure = measure; }

    public int getMeasureIndex() { return measureIndex; }
    public void setMeasureIndex(int measureIndex) { this.measureIndex = measureIndex; }
}