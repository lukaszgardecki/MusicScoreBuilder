package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String numberNew, numberOld, title, subtitle, composer;
    private Page page = new Page(PageFormat.A4_V, 10, 10, 10, 10);

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxTransposeDown;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxTransposeUp;

    @JsonProperty("modes")
    private List<ScoreMode> scoreModes = new ArrayList<>();

    public Score() {
        this("", "", "", "", "");
    }

    @JsonCreator
    public Score(
            @JsonProperty("numberNew") String newNum,
            @JsonProperty("numberOld") String oldNum,
            @JsonProperty("title") String title,
            @JsonProperty("subtitle") String subtitle,
            @JsonProperty("composer") String composer
    ) {
        this.numberNew = newNum != null ? newNum : "";
        this.numberOld = oldNum != null ? oldNum : "";
        this.title = title != null ? title : "";
        this.subtitle = subtitle != null ? subtitle : "";
        this.composer = composer != null ? composer : "";
    }

    public void add(ScoreMode scoreMode) { scoreModes.add(scoreMode); }

    public void applyTranspositionDelta(int semitoneDelta) {
        if (maxTransposeDown != null) {
            this.maxTransposeDown += semitoneDelta;
        }
        if (maxTransposeUp != null) {
            this.maxTransposeUp -= semitoneDelta;
        }
    }

    public String getNumberNew() { return numberNew; }
    public String getNumberOld() { return numberOld; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getComposer() { return composer; }
    public Page getPage() { return page; }
    public Integer getMaxTransposeDown() { return maxTransposeDown; }
    public Integer getMaxTransposeUp() { return maxTransposeUp; }

    public boolean canTransposeBy(int semitoneDelta) {
        if (semitoneDelta < 0) {
            return maxTransposeDown == null || Math.abs(semitoneDelta) <= maxTransposeDown;
        } else if (semitoneDelta > 0) {
            return maxTransposeUp == null || semitoneDelta <= maxTransposeUp;
        }
        return true;
    }

    @JsonProperty("modes")
    public List<ScoreMode> getModes() { return scoreModes; }

    public void setTitle(String title) { this.title = title; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public void setComposer(String composer) { this.composer = composer; }
    public void setNumberNew(String numberNew) { this.numberNew = numberNew; }
    public void setNumberOld(String numberOld) { this.numberOld = numberOld; }
    public void setMaxTransposeDown(Integer maxTransposeDown) { this.maxTransposeDown = maxTransposeDown; }
    public void setMaxTransposeUp(Integer maxTransposeUp) { this.maxTransposeUp = maxTransposeUp; }
}
