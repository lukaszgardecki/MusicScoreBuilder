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

    public List<Part> getParts() { return parts; }
    public List<Measure> getMeasures() { return measures; }
    public String getTitle() { return title; }
    public String getComposer() { return composer; }

    public void add(Part part) {
        parts.add(part);

        for (Measure measure : measures) {
            measure.put(part, createPMeasure(part));
        }
    }

    public void addNewMeasure() {
        Measure measure = measures.isEmpty() ? createFirstMeasure() : createNextMeasure();
        addMeasure(measure);
    }

    public void addMeasures(int count) {
        for (int i = 0; i < count; i++) {
            addNewMeasure();
        }
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
        if (measures.isEmpty()) return;
        measures.getLast().setBarlineStyle(BarlineStyle.END);
    }

    private void addMeasure(Measure measure) {
        measures.add(measure);
        for (Part part : parts) {
            measure.put(part, createPMeasure(part));
        }
    }

    private Measure createFirstMeasure() {
        return new Measure(1, new TimeSignature(4), BarlineStyle.END);
    }

    private Measure createNextMeasure() {
        int nextMeasureNumber = measures.size() + 1;
        Measure lastMeasure = measures.getLast();
        Measure newMeasure = new Measure(nextMeasureNumber, lastMeasure.getTimeSignature(), lastMeasure.getBarlineStyle());
        lastMeasure.setBarlineStyle(BarlineStyle.SINGLE);
        return newMeasure;
    }

    private PMeasure createPMeasure(Part part) {
        PMeasure pMeasure = new PMeasure();
        for (Staff staff : part.getStaves()) {
            SMeasure sMeasure = new SMeasure(staff.getDefaultClef());
            pMeasure.add(sMeasure);
        }
        return pMeasure;
    }
}

