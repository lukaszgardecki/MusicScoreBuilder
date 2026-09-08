package org.example.musicscorebuilder.components.music.frames;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("header")
public class HeaderFrame extends Frame {

    @JsonProperty("title") private String title;
    @JsonProperty("subtitle") private String subtitle;
    @JsonProperty("composer") private String composer;
    @JsonProperty("numOld") private String numberOld;
    @JsonProperty("numNew") private String numberNew;

    public HeaderFrame() {
        super();
    }

    public HeaderFrame(int measureIndex) {
        super(measureIndex);
    }

    @JsonCreator
    public HeaderFrame(
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
        super(width, height, marginTop, marginBottom, measureIndex);
        this.title = title;
        this.subtitle = subtitle;
        this.composer = composer;
        this.numberOld = numberOld;
        this.numberNew = numberNew;
    }

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