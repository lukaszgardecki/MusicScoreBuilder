package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String numberNew, numberOld, title, subtitle, composer;
    private final PageFormat pageFormat = PageFormat.A4_V;
    private final List<ScoreMode> scoreModes = new ArrayList<>();

    public Score() {
        this("", "", "", "", "");
    }

    public Score(String newNum, String oldNum, String title, String subtitle, String composer) {
        this.numberNew = newNum;
        this.numberOld = oldNum;
        this.title = title;
        this.subtitle = subtitle;
        this.composer = composer;
    }

    public void add(ScoreMode scoreMode) { scoreModes.add(scoreMode); }
    public void addNewMeasure() {
        scoreModes.forEach(ScoreMode::appendMeasure);
    }

    public void removeLastMeasure() {
        scoreModes.forEach(ScoreMode::removeLastMeasure);
    }

    public String getNumberNew() { return numberNew; }
    public String getNumberOld() { return numberOld; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getComposer() { return composer; }
    public PageFormat getPageFormat() { return pageFormat; }
    public List<ScoreMode> getModes() { return scoreModes; }
}
