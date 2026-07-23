package org.example.musicscorebuilder.components.music;

import org.example.musicscorebuilder.util.FakeMeasureNotesGenerator;

import java.util.ArrayList;
import java.util.List;

public class ScoreMode {
    private final ModeType type;
    private final BraceType braceType;
    private final Barline startBarline;
    private final KeySignature keySignature;
    private final TimeSignature timeSignature;
    private final List<Staff> staves = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();

    public ScoreMode(ModeType type) {
        this.type = type;
        this.braceType = type == ModeType.SOLO ? BraceType.NONE : BraceType.BRACE;
        this.startBarline = type == ModeType.SOLO ? null : new Barline(BarlineStyle.SINGLE, Barline.Type.START);
        this.keySignature = new KeySignature(-2);
        this.timeSignature = new TimeSignature(4, 4);
        addDefaultStaves();
    }

    public void appendMeasures(int count) {
        for (int i = 0; i < count; i++) appendMeasure();
    }

    public void appendMeasure() {
        Measure measure = new Measure(BarlineStyle.SINGLE, staves);
        int targetSegments = FakeMeasureNotesGenerator.getMeasureCapacityInSegments(timeSignature);
        FakeMeasureNotesGenerator.fillMeasureWithTwoVoices(measure, targetSegments);

        if (!measures.isEmpty()) {
            Measure lastMeasure = measures.getLast();
            List<Segment> segments = lastMeasure.getSegments();
            segments.removeLast();
            lastMeasure.addEndBarlineSegment(BarlineStyle.SINGLE);
        }
        measure.addEndBarlineSegment(BarlineStyle.END);
        measures.add(measure);
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
        if (measures.isEmpty()) return;
        Measure lastMeasure = measures.getLast();
        List<Segment> segments = lastMeasure.getSegments();
        segments.removeLast();
        lastMeasure.addEndBarlineSegment(BarlineStyle.END);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Measure> getMeasures() { return measures; }
    public BraceType getBraceType() { return braceType; }
    public Barline getStartBarline() { return startBarline; }
    public KeySignature getKeySignature() { return keySignature; }
    public TimeSignature getTimeSignature() { return timeSignature; }

    public void setTimeSignature(TimeSignature timeSig) {
        var currentBeat = this.timeSignature.getBeat();
        var newBeat = timeSig.getBeat();

        this.timeSignature.update(timeSig.getBeat(), timeSig.getBeatType(), timeSig.getType());
        measures.forEach(m -> {
            List<Segment> segments = m.getSegments();

            if (currentBeat > newBeat) {
                int diff = currentBeat - newBeat;
                for (int i = 0; i < diff; i++) {
                    var lastBeatSegmentIdx = segments.size() - 2;
                    segments.remove(lastBeatSegmentIdx);
                }
            } else {
                int diff = newBeat - currentBeat;
                addSegments(diff, m);
            }
        });
    }

    private void addSegments(int count, Measure measure) {
        for (int i = 0; i < count; i++) {
            measure.addChordRestSegmentAtEnd();
        }
    }

    private void addDefaultStaves() {
        switch (type) {
            case SOLO -> staves.add(new Staff(0, ClefType.G));
            case HARMONY -> {
                staves.add(new Staff(0, ClefType.G));
                staves.add(new Staff(1, ClefType.F));
            }
        }
    }
}
