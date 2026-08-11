package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Lyric {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<LyricFragment> fragments;

    private int verse = 1;
    private SyllableType type = SyllableType.SINGLE;

    @JsonProperty("fontSize")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double fontSize = null;

    public Lyric() {
        this.fragments = new ArrayList<>();
    }

    @JsonCreator
    public Lyric(
            @JsonProperty("fragments") List<LyricFragment> fragments,
            @JsonProperty("type") SyllableType type,
            @JsonProperty("verse") Integer verse,
            @JsonProperty("fontSize") Double fontSize
    ) {
        this.fragments = fragments != null ? fragments : new ArrayList<>();
        this.type = type != null ? type : SyllableType.SINGLE;
        this.verse = (verse != null && verse > 0) ? verse : 1;
        setFontSize(fontSize);
    }

    public Lyric(List<LyricFragment> fragments, SyllableType type, int verse, Double fontSize) {
        this.fragments = fragments != null ? fragments : new ArrayList<>();
        this.type = type != null ? type : SyllableType.SINGLE;
        this.verse = verse > 0 ? verse : 1;
        setFontSize(fontSize);
    }

    public List<LyricFragment> getFragments() { return fragments; }
    public void setFragments(List<LyricFragment> fragments) { this.fragments = fragments; }

    @JsonIgnore
    public String getText() {
        if (fragments == null || fragments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (LyricFragment fragment : fragments) {
            sb.append(fragment.getText());
        }
        return sb.toString();
    }

    public SyllableType getType() { return type; }
    public void setType(SyllableType type) { this.type = type; }

    public int getVerse() { return verse; }
    public void setVerse(int verse) { this.verse = verse; }

    public Double getFontSize() { return fontSize; }
    public void setFontSize(Double fontSize) {
        if (fontSize == null || fontSize <= 0.0) {
            this.fontSize = null;
        } else {
            this.fontSize = Math.round(fontSize * 100.0) / 100.0;
        }
    }
}