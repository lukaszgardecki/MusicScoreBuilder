package org.example.musicscorebuilder.components.music;

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
//        int targetSegments = FakeMeasureNotesGenerator.getMeasureCapacityInSegments(timeSignature);
//        FakeMeasureNotesGenerator.fillMeasureWithTwoVoices(measure, targetSegments);

        Segment seg = new Segment(SegmentType.NOTEREST, measure);
        seg.addElement(staves.getFirst(), new Rest(1, NoteType.WHOLE, measure));
        measure.getSegments().add(seg);

        if (!measures.isEmpty()) {
            Measure lastMeasure = measures.getLast();
            measure.setKeySignature(lastMeasure.getKeySignature());
            measure.setTimeSignature(lastMeasure.getTimeSignature());
            measure.setBarlineStyle(lastMeasure.getBarlineStyle());
            lastMeasure.setBarlineStyle(BarlineStyle.SINGLE);
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

        int newBeat = timeSig.getBeat();

        measures.forEach(m -> {
            TimeSignature timeSignature = m.getTimeSignature();
            timeSignature.update(timeSig.getBeat(), timeSig.getBeatType(), timeSig.getType());

            List<Segment> segments = m.getSegments();

            long noteRestCount = segments.stream()
                    .filter(s -> s.getType() == SegmentType.NOTEREST)
                    .count();

            if (noteRestCount > newBeat) {
                int diff = (int) (noteRestCount - newBeat);
                for (int i = 0; i < diff; i++) {
                    for (int idx = segments.size() - 1; idx >= 0; idx--) {
                        if (segments.get(idx).getType() == SegmentType.NOTEREST) {
                            segments.remove(idx);
                            break;
                        }
                    }
                }
            } else if (noteRestCount < newBeat) {
                int diff = (int) (newBeat - noteRestCount);
                addSegments(diff, m);
            }
            m.setDirty(true);
        });
    }

    private void addSegments(int count, Measure measure) {
        for (int i = 0; i < count; i++) {
            measure.addChordRestSegmentAtEnd();
        }
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
