package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Page {
    private final PageFormat format;
    private final double marginTop;
    private final double marginBottom;
    private final double marginLeft;
    private final double marginRight;

    public Page(PageFormat format) {
        this(format, 25.0, 25.0, 25.0, 25.0);
    }

    @JsonCreator
    public Page(
            @JsonProperty("format") PageFormat format,
            @JsonProperty("marginTop") double marginTop,
            @JsonProperty("marginBottom") double marginBottom,
            @JsonProperty("marginLeft") double marginLeft,
            @JsonProperty("marginRight") double marginRight
    ) {
        this.format = format;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
    }

    public PageFormat getFormat() { return format; }

    @JsonProperty("marginTop")
    public double getMarginTopMm() { return marginTop; }

    @JsonProperty("marginBottom")
    public double getMarginBottomMm() { return marginBottom; }

    @JsonProperty("marginLeft")
    public double getMarginLeftMm() { return marginLeft; }

    @JsonProperty("marginRight")
    public double getMarginRightMm() { return marginRight; }

    @JsonIgnore
    public double getWidthMm() { return format.getWidthMm(); }

    @JsonIgnore
    public double getHeightMm() { return format.getHeightMm(); }

    @JsonIgnore
    public double getEffectiveWidthMm() { return format.getWidthMm() - marginLeft - marginRight; }

    @JsonIgnore
    public double getEffectiveHeightMm() { return format.getHeightMm() - marginTop - marginBottom; }
}
