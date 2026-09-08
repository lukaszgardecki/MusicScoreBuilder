package org.example.musicscorebuilder.components.music.frames;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HeaderFrame.class, name = "header"),
        @JsonSubTypes.Type(value = TextFrame.class, name = "text")
})
public abstract class Frame {

    @JsonProperty("w") protected Double width;
    @JsonProperty("h") protected Double height;
    @JsonProperty("mTop") protected Double marginTop;
    @JsonProperty("mBot") protected Double marginBottom;
    @JsonProperty("mIdx") protected int measureIndex;

    public Frame() {
        this(null, null, null, null, 0);
    }

    public Frame(int measureIndex) {
        this(null, null, null, null, measureIndex);
    }

    public Frame(Double width, Double height, Double marginTop, Double marginBottom, int measureIndex) {
        this.width = width;
        this.height = height;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.measureIndex = measureIndex;
    }

    @JsonIgnore public boolean isBeforeMeasure() { return true; }

    @JsonProperty("w") public Double getWidth() { return width; }
    @JsonProperty("w") public void setWidth(Double width) { this.width = width; }

    @JsonProperty("h") public Double getHeight() { return height; }
    @JsonProperty("h") public void setHeight(Double height) { this.height = height; }

    @JsonProperty("mTop") public Double getMarginTop() { return marginTop; }
    @JsonProperty("mTop") public void setMarginTop(Double marginTop) { this.marginTop = marginTop; }

    @JsonProperty("mBot") public Double getMarginBottom() { return marginBottom; }
    @JsonProperty("mBot") public void setMarginBottom(Double marginBottom) { this.marginBottom = marginBottom; }

    @JsonProperty("mIdx") public int getMeasureIndex() { return measureIndex; }
    @JsonProperty("mIdx") public void setMeasureIndex(int measureIndex) { this.measureIndex = measureIndex; }
}