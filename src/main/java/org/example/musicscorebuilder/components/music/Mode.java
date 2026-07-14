package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Mode {
    private final ModeType type;
    private final BraceType braceType;
    private final Barline startBarline;
    private final List<Staff> staves = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();

    public Mode(ModeType type) {
        this.type = type;
        switch (type) {
            case SOLO -> staves.add(new Staff(0, ClefType.G, KeySigType.S2));
            case HARMONY -> {
                staves.add(new Staff(0, ClefType.G, KeySigType.F3));
                staves.add(new Staff(1, ClefType.F, KeySigType.F3));
            }
        }
       this.braceType = switch (type) {
           case SOLO -> BraceType.NONE;
           case HARMONY -> BraceType.BRACE;
       };
       this.startBarline = switch (type) {
            case SOLO -> null;
            case HARMONY -> new Barline(BarlineStyle.SINGLE, Barline.Type.START);
        };
    }

    public void appendMeasures(int count) {
        for (int i = 0; i < count; i++) appendMeasure();
    }

    public void appendMeasure() {
        Measure measure = new Measure(BarlineStyle.SINGLE, staves);
        for (int i = 0; i < 4; i++) {
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
}
