package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String title;
    private String composer;
    private final List<Part> parts = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();

    public Score(String title, String composer) {
        this.title = title;
        this.composer = composer;
    }

    public List<Part> getParts() {
        return parts;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public void add(Part part) {
        parts.add(part);
    }

    public void add(Measure measure) {
        measures.add(measure);
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }
}

