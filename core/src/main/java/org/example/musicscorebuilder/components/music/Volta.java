package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Volta {
    private String text;
    private boolean closedEnd;

    @JsonIgnore
    private Measure startMeasure;
    @JsonIgnore
    private Measure endMeasure;

    @JsonProperty("mIdxStart")
    private int startMeasureIndex = -1;
    @JsonProperty("mIdxEnd")
    private int endMeasureIndex = -1;

    public Volta(Measure startMeasure, Measure endMeasure, String text, boolean closedEnd) {
        this.startMeasure = startMeasure;
        this.endMeasure = endMeasure;
        this.text = text;
        this.closedEnd = closedEnd;
    }

    @JsonCreator
    public Volta(
            @JsonProperty("text") String text,
            @JsonProperty("closedEnd") boolean closedEnd,
            @JsonProperty("mIdxStart") int startMeasureIndex,
            @JsonProperty("mIdxEnd") int endMeasureIndex
    ) {
        this.text = text;
        this.closedEnd = closedEnd;
        this.startMeasureIndex = startMeasureIndex;
        this.endMeasureIndex = endMeasureIndex;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isClosedEnd() { return closedEnd; }
    public void setClosedEnd(boolean closedEnd) { this.closedEnd = closedEnd; }

    public Measure getStartMeasure() { return startMeasure; }
    public void setStartMeasure(Measure startMeasure) { this.startMeasure = startMeasure; }

    public Measure getEndMeasure() { return endMeasure; }
    public void setEndMeasure(Measure endMeasure) { this.endMeasure = endMeasure; }

    public int getStartMeasureIndex() { return startMeasureIndex; }
    public void setStartMeasureIndex(int startMeasureIndex) { this.startMeasureIndex = startMeasureIndex; }

    public int getEndMeasureIndex() { return endMeasureIndex; }
    public void setEndMeasureIndex(int endMeasureIndex) { this.endMeasureIndex = endMeasureIndex; }
}