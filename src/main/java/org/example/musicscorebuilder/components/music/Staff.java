package org.example.musicscorebuilder.components.music;

public class Staff {
    private final int linesNumber;
    private final Clef defaultClef;

    public Staff() {
        this.defaultClef = new Clef(ClefType.G);
        this.linesNumber = 5;
    }

    public Staff(int linesNumber, Clef defaultClef) {
        this.defaultClef = defaultClef;
        this.linesNumber = linesNumber;
    }

    public int getLinesNumber() { return linesNumber; }
    public Clef getDefaultClef() { return defaultClef; }

//    public void addNewMeasure(Measure measure) {
//        SMeasure sm;
//        if (measures.isEmpty()) {
//            sm = new SMeasure(measure.getNumber(), defaultClef);
//        } else {
//            SMeasure last = measures.getLast();
//            var clef = last.getClef();
//            sm = new SMeasure(measure.getNumber(), clef);
//        }
//        measures.add(sm);
//        measure.add(sm);
//    }
//
//    public void removeLastMeasure() {
//        if (measures.isEmpty()) return;
//        measures.removeLast();
//    }
}