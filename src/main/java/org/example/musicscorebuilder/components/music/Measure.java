package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Measure {
    private TimeSignature timeSignature;
    private KeySignature keySignature;
    private Barline rightBarline;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();
    private boolean dirty = true;

    // =========================================================
    // AKTUALNA ROZDZIELCZOŚĆ TAKTU – POBIERANA BEZPOŚREDNIO Z SEGMENTÓW
    // =========================================================
    private int currentResolutionTicks;

    public Measure(List<Staff> staves) {
        this.staves = staves;
        staves.forEach(staff -> staff.getDefaultClef().setParent(this));
        this.timeSignature = new TimeSignature(4, 4, this);
        this.keySignature = new KeySignature(-2, this);
        this.rightBarline = new Barline(BarlineStyle.FINAL, this);

        this.currentResolutionTicks = NoteType.WHOLE.getTicks();
    }

    // =========================================================
    // POBIERAMY INFO BEZPOŚREDNIO Z METOD SEGMENTU (ZAMIAST PĘTLI I LICZENIA)
    // =========================================================
    public void updateResolutionFromSegments() {
        if (segments.isEmpty()) {
            if (timeSignature != null) {
                this.currentResolutionTicks = timeSignature.getTotalTicks();
            } else {
                this.currentResolutionTicks = NoteType.WHOLE.getTicks();
            }
            return;
        }

        // Pobieramy gotową informację o czasie trwania bezpośrednio z każdego segmentu
        int minSegmentDuration = Integer.MAX_VALUE;

        for (Segment seg : segments) {
            if (seg.getType() == SegmentType.NOTEREST) {
                int segDuration = seg.getDuration(); // Pobieramy info bezpośrednio z segmentu!
                if (segDuration > 0 && segDuration < minSegmentDuration) {
                    minSegmentDuration = segDuration;
                }
            }
        }

        if (minSegmentDuration != Integer.MAX_VALUE) {
            this.currentResolutionTicks = minSegmentDuration;
        } else if (timeSignature != null) {
            this.currentResolutionTicks = timeSignature.getTotalTicks();
        }
    }

    public int getCurrentResolutionTicks() {
        return currentResolutionTicks;
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

            secondHalf.addElement(staff, new Rest(newNote.getVoice(), NoteType.fromTicks(halfTicks), this));

            segments.add(targetIndex + 1, secondHalf);
            segmentTicks = halfTicks;
        }
        targetSegment.insertNote(staff, newNote);

        updateResolutionFromSegments();
        setDirty(true);
    }

    public void changeElementDuration(Segment targetSegment, Staff staff, NoteRestElement elementToChange, NoteType newType) {
        MeasureDurationEditor.changeElementDuration(this, targetSegment, staff, elementToChange, newType);

        updateResolutionFromSegments();
        setDirty(true);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
    public Barline getRightBarline() { return rightBarline; }
    public BarlineStyle getBarlineStyle() { return rightBarline.getStyle(); }
    public TimeSignature getTimeSignature() { return timeSignature; }
    public KeySignature getKeySignature() { return keySignature; }
    public int countVoicesByStaff(Staff staff) {
        return segments.stream()
                .mapToInt(s -> s.getVoiceCountByStaff(staff))
                .max()
                .orElse(0);
    }

    public void addChordRestSegmentAtEnd() {
        Segment seg = new Segment(SegmentType.NOTEREST, this);

        var staff1 = staves.get(0);
        seg.addElement(staff1, new Note(1, PitchStep.C, 0, 4, NoteType.getRandom(), BeamType.NONE, this));
        seg.addElement(staff1, new Note(1, PitchStep.G, 0, 4, NoteType.getRandom(), BeamType.NONE, this));

        if (staves.size() == 2) {
            var staff2 = staves.get(1);
            seg.addElement(staff2, new Note(1, PitchStep.A, 0, 3, NoteType.getRandom(), BeamType.NONE, this));
            seg.addElement(staff2, new Note(1, PitchStep.D, 0, 3, NoteType.getRandom(), BeamType.NONE, this));
        }

        if (segments.isEmpty()) {
            segments.add(seg);
        } else {
            segments.add(segments.size() - 1, seg);
        }

        updateResolutionFromSegments();
        setDirty(true);
    }

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
    public void setBarlineStyle(BarlineStyle barlineStyle) {
        this.rightBarline.setStyle(barlineStyle);
        setDirty(true);
    }

    public void setTimeSignature(TimeSignature timeSignature) {
        this.timeSignature = timeSignature;
        updateResolutionFromSegments();
        setDirty(true);
    }
    public void setKeySignature(KeySignature keySignature) {
        this.keySignature = keySignature;
        setDirty(true);
    }
}