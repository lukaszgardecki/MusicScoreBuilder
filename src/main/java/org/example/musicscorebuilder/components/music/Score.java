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

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
        parts.forEach(Part::removeLast);
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }

    public void addNewMeasure() {
        Measure measure;
        if (measures.isEmpty()) {
            measure = new Measure(1, new TimeSignature(4));
        } else {
            Measure last = measures.getLast();
            measure = new Measure(last.getNumber() + 1, last.getTimeSignature());
        }

        measures.add(measure);
        parts.forEach(part -> part.addMeasure(measure));
    }

    public void addMeasures(int count) {
        for (int i = 0; i < count; i++) {
            addNewMeasure();
        }
    }
}

