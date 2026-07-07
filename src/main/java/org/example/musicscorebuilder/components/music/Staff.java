package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Staff {
    private final int linesNumber;
    private double lineWidth = 0.5;
    //    private List<Voice> voices = new ArrayList<>();
    private final Clef defaultClef;
    private final List<SMeasure> measures = new ArrayList<>();
    double lineSpacing = 5;

    public Staff() {
        this.defaultClef = new Clef(ClefType.G);
        this.linesNumber = 5;
    }

    public Staff(int linesNumber, Clef defaultClef) {
        this.defaultClef = defaultClef;
        this.linesNumber = linesNumber;
    }

    public int getLinesNumber() { return linesNumber; }
    public double getLineWidth() { return lineWidth; }
    public double getLineSpacing() { return lineSpacing; }
    public double getHeight() { return (linesNumber - 1) * lineSpacing; }

    public void addNewMeasure(Measure measure) {
        SMeasure sm;
        if (measures.isEmpty()) {
            sm = new SMeasure(measure.getNumber(), defaultClef, measure.getTimeSignature());
        } else {
            SMeasure last = measures.getLast();
            var clef = last.getClef();
            sm = new SMeasure(measure.getNumber(), clef, measure.getTimeSignature());
        }
        measures.add(sm);
        measure.add(sm);
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
    }
}