package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Mode {
    private final ModeType type;
    private final BraceType braceType;
    private final Barline startBarline;
    private final KeySignature keySignature;
    private final TimeSignature timeSignature;
    private final List<Staff> staves = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();

    public Mode(ModeType type) {
        this.type = type;
        switch (type) {
            case SOLO -> staves.add(new Staff(0, ClefType.G));
            case HARMONY -> {
                staves.add(new Staff(0, ClefType.G));
                staves.add(new Staff(1, ClefType.F));
            }
        }
        this.braceType = type == ModeType.SOLO ? BraceType.NONE : BraceType.BRACE;
        this.startBarline = type == ModeType.SOLO ? null : new Barline(BarlineStyle.SINGLE, Barline.Type.START);
        this.keySignature = new KeySignature(0);
        this.timeSignature = new TimeSignature(5, 8, false, false);
    }

    public void appendMeasures(int count) {
        for (int i = 0; i < count; i++) appendMeasure();
    }

    public void appendMeasure() {
        Measure measure = new Measure(BarlineStyle.SINGLE, staves);
        var measureBeats = timeSignature.getBeat();
        for (int i = 0; i < measureBeats; i++) {
            measure.add(new Element());
        }
        if (!measures.isEmpty()) {
            Measure lastMeasure = measures.getLast();
            List<Segment> segments = lastMeasure.getSegments();
            segments.removeLast();
            lastMeasure.add(new Barline(BarlineStyle.SINGLE, Barline.Type.END));
        }
        measure.add(new Barline(BarlineStyle.END, Barline.Type.END));
        measures.add(measure);
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
        if (measures.isEmpty()) return;
        Measure lastMeasure = measures.getLast();
        List<Segment> segments = lastMeasure.getSegments();
        segments.removeLast();
        lastMeasure.add(new Barline(BarlineStyle.END, Barline.Type.END));
    }

    public List<Staff> getStaves() { return staves; }
    public List<Measure> getMeasures() { return measures; }
    public BraceType getBraceType() { return braceType; }
    public Barline getStartBarline() { return startBarline; }
    public  KeySignature getKeySignature() { return keySignature; }
    public TimeSignature getTimeSignature() { return timeSignature; }
}
