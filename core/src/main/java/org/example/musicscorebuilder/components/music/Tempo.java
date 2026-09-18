package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tempo {

    private String text;
    private Double fontSize;
    private Double xOffset;
    private Double yOffset;
    private Boolean bold;
    private Boolean italic;

    @JsonIgnore
    private Measure measure;
    private int measureIndex = -1;
    private int segmentIndex = 0;

    public Tempo() {}

    public Tempo(Measure measure, int segmentIndex, String text) {
        this(measure, segmentIndex, text, null, null);
    }

    public Tempo(int measureIndex, int segmentIndex, String text) {
        this(measureIndex, segmentIndex, text, null, null);
    }

    public Tempo(Measure measure, int segmentIndex, String text, Boolean bold, Boolean italic) {
        this.measure = measure;
        this.segmentIndex = segmentIndex;
        this.text = text;
        this.bold = bold;
        this.italic = italic;
    }

    public Tempo(int measureIndex, int segmentIndex, String text, Boolean bold, Boolean italic) {
        this.measureIndex = measureIndex;
        this.segmentIndex = segmentIndex;
        this.text = text;
        this.bold = bold;
        this.italic = italic;
    }

    @JsonProperty("text")
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @JsonProperty("fontSize")
    public Double getFontSize() { return fontSize; }
    public void setFontSize(Double fontSize) { this.fontSize = fontSize; }

    @JsonProperty("xOffset")
    public Double getXOffset() { return xOffset; }
    public void setXOffset(Double xOffset) { this.xOffset = xOffset; }

    @JsonProperty("yOffset")
    public Double getYOffset() { return yOffset; }
    public void setYOffset(Double yOffset) { this.yOffset = yOffset; }

    @JsonProperty("bold")
    public Boolean getBold() { return bold; }
    public void setBold(Boolean bold) { this.bold = bold; }

    @JsonProperty("italic")
    public Boolean getItalic() { return italic; }
    public void setItalic(Boolean italic) { this.italic = italic; }

    @JsonIgnore
    public Measure getMeasure() { return measure; }
    public void setMeasure(Measure measure) { this.measure = measure; }

    @JsonProperty("mIdx")
    public int getMeasureIndex() { return measureIndex; }
    public void setMeasureIndex(int measureIndex) { this.measureIndex = measureIndex; }

    @JsonProperty("segIdx")
    public int getSegmentIndex() { return segmentIndex; }
    public void setSegmentIndex(int segmentIndex) { this.segmentIndex = segmentIndex; }
}