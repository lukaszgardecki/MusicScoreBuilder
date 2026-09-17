package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Volta {

    public enum LineStyle {
        SOLID,
        DASHED,
        DOTTED
    }

    @JsonProperty("text") private String text;
    @JsonProperty("fontSize") private Double fontSize;
    @JsonProperty("textXOffset") private Double textXOffset;
    @JsonProperty("closedEnd") private Boolean closedEnd;
    @JsonProperty("startHookH") private Double startHookHeight;
    @JsonProperty("endHookH") private Double endHookHeight;
    @JsonProperty("lineW") private Double lineWidth;
    @JsonProperty("style") private LineStyle lineStyle;
    @JsonProperty("dashLen") private Double dashLength;
    @JsonProperty("dashGap") private Double dashGap;
    @JsonProperty("yOffset") private Double yOffset;

    @JsonIgnore private Measure startMeasure;
    @JsonIgnore private Measure endMeasure;

    @JsonProperty("mIdxStart") private int startMeasureIndex = -1;
    @JsonProperty("mIdxEnd") private int endMeasureIndex = -1;

    public Volta() {
        this(null, null, null, false);
    }

    public Volta(Measure startMeasure, Measure endMeasure, String text, boolean closedEnd) {
        this.startMeasure = startMeasure;
        this.endMeasure = endMeasure;
        this.text = text;
        this.closedEnd = closedEnd ? true : null;
    }

    @JsonCreator
    public Volta(
            @JsonProperty("text") String text,
            @JsonProperty("fontSize") Double fontSize,
            @JsonProperty("textXOffset") Double textXOffset,
            @JsonProperty("closedEnd") Boolean closedEnd,
            @JsonProperty("startHookH") Double startHookHeight,
            @JsonProperty("endHookH") Double endHookHeight,
            @JsonProperty("lineW") Double lineWidth,
            @JsonProperty("style") LineStyle lineStyle,
            @JsonProperty("dashLen") Double dashLength,
            @JsonProperty("dashGap") Double dashGap,
            @JsonProperty("yOffset") Double yOffset,
            @JsonProperty("mIdxStart") int startMeasureIndex,
            @JsonProperty("mIdxEnd") int endMeasureIndex
    ) {
        this.text = text;
        this.fontSize = fontSize;
        this.textXOffset = textXOffset;
        this.closedEnd = closedEnd;
        this.startHookHeight = startHookHeight;
        this.endHookHeight = endHookHeight;
        this.lineWidth = lineWidth;
        this.lineStyle = lineStyle;
        this.dashLength = dashLength;
        this.dashGap = dashGap;
        this.yOffset = yOffset;
        this.startMeasureIndex = startMeasureIndex;
        this.endMeasureIndex = endMeasureIndex;
    }

    @JsonProperty("text") public String getText() { return text; }
    @JsonProperty("text") public void setText(String text) { this.text = text; }

    @JsonProperty("fontSize") public Double getFontSize() { return fontSize; }
    @JsonProperty("fontSize") public void setFontSize(Double fontSize) { this.fontSize = fontSize; }

    @JsonProperty("textXOffset") public Double getTextXOffset() { return textXOffset; }
    @JsonProperty("textXOffset") public void setTextXOffset(Double textXOffset) { this.textXOffset = textXOffset; }

    @JsonProperty("closedEnd") public boolean isClosedEnd() { return Boolean.TRUE.equals(closedEnd); }
    @JsonProperty("closedEnd") public void setClosedEnd(Boolean closedEnd) { this.closedEnd = closedEnd; }

    @JsonProperty("startHookH") public Double getStartHookHeight() { return startHookHeight; }
    @JsonProperty("startHookH") public void setStartHookHeight(Double startHookHeight) { this.startHookHeight = startHookHeight; }

    @JsonProperty("endHookH") public Double getEndHookHeight() { return endHookHeight; }
    @JsonProperty("endHookH") public void setEndHookHeight(Double endHookHeight) { this.endHookHeight = endHookHeight; }

    @JsonProperty("lineW") public Double getLineWidth() { return lineWidth; }
    @JsonProperty("lineW") public void setLineWidth(Double lineWidth) { this.lineWidth = lineWidth; }

    @JsonProperty("style") public LineStyle getLineStyle() { return lineStyle; }
    @JsonProperty("style") public void setLineStyle(LineStyle lineStyle) { this.lineStyle = lineStyle; }

    @JsonProperty("dashLen") public Double getDashLength() { return dashLength; }
    @JsonProperty("dashLen") public void setDashLength(Double dashLength) { this.dashLength = dashLength; }

    @JsonProperty("dashGap") public Double getDashGap() { return dashGap; }
    @JsonProperty("dashGap") public void setDashGap(Double dashGap) { this.dashGap = dashGap; }

    @JsonProperty("yOffset") public Double getYOffset() { return yOffset; }
    @JsonProperty("yOffset") public void setYOffset(Double yOffset) { this.yOffset = yOffset; }

    @JsonProperty("mIdxStart") public int getStartMeasureIndex() { return startMeasureIndex; }
    @JsonProperty("mIdxStart") public void setStartMeasureIndex(int startMeasureIndex) { this.startMeasureIndex = startMeasureIndex; }

    @JsonProperty("mIdxEnd") public int getEndMeasureIndex() { return endMeasureIndex; }
    @JsonProperty("mIdxEnd") public void setEndMeasureIndex(int endMeasureIndex) { this.endMeasureIndex = endMeasureIndex; }

    public Measure getStartMeasure() { return startMeasure; }
    public void setStartMeasure(Measure startMeasure) { this.startMeasure = startMeasure; }

    public Measure getEndMeasure() { return endMeasure; }
    public void setEndMeasure(Measure endMeasure) { this.endMeasure = endMeasure; }
}