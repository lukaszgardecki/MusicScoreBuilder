package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Measure {
    private TimeSignature timeSignature;
    private Barline rightBarline;
    private boolean dirty = true;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();

    public Measure(BarlineStyle barlineStyle, List<Staff> staves, TimeSignature timeSignature) {
        this.staves = staves;
        this.timeSignature = timeSignature;
        this.rightBarline = new Barline(barlineStyle, Barline.Type.END);
    }

    public void insertNote(Segment targetSegment, Staff staff, Note newNote) {
        if (targetSegment.getType() != SegmentType.NOTEREST) return;

        int targetIndex = segments.indexOf(targetSegment);
        if (targetIndex == -1) return;

        int noteTicks = newNote.getType().getTicks();
        int segmentTicks = targetSegment.getDuration();

        while (segmentTicks > noteTicks) {
            int halfTicks = segmentTicks / 2;

            Segment secondHalf = new Segment(SegmentType.NOTEREST, this);

            secondHalf.addElement(staff, new Note(1, PitchStep.C, 0, 4, NoteType.fromTicks(halfTicks), BeamType.NONE));

            segments.add(targetIndex + 1, secondHalf);
            segmentTicks = halfTicks;
        }
        targetSegment.insertNote(staff, newNote);
        setDirty(true);
    }

    public void addEndBarlineSegment(BarlineStyle style) {
        var element = new Barline(style, Barline.Type.END);
        Segment seg = new Segment(SegmentType.BARLINE, this);

        for (Staff staff : staves) {
            seg.addElement(staff, element);
        }
        segments.add(seg);
        setDirty(true);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
    public int countVoicesByStaff(Staff staff) {
        return segments.stream()
                .mapToInt(s -> s.getVoiceCountByStaff(staff))
                .max()
                .orElse(0);
    }

    public void addChordRestSegmentAtEnd() {
        Segment seg = new Segment(SegmentType.NOTEREST, this);

        var staff1 = staves.get(0);
        seg.addElement(staff1, new Note(1, PitchStep.C, 0, 4, NoteType.getRandom(), BeamType.NONE));
        seg.addElement(staff1, new Note(1, PitchStep.G, 0, 4, NoteType.getRandom(), BeamType.NONE));

        if (staves.size() == 2) {
            var staff2 = staves.get(1);
            seg.addElement(staff2, new Note(1, PitchStep.A, 0, 3, NoteType.getRandom(), BeamType.NONE));
            seg.addElement(staff2, new Note(1, PitchStep.D, 0, 3, NoteType.getRandom(), BeamType.NONE));
        }

        if (segments.isEmpty()) {
            segments.add(seg);
        } else {
            segments.add(segments.size() - 1, seg);
        }

        setDirty(true);
    }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
}