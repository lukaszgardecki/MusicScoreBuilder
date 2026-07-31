package org.example.musicscorebuilder.components.music;

import org.example.musicscorebuilder.components.music.util.MeasureDurationEditor;
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

    public Measure(List<Staff> staves) {
        this.staves = staves;
        staves.forEach(staff -> staff.getDefaultClef().setParent(this));
        this.timeSignature = new TimeSignature(4, 4, TimeSignature.Type.FRACTIONAL, this);
        this.keySignature = new KeySignature(-2, this);
        this.rightBarline = new Barline(BarlineStyle.FINAL, this);
    }

    public void recalculateSegmentDurations() {
        int totalTicks = timeSignature.getTotalTicks();

        List<Segment> noteRestSegments = segments.stream()
                .filter(Segment::isNoteRest)
                .toList();

        if (noteRestSegments.isEmpty()) return;
        int defaultDur = totalTicks / noteRestSegments.size();

        for (Segment seg : noteRestSegments) {
            int d = seg.getDuration();
            if (d <= 0) {
                seg.setDuration(defaultDur);
                d = defaultDur;
            }
        }
    }

    public void changeElementDuration(Segment targetSegment, Staff staff, NoteRestElement elementToChange, NoteType newType) {
        MeasureDurationEditor.changeElementDuration(this, targetSegment, staff, elementToChange, newType);
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
    public int getStartTickOfSegment(Segment target) {
        int tick = 0;
        for (Segment seg : getSegments()) {
            if (seg == target) {
                return tick;
            }
            if (seg.getType() == SegmentType.NOTEREST) {
                tick += seg.getDuration();
            }
        }
        return -1;
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
        recalculateSegmentDurations();
        setDirty(true);
    }

    public void setKeySignature(KeySignature keySignature) {
        this.keySignature = keySignature;
        setDirty(true);
    }
}