package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String title;
    private final PageFormat pageFormat = PageFormat.A4_V;
    private final List<ScoreMode> scoreModes = new ArrayList<>();

    public void add(ScoreMode scoreMode) { scoreModes.add(scoreMode); }
    public void addNewMeasure() {
        scoreModes.forEach(ScoreMode::appendMeasure);
    }

    public void removeLastMeasure() {
        scoreModes.forEach(ScoreMode::removeLastMeasure);
    }

    public String getTitle() { return title; }
    public PageFormat getPageFormat() { return pageFormat; }
    public List<ScoreMode> getModes() { return scoreModes; }
}
