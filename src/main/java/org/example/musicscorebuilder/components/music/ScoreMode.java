package org.example.musicscorebuilder.components.music;

import org.example.musicscorebuilder.components.music.util.MeasureTimeSignatureAdjuster;
import org.example.musicscorebuilder.palette.PreDefinedTimeSignature;

import java.util.ArrayList;
import java.util.List;

public class ScoreMode {
    private final ModeType type;
    private final BraceType braceType;
    private final Barline startBarline;
    private final List<Staff> staves = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();

    public ScoreMode(ModeType type) {
        this.type = type;
        this.braceType = type == ModeType.SOLO ? BraceType.NONE : BraceType.BRACE;
        this.startBarline = type == ModeType.SOLO
                ? new Barline(BarlineStyle.NONE, Barline.Type.START, null)
                : new Barline(BarlineStyle.SINGLE, Barline.Type.START, null);
        addDefaultStaves();
    }

    public void appendMeasures(int count) {
        for (int i = 0; i < count; i++) appendMeasure();
    }

    public void appendMeasure() {
        Measure measure = new Measure(staves);

        if (!measures.isEmpty()) {
            Measure lastMeasure = measures.getLast();
            measure.setKeySignature(lastMeasure.getKeySignature());

            TimeSignature lastTimeSig = lastMeasure.getTimeSignature();
            measure.setTimeSignature(new TimeSignature(
                    lastTimeSig.getBeat(),
                    lastTimeSig.getBeatType(),
                    lastTimeSig.getType(),
                    measure
            ));

            measure.setBarlineStyle(lastMeasure.getBarlineStyle());
            lastMeasure.setBarlineStyle(BarlineStyle.SINGLE);
        } else {
            MeasureTimeSignatureAdjuster.adjustSegmentsToTimeSignature(measure);
        }

        measures.add(measure);
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
        if (measures.isEmpty()) return;
        measures.getLast().setBarlineStyle(BarlineStyle.FINAL);
    }

    public List<Measure> getMeasures() { return measures; }
    public BraceType getBraceType() { return braceType; }
    public Barline getStartBarline() { return startBarline; }

    public void setTimeSignature(PreDefinedTimeSignature timeSig) {
        if (measures.isEmpty()) return;

        measures.forEach(m -> {
            m.setTimeSignature(new TimeSignature(
                    timeSig.getBeat(),
                    timeSig.getBeatType(),
                    timeSig.getType(),
                    m
            ));
        });
    }

    public void setKeySignature(Integer key) {
        if (measures.isEmpty()) return;
        measures.forEach(m -> m.setKeySignature(new KeySignature(key, m)));
    }

    private void addDefaultStaves() {
        switch (type) {
            case SOLO -> staves.add(new Staff(0, new Clef(ClefType.G)));
            case HARMONY -> {
                staves.add(new Staff(0, new Clef(ClefType.G)));
                staves.add(new Staff(1, new Clef(ClefType.F)));
            }
        }
    }
}