package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Measure {
    private int number;
    private TimeSignature timeSignature;
    private final List<SMeasure> sMeasures = new ArrayList<>();

    public Measure(int number, TimeSignature timeSignature) {
        this.number = number;
        this.timeSignature = timeSignature;
    }

    public int getNumber() { return number; }
    public TimeSignature getTimeSignature() { return timeSignature; }
    public List<SMeasure> getSMeasures() { return sMeasures; }

    public void add(SMeasure sm) {
        sMeasures.add(sm);
    }

}