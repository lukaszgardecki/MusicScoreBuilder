package org.example.musicscorebuilder.components.music;

import org.example.musicscorebuilder.components.music.util.MeasureDurationEditor;
import org.example.musicscorebuilder.components.music.util.MeasureNoteInserter;
import org.example.musicscorebuilder.components.music.util.MeasureTimeSignatureAdjuster;

import java.util.ArrayList;
import java.util.List;

public class Measure {
    private TimeSignature timeSignature;
    private KeySignature keySignature;
    private Barline rightBarline;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();
    private boolean dirty = true;
    private int currentResolutionTicks;

    public Measure(List<Staff> staves) {
        this.staves = staves;
        staves.forEach(staff -> staff.getDefaultClef().setParent(this));
        this.timeSignature = new TimeSignature(4, 4, this);
        this.keySignature = new KeySignature(-2, this);
        this.rightBarline = new Barline(BarlineStyle.FINAL, this);

        this.currentResolutionTicks = NoteType.WHOLE.getTicks();
    }

    public void updateResolutionFromSegments() {
        if (segments.isEmpty()) {
            if (timeSignature != null) {
                this.currentResolutionTicks = timeSignature.getTotalTicks();
            } else {
                this.currentResolutionTicks = NoteType.WHOLE.getTicks();
            }
            return;
        }

        int minSegmentDuration = Integer.MAX_VALUE;

        for (Segment seg : segments) {
            if (seg.getType() == SegmentType.NOTEREST) {
                int segDuration = seg.getDuration();
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
        MeasureNoteInserter.insertNote(this, targetSegment, staff, newNote);
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

    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
    public void setBarlineStyle(BarlineStyle barlineStyle) {
        this.rightBarline.setStyle(barlineStyle);
        setDirty(true);
    }

    public void setTimeSignature(TimeSignature timeSignature) {
        this.timeSignature = timeSignature;
        MeasureTimeSignatureAdjuster.adjustSegmentsToTimeSignature(this);
        updateResolutionFromSegments();
        setDirty(true);
    }

    public void setKeySignature(KeySignature keySignature) {
        this.keySignature = keySignature;
        setDirty(true);
    }

    private NoteType findNoteTypeByTicks(int ticks) {
        for (NoteType type : NoteType.values()) {
            if (type.getTicks() == ticks) return type;
        }
        return MeasureTimeSignatureAdjuster.findLargestFittingNoteType(ticks);
    }
}