package org.example.musicscorebuilder.components.music;

import java.util.HashMap;
import java.util.Map;

public class Measure {
    private int number;
    private TimeSignature timeSignature;
    private final Map<Part, PMeasure> partMeasures = new HashMap<>();
    private BarlineStyle barlineStyle;

    public Measure(int number, TimeSignature timeSignature, BarlineStyle barlineStyle) {
        this.number = number;
        this.timeSignature = timeSignature;
        this.barlineStyle = barlineStyle;
    }

    public void put(Part part, PMeasure measure) {
        partMeasures.put(part, measure);
    }

    public int getNumber() { return number; }
    public TimeSignature getTimeSignature() { return timeSignature; }
    public Map<Part,  PMeasure> getPartMeasures() { return partMeasures; }
    public BarlineStyle getBarlineStyle() { return barlineStyle; }

    public void setBarlineStyle(BarlineStyle barlineStyle) { this.barlineStyle = barlineStyle; }
}