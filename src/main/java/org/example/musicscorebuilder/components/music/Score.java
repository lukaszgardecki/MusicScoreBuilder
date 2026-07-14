package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String title;
    private final PageFormat pageFormat = PageFormat.A4_V;
    private final List<Mode> modes = new ArrayList<>();

    public void add(Mode mode) { modes.add(mode); }
    public void addNewMeasure() {
        modes.forEach(Mode::appendMeasure);
    }

    public void removeLastMeasure() {
        modes.forEach(Mode::removeLastMeasure);
    }

    public String getTitle() { return title; }
    public PageFormat getPageFormat() { return pageFormat; }
    public List<Mode> getModes() { return modes; }
}
