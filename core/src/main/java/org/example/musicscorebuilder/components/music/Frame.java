package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
public class Frame {

    @JsonProperty("w") private Double width;
    @JsonProperty("h") private Double height;
    @JsonProperty("mTop") private Double marginTop;
    @JsonProperty("mBot") private Double marginBottom;
    @JsonProperty("mIdx") private int measureIndex;

    @JsonProperty("title") private String title;
    @JsonProperty("subtitle") private String subtitle;
    @JsonProperty("composer") private String composer;
    @JsonProperty("numOld") private String numberOld;
    @JsonProperty("numNew") private String numberNew;

    public Frame() {
        this(null, null, null, null, 0, null, null, null, null, null);
    }

    public Frame(int measureIndex) {
        this(null, null, null, null, measureIndex, null, null, null, null, null);
    }

    @JsonCreator
    public Frame(
            @JsonProperty("w") Double width,
            @JsonProperty("h") Double height,
            @JsonProperty("mTop") Double marginTop,
            @JsonProperty("mBot") Double marginBottom,
            @JsonProperty("mIdx") int measureIndex,
            @JsonProperty("title") String title,
            @JsonProperty("subtitle") String subtitle,
            @JsonProperty("composer") String composer,
            @JsonProperty("numOld") String numberOld,
            @JsonProperty("numNew") String numberNew
    ) {
        this.width = width;
        this.height = height;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.measureIndex = measureIndex;
        this.title = title;
        this.subtitle = subtitle;
        this.composer = composer;
        this.numberOld = numberOld;
        this.numberNew = numberNew;
    }

    @JsonProperty("w") public Double getWidth() { return width; }
    @JsonProperty("w") public void setWidth(Double width) { this.width = width; }

    @JsonProperty("mTop") public Double getMarginTop() { return marginTop; }
    @JsonProperty("mTop") public void setMarginTop(Double marginTop) { this.marginTop = marginTop; }

    @JsonProperty("mBot") public Double getMarginBottom() { return marginBottom; }
    @JsonProperty("mBot") public void setMarginBottom(Double marginBottom) { this.marginBottom = marginBottom; }

    @JsonProperty("h") public Double getHeight() { return height; }
    @JsonProperty("h") public void setHeight(Double height) { this.height = height; }

    @JsonProperty("mIdx") public int getMeasureIndex() { return measureIndex; }
    @JsonProperty("mIdx") public void setMeasureIndex(int measureIndex) { this.measureIndex = measureIndex; }

    @JsonProperty("title") public String getTitle() { return title; }
    @JsonProperty("title") public void setTitle(String title) { this.title = title; }

    @JsonProperty("subtitle") public String getSubtitle() { return subtitle; }
    @JsonProperty("subtitle") public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    @JsonProperty("composer") public String getComposer() { return composer; }
    @JsonProperty("composer") public void setComposer(String composer) { this.composer = composer; }

    @JsonProperty("numOld") public String getNumberOld() { return numberOld; }
    @JsonProperty("numOld") public void setNumberOld(String numberOld) { this.numberOld = numberOld; }

    @JsonProperty("numNew") public String getNumberNew() { return numberNew; }
    @JsonProperty("numNew") public void setNumberNew(String numberNew) { this.numberNew = numberNew; }
}