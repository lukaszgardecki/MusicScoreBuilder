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

    public void convertNoteToRest(Segment targetSegment, Staff staff, Note noteToConvert) {
        if (targetSegment == null || staff == null || noteToConvert == null) return;

        if (noteToConvert.isBeamed()) {
            adjustBeamsOnNoteRemoval(targetSegment, staff, noteToConvert);
        }

        Rest newRest = new Rest(noteToConvert.getVoice(), noteToConvert.getType(), this);
        newRest.setDots(noteToConvert.getDots());

        targetSegment.replaceElement(staff, noteToConvert, newRest);
        setDirty(true);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
    public Barline getRightBarline() { return rightBarline; }
    public BarlineStyle getBarlineStyle() { return rightBarline.getStyle(); }
    public TimeSignature getTimeSignature() { return timeSignature; }
    public KeySignature getKeySignature() { return keySignature; }
    public int getKeySignatureAlterForStep(PitchStep step) {
        return keySignature != null ? keySignature.getAlterForStep(step) : 0;
    }

    public int getEffectiveAlterBefore(Segment targetSegment, Staff staff, PitchStep step, int octave) {
        int activeAlter = getKeySignatureAlterForStep(step);

        if (targetSegment == null) {
            return activeAlter;
        }


        for (Segment seg : segments) {
            if (seg == targetSegment) break;
            if (!seg.isNoteRest()) continue;

            List<Element> elements = seg.getElementsByStaff(staff);

            for (Element el : elements) {
                if (el instanceof Note prevNote) {
                    Pitch prevPitch = prevNote.getPitch();
                    if (prevPitch != null
                            && prevPitch.getStep() == step
                            && prevPitch.getOctave() == octave) {
                        activeAlter = prevPitch.getAlter();
                    }
                }
            }
        }

        return activeAlter;
    }

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

    private void adjustBeamsOnNoteRemoval(Segment targetSegment, Staff staff, Note note) {
        BeamType currentBeam = note.getBeam();
        int voice = note.getVoice();

        Note prevNote = findPreviousNoteInVoice(targetSegment, staff, voice);
        Note nextNote = findNextNoteInVoice(targetSegment, staff, voice);

        switch (currentBeam) {
            case BEGIN -> {
                if (nextNote != null && nextNote.isBeamed()) {
                    if (nextNote.getBeam() == BeamType.CONTINUE) {
                        nextNote.setBeamType(BeamType.BEGIN);
                    } else if (nextNote.getBeam() == BeamType.END) {
                        nextNote.setBeamType(BeamType.NONE);
                    }
                }
            }
            case END -> {
                if (prevNote != null && prevNote.isBeamed()) {
                    if (prevNote.getBeam() == BeamType.CONTINUE) {
                        prevNote.setBeamType(BeamType.END);
                    } else if (prevNote.getBeam() == BeamType.BEGIN) {
                        prevNote.setBeamType(BeamType.NONE);
                    }
                }
            }
            case CONTINUE -> {
                if (prevNote != null && prevNote.isBeamed()) {
                    if (prevNote.getBeam() == BeamType.BEGIN) {
                        prevNote.setBeamType(BeamType.NONE);
                    } else if (prevNote.getBeam() == BeamType.CONTINUE) {
                        prevNote.setBeamType(BeamType.END);
                    }
                }
                if (nextNote != null && nextNote.isBeamed()) {
                    if (nextNote.getBeam() == BeamType.END) {
                        nextNote.setBeamType(BeamType.NONE);
                    } else if (nextNote.getBeam() == BeamType.CONTINUE) {
                        nextNote.setBeamType(BeamType.BEGIN);
                    }
                }
            }
            default -> {}
        }
    }

    private Note findPreviousNoteInVoice(Segment targetSegment, Staff staff, int voice) {
        int index = segments.indexOf(targetSegment);
        if (index <= 0) return null;

        for (int i = index - 1; i >= 0; i--) {
            Segment seg = segments.get(i);
            if (seg.isNoteRest()) {
                for (Element el : seg.getElementsByStaff(staff)) {
                    if (el instanceof Note note && note.getVoice() == voice) {
                        return note;
                    }
                }
            }
        }
        return null;
    }

    private Note findNextNoteInVoice(Segment targetSegment, Staff staff, int voice) {
        int index = segments.indexOf(targetSegment);
        if (index == -1 || index >= segments.size() - 1) return null;

        for (int i = index + 1; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.isNoteRest()) {
                for (Element el : seg.getElementsByStaff(staff)) {
                    if (el instanceof Note note && note.getVoice() == voice) {
                        return note;
                    }
                }
            }
        }
        return null;
    }
}