package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.example.musicscorebuilder.components.music.util.MeasureDurationEditor;
import org.example.musicscorebuilder.components.music.util.MeasureTimeSignatureAdjuster;
import org.example.musicscorebuilder.data.MeasureSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonSerialize(using = MeasureSerializer.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Measure {

    private TimeSignature timeSignature;
    private KeySignature keySignature;
    private Barline rightBarline;
    private boolean systemBreak = false;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();
    @JsonIgnore private Measure prev;
    @JsonIgnore private Measure next;
    @JsonIgnore private ScoreMode parentMode;
    @JsonIgnore private boolean dirty = true;

    @JsonCreator
    public Measure(
            @JsonProperty("staves") List<Staff> staves,
            @JsonProperty("timeSig") TimeSignature timeSignature,
            @JsonProperty("keySig") KeySignature keySignature,
            @JsonProperty("barline") Barline rightBarline,
            @JsonProperty("segs") List<Segment> segments,
            @JsonProperty("systemBreak") Boolean systemBreak
    ) {
        this.staves = staves != null ? staves : new ArrayList<>();
        this.timeSignature = timeSignature;
        this.keySignature = keySignature;
        this.rightBarline = rightBarline != null ? rightBarline : new Barline(BarlineStyle.SINGLE, this);
        if (segments != null) {
            this.segments.addAll(segments);
        }
        this.systemBreak = systemBreak != null && systemBreak;
    }

    public Measure(List<Staff> staves) {
        this.staves = staves;
        staves.forEach(staff -> staff.getDefaultClef().setParent(this));
        this.timeSignature = new TimeSignature(4, 4, TimeSignature.Type.FRACTIONAL, this);
        this.keySignature = new KeySignature(0, this);
        this.rightBarline = new Barline(BarlineStyle.FINAL, this);
    }

    public void recalculateSegmentDurations() {
        TimeSignature ts = getTimeSignature();
        if (ts == null) return;

        int totalTicks = ts.getTotalTicks();

        List<Segment> noteRestSegments = segments.stream()
                .filter(Segment::isNoteRest)
                .collect(Collectors.toList());

        if (noteRestSegments.isEmpty()) return;
        int defaultDur = totalTicks / noteRestSegments.size();

        for (Segment seg : noteRestSegments) {
            int d = seg.getDuration();
            if (d <= 0) {
                seg.setDuration(defaultDur);
            }
        }
    }

    public void changeElementDuration(Segment targetSegment, int staffId, NoteRestElement elementToChange, NoteType newType) {
        MeasureDurationEditor.changeElementDuration(this, targetSegment, staffId, elementToChange, newType);
        setDirty(true);
    }

    public void convertNoteToRest(Segment targetSegment, int staffId, Note noteToConvert) {
        if (targetSegment == null || noteToConvert == null) return;

        if (noteToConvert.isBeamed()) {
            adjustBeamsOnNoteRemoval(targetSegment, staffId, noteToConvert);
        }

        Rest newRest = new Rest(noteToConvert.getVoice(), noteToConvert.getType(), this);
        newRest.setDots(noteToConvert.getDots());

        targetSegment.replaceElement(staffId, noteToConvert, newRest);
        setDirty(true);
    }

    public void convertStaffToWholeRest(int staffId) {
        for (Segment seg : segments) {
            seg.clearStaff(staffId);
        }
        segments.removeIf(Segment::isEmpty);

        if (segments.isEmpty()) {
            Segment newSegment = new Segment(SegmentType.NOTEREST, this);
            segments.add(newSegment);
        }

        Segment firstSegment = segments.get(0);
        Rest wholeRest = new Rest(1, NoteType.WHOLE, this);
        firstSegment.addElement(staffId, wholeRest);

        recalculateSegmentDurations();
        setDirty(true);
    }

    public int countVoicesByStaff(int staffId) {
        return segments.stream()
                .mapToInt(s -> s.getVoiceCountByStaff(staffId))
                .max()
                .orElse(0);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
    public Barline getRightBarline() {
        if (rightBarline == null) {
            rightBarline = new Barline(BarlineStyle.SINGLE, this);
        }
        return rightBarline;
    }
    public TimeSignature getTimeSignature() {
        if (this.timeSignature != null) {
            return this.timeSignature;
        }
        if (prev != null) {
            TimeSignature prevTs = prev.getTimeSignature();
            if (prevTs != null) {
                return new TimeSignature(
                        prevTs.getNominalBeat(),
                        prevTs.getNominalBeatType(),
                        prevTs.getNominalBeat(),     // actualBeat = nominalBeat
                        prevTs.getNominalBeatType(), // actualBeatType = nominalBeatType
                        prevTs.getType(),
                        this
                );
            }
        }
        return new TimeSignature(4, 4, TimeSignature.Type.FRACTIONAL, this);
    }
    public TimeSignature getExplicitTimeSignature() {
        return this.timeSignature;
    }
    public KeySignature getKeySignature() { return keySignature; }
    @JsonIgnore public BarlineStyle getBarlineStyle() { return getRightBarline().getStyle(); }
    @JsonIgnore public boolean isDirty() { return dirty; }
    @JsonIgnore public Measure getPrev() { return prev; }
    @JsonIgnore public Measure getNext() { return next; }
    @JsonIgnore public ScoreMode getParentMode() { return parentMode; }
    public void setParentMode(ScoreMode parentMode) { this.parentMode = parentMode; }
    public int getIndex() { return parentMode.getMeasures().indexOf(this); }
    public boolean hasSystemBreak() { return systemBreak; }
    public int getKeySignatureAlterForStep(PitchStep step) {
        return keySignature != null ? keySignature.getAlterForStep(step) : 0;
    }
    public int getEffectiveAlterBefore(Segment targetSegment, int staffId, PitchStep step, int octave) {
        int activeAlter = getKeySignatureAlterForStep(step);

        if (targetSegment == null) {
            return activeAlter;
        }

        for (Segment seg : segments) {
            if (seg == targetSegment) break;
            if (!seg.isNoteRest()) continue;

            List<Element> elements = seg.getElementsByStaff(staffId);

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

    public void setPrev(Measure prev) { this.prev = prev; }
    public void setNext(Measure next) { this.next = next; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
    public void setBarlineStyle(BarlineStyle barlineStyle) {
        if (this.rightBarline == null) {
            this.rightBarline = new Barline(barlineStyle, this);
        } else {
            this.rightBarline.setStyle(barlineStyle);
        }
        setDirty(true);
    }

    public void setTimeSignature(TimeSignature timeSignature) {
        setTimeSignature(timeSignature, true);
    }

    public void setTimeSignature(TimeSignature timeSignature, boolean triggerAdjust) {
        this.timeSignature = timeSignature;
        setDirty(true);
        if (triggerAdjust) {
            MeasureTimeSignatureAdjuster.adjustFromMeasure(this);
        }
    }

    public void setKeySignature(KeySignature keySignature) {
        this.keySignature = keySignature;
        setDirty(true);
    }

    public void setSystemBreak(boolean systemBreak) {
        this.systemBreak = systemBreak;
        setDirty(true);
    }

    private void adjustBeamsOnNoteRemoval(Segment targetSegment, int staffId, Note note) {
        BeamType currentBeam = note.getBeam();
        int voice = note.getVoice();

        Note prevNote = findPreviousNoteInVoice(targetSegment, staffId, voice);
        Note nextNote = findNextNoteInVoice(targetSegment, staffId, voice);

        switch (currentBeam) {
            case BEGIN -> {
                if (nextNote != null && nextNote.isBeamed()) {
                    if (nextNote.getBeam() == BeamType.CONTINUE) {
                        nextNote.setBeam(BeamType.BEGIN);
                    } else if (nextNote.getBeam() == BeamType.END) {
                        nextNote.setBeam(BeamType.NONE);
                    }
                }
            }
            case END -> {
                if (prevNote != null && prevNote.isBeamed()) {
                    if (prevNote.getBeam() == BeamType.CONTINUE) {
                        prevNote.setBeam(BeamType.END);
                    } else if (prevNote.getBeam() == BeamType.BEGIN) {
                        prevNote.setBeam(BeamType.NONE);
                    }
                }
            }
            case CONTINUE -> {
                if (prevNote != null && prevNote.isBeamed()) {
                    if (prevNote.getBeam() == BeamType.BEGIN) {
                        prevNote.setBeam(BeamType.NONE);
                    } else if (prevNote.getBeam() == BeamType.CONTINUE) {
                        prevNote.setBeam(BeamType.END);
                    }
                }
                if (nextNote != null && nextNote.isBeamed()) {
                    if (nextNote.getBeam() == BeamType.END) {
                        nextNote.setBeam(BeamType.NONE);
                    } else if (nextNote.getBeam() == BeamType.CONTINUE) {
                        nextNote.setBeam(BeamType.BEGIN);
                    }
                }
            }
            default -> {}
        }
    }

    private Note findPreviousNoteInVoice(Segment targetSegment, int staffId, int voice) {
        int index = segments.indexOf(targetSegment);
        if (index <= 0) return null;

        for (int i = index - 1; i >= 0; i--) {
            Segment seg = segments.get(i);
            if (seg.isNoteRest()) {
                for (Element el : seg.getElementsByStaff(staffId)) {
                    if (el instanceof Note note && note.getVoice() == voice) {
                        return note;
                    }
                }
            }
        }
        return null;
    }

    private Note findNextNoteInVoice(Segment targetSegment, int staffId, int voice) {
        int index = segments.indexOf(targetSegment);
        if (index == -1 || index >= segments.size() - 1) return null;

        for (int i = index + 1; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.isNoteRest()) {
                for (Element el : seg.getElementsByStaff(staffId)) {
                    if (el instanceof Note note && note.getVoice() == voice) {
                        return note;
                    }
                }
            }
        }
        return null;
    }
}