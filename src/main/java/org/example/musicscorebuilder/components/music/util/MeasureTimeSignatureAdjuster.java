package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.*;

import java.util.*;

public class MeasureTimeSignatureAdjuster {

    private static boolean isAdjusting = false;

    public static void adjustFromMeasure(Measure startMeasure) {
        if (startMeasure == null || isAdjusting) return;

        try {
            isAdjusting = true;

            ScoreMode parentMode = startMeasure.getParentMode();

            // 1. Ustal metrum wzorcowe (np. 2/4 = 1920 ticków)
            TimeSignature masterTimeSig = startMeasure.getTimeSignature();
            if (masterTimeSig == null) {
                masterTimeSig = new TimeSignature(2, 4, TimeSignature.Type.FRACTIONAL, startMeasure);
                startMeasure.setTimeSignature(masterTimeSig, false);
            }
            int masterTargetTicks = masterTimeSig.getTotalTicks();

            List<Measure> measures = new ArrayList<>();
            Measure curr = startMeasure;
            while (curr != null) {
                if (curr.getTimeSignature() == null) {
                    curr.setTimeSignature(new TimeSignature(
                            masterTimeSig.getBeat(),
                            masterTimeSig.getBeatType(),
                            masterTimeSig.getType(),
                            curr
                    ), false);
                }
                measures.add(curr);
                curr = curr.getNext();
            }

            Map<StaffVoiceKey, List<NoteData>> streams = extractParallelStreams(measures);

            boolean allEmpty = streams.values().stream().allMatch(List::isEmpty);
            if (allEmpty) {
                for (Measure m : measures) {
                    fillEmptyMeasure(m, masterTargetTicks);
                    m.setDirty(true);
                }
                if (parentMode != null) {
                    parentMode.getSlurs().clear();
                }
                return;
            }

            Map<Note, Note> oldToNewStartMap = new HashMap<>();
            Map<Note, Note> oldToNewEndMap = new HashMap<>();

            for (Measure m : measures) {
                int targetTicks = (m.getTimeSignature() != null) ? m.getTimeSignature().getTotalTicks() : masterTargetTicks;
                fillMeasureParallel(m, streams, targetTicks, oldToNewStartMap, oldToNewEndMap);
                m.setDirty(true);
            }

            while (hasRemainingNotes(streams)) {
                ScoreMode parent = (parentMode != null) ? parentMode : startMeasure.getParentMode();
                if (parent == null) break;

                parent.appendMeasure();
                Measure lastM = parent.getMeasures().getLast();

                lastM.setTimeSignature(new TimeSignature(
                        masterTimeSig.getBeat(),
                        masterTimeSig.getBeatType(),
                        masterTimeSig.getType(),
                        lastM
                ), false);

                fillMeasureParallel(lastM, streams, masterTargetTicks, oldToNewStartMap, oldToNewEndMap);
                lastM.setDirty(true);
            }

            if (parentMode != null && !parentMode.getSlurs().isEmpty()) {
                List<Slur> updatedSlurs = new ArrayList<>();
                for (Slur oldSlur : parentMode.getSlurs()) {
                    Note newStart = oldToNewStartMap.get(oldSlur.getStartNote());
                    Note newEnd = oldToNewEndMap.get(oldSlur.getEndNote());

                    if (newStart != null && newEnd != null) {
                        updatedSlurs.add(new Slur(newStart, newEnd));
                    }
                }
                parentMode.getSlurs().clear();
                parentMode.getSlurs().addAll(updatedSlurs);
            }

            updateBeamsForMeasures(measures);
        } finally {
            isAdjusting = false;
        }
    }

    private static void updateBeamsForMeasures(List<Measure> measures) {
        for (Measure m : measures) {
            updateBeamsForMeasure(m);
        }
    }

    private static void updateBeamsForMeasure(Measure m) {
        TimeSignature ts = m.getTimeSignature();
        int beatType = (ts != null) ? ts.getBeatType() : 4;
        int beats = (ts != null) ? ts.getBeat() : 2;

        int beatTicks = 3840 / beatType;
        if (beatType == 8 && beats % 3 == 0) {
            beatTicks = 1440; // Dla metrum złożonego (np. 6/8, 9/8) grupa obejmuje ćwierćnutę z kropką
        }

        for (Staff staff : m.getStaves()) {
            int staffIdx = staff.getIndex();
            for (int voice = 1; voice <= 4; voice++) {
                updateBeamsForVoice(m, staffIdx, voice, beatTicks);
            }
        }
    }

    private static void updateBeamsForVoice(Measure m, int staffIndex, int voice, int beatTicks) {
        List<NoteTickInfo> items = new ArrayList<>();
        int currentTick = 0;

        for (Segment seg : m.getSegments()) {
            if (!seg.isNoteRest()) continue;

            List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(staffIndex, voice);
            for (NoteRestElement el : elements) {
                if (el instanceof Note note) {
                    items.add(new NoteTickInfo(note, currentTick));
                } else if (el instanceof Rest) {
                    items.add(new NoteTickInfo(null, currentTick));
                }
            }
            currentTick += seg.getDuration();
        }

        List<Note> currentGroup = new ArrayList<>();
        int currentBeatIndex = -1;

        for (NoteTickInfo item : items) {
            if (item.note == null || !isBeamable(item.note.getType())) {
                applyBeamGroup(currentGroup);
                currentGroup.clear();
                currentBeatIndex = -1;
                continue;
            }

            int noteBeatIndex = item.startTick / beatTicks;

            if (currentGroup.isEmpty()) {
                currentGroup.add(item.note);
                currentBeatIndex = noteBeatIndex;
            } else {
                if (noteBeatIndex == currentBeatIndex) {
                    currentGroup.add(item.note);
                } else {
                    applyBeamGroup(currentGroup);
                    currentGroup.clear();
                    currentGroup.add(item.note);
                    currentBeatIndex = noteBeatIndex;
                }
            }
        }

        applyBeamGroup(currentGroup);
    }

    private static void applyBeamGroup(List<Note> group) {
        if (group.isEmpty()) return;

        if (group.size() == 1) {
            group.getFirst().setBeam(BeamType.NONE);
        } else {
            for (int i = 0; i < group.size(); i++) {
                if (i == 0) {
                    group.get(i).setBeam(BeamType.BEGIN);
                } else if (i == group.size() - 1) {
                    group.get(i).setBeam(BeamType.END);
                } else {
                    group.get(i).setBeam(BeamType.CONTINUE);
                }
            }
        }
    }

    private static class NoteTickInfo {
        final Note note;
        final int startTick;

        NoteTickInfo(Note note, int startTick) {
            this.note = note;
            this.startTick = startTick;
        }
    }

    private static class StaffVoiceKey implements Comparable<StaffVoiceKey> {
        final int staffIndex;
        final int voice;

        StaffVoiceKey(int staffIndex, int voice) {
            this.staffIndex = staffIndex;
            this.voice = voice;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof StaffVoiceKey that)) return false;
            return staffIndex == that.staffIndex && voice == that.voice;
        }

        @Override
        public int hashCode() {
            return Objects.hash(staffIndex, voice);
        }

        @Override
        public int compareTo(StaffVoiceKey o) {
            if (this.staffIndex != o.staffIndex) return Integer.compare(this.staffIndex, o.staffIndex);
            return Integer.compare(this.voice, o.voice);
        }
    }

    private static Map<StaffVoiceKey, List<NoteData>> extractParallelStreams(List<Measure> measures) {
        Map<StaffVoiceKey, List<NoteData>> streams = new HashMap<>();

        for (Measure m : measures) {
            for (Segment seg : m.getSegments()) {
                if (!seg.isNoteRest()) continue;

                for (Staff staff : m.getStaves()) {
                    int staffIdx = staff.getIndex();
                    for (int voice = 1; voice <= 4; voice++) {
                        List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staffIdx, voice);
                        for (NoteRestElement el : nres) {
                            StaffVoiceKey key = new StaffVoiceKey(staffIdx, voice);
                            streams.putIfAbsent(key, new ArrayList<>());

                            if (el instanceof Note note) {
                                int ticks = calculateTicks(note.getType(), note.getDots());
                                streams.get(key).add(new NoteData(note, ticks, staffIdx, voice));
                            } else if (el instanceof Rest rest) {
                                int ticks = calculateTicks(rest.getType(), rest.getDots());
                                streams.get(key).add(new NoteData(rest, ticks, staffIdx, voice));
                            }
                        }
                    }
                }
            }
            m.getSegments().removeIf(Segment::isNoteRest);
        }

        for (List<NoteData> stream : streams.values()) {
            while (!stream.isEmpty() && stream.getLast().isRest) {
                stream.removeLast();
            }
        }

        return streams;
    }

    private static boolean hasRemainingNotes(Map<StaffVoiceKey, List<NoteData>> streams) {
        for (List<NoteData> stream : streams.values()) {
            for (NoteData nd : stream) {
                if (!nd.isRest) return true;
            }
        }
        return false;
    }

    private static void fillMeasureParallel(Measure m, Map<StaffVoiceKey, List<NoteData>> streams, int targetTicks,
                                            Map<Note, Note> oldToNewStartMap, Map<Note, Note> oldToNewEndMap) {
        Map<StaffVoiceKey, List<PlacedElement>> placedPerKey = new HashMap<>();
        Set<Integer> tickBoundaries = new TreeSet<>();
        tickBoundaries.add(0);
        tickBoundaries.add(targetTicks);

        for (Map.Entry<StaffVoiceKey, List<NoteData>> entry : streams.entrySet()) {
            StaffVoiceKey key = entry.getKey();
            List<NoteData> stream = entry.getValue();
            List<PlacedElement> placedList = new ArrayList<>();

            int currentTicks = 0;
            while (currentTicks < targetTicks) {
                if (stream.isEmpty()) {
                    int padTicks = targetTicks - currentTicks;
                    placedList.add(new PlacedElement(null, currentTicks, padTicks, key.staffIndex, key.voice, true));
                    tickBoundaries.add(currentTicks);
                    currentTicks = targetTicks;
                    break;
                }

                NoteData nd = stream.getFirst();
                int remainingSpace = targetTicks - currentTicks;
                int fitTicks = Math.min(nd.durationTicks, remainingSpace);

                if (nd.isRest) {
                    placedList.add(new PlacedElement(nd, currentTicks, fitTicks, key.staffIndex, key.voice, true));
                    tickBoundaries.add(currentTicks);

                    if (fitTicks < nd.durationTicks) {
                        nd.durationTicks -= fitTicks;
                    } else {
                        stream.removeFirst();
                    }
                } else {
                    boolean splitNeeded = fitTicks < nd.durationTicks;
                    NoteData activeNd = new NoteData(nd);
                    activeNd.durationTicks = fitTicks;

                    if (splitNeeded) {
                        activeNd.tieStart = true;
                        nd.durationTicks -= fitTicks;
                        nd.tieStop = true;
                    } else {
                        stream.removeFirst();
                    }

                    placedList.add(new PlacedElement(activeNd, currentTicks, fitTicks, key.staffIndex, key.voice, false));
                    tickBoundaries.add(currentTicks);
                }

                currentTicks += fitTicks;
            }

            placedPerKey.put(key, placedList);
        }

        List<Integer> sortedTicks = new ArrayList<>(tickBoundaries);
        for (int i = 0; i < sortedTicks.size() - 1; i++) {
            int segStart = sortedTicks.get(i);
            int segEnd = sortedTicks.get(i + 1);
            int segDuration = segEnd - segStart;

            Segment seg = new Segment(SegmentType.NOTEREST, m);
            seg.setDuration(segDuration);

            for (Map.Entry<StaffVoiceKey, List<PlacedElement>> entry : placedPerKey.entrySet()) {
                StaffVoiceKey key = entry.getKey();
                List<PlacedElement> elements = entry.getValue();

                for (PlacedElement pe : elements) {
                    if (pe.startTick == segStart) {
                        NoteTypeAndDots typeAndDots = resolveExactNoteTypeAndDots(pe.durationTicks);

                        if (pe.isRest) {
                            Rest rest = new Rest(key.voice, typeAndDots.type, m);
                            rest.setDots(typeAndDots.dots);
                            seg.addElement(key.staffIndex, rest);
                        } else {
                            NoteData nd = pe.noteData;

                            Note newNote = new Note(
                                    key.voice,
                                    nd.step,
                                    nd.alter,
                                    nd.octave,
                                    typeAndDots.type,
                                    BeamType.NONE,
                                    typeAndDots.dots,
                                    m
                            );
                            newNote.setTieStart(nd.tieStart);
                            newNote.setTieStop(nd.tieStop);

                            if (nd.originalNote != null) {
                                oldToNewStartMap.putIfAbsent(nd.originalNote, newNote);
                                oldToNewEndMap.put(nd.originalNote, newNote);
                            }

                            seg.addElement(key.staffIndex, newNote);
                        }
                    }
                }
            }

            insertSegmentBeforeBarline(m, seg);
        }
    }

    private static boolean isBeamable(NoteType type) {
        return type == NoteType.EIGHTH || type == NoteType.SIXTEENTH || type == NoteType.THIRTY_SECOND;
    }

    private static void fillEmptyMeasure(Measure measure, int targetTicks) {
        measure.getSegments().removeIf(Segment::isNoteRest);

        Segment padSeg = new Segment(SegmentType.NOTEREST, measure);
        padSeg.setDuration(targetTicks);

        NoteTypeAndDots typeAndDots = resolveExactNoteTypeAndDots(targetTicks);

        for (Staff staff : measure.getStaves()) {
            Rest rest = new Rest(1, typeAndDots.type, measure);
            rest.setDots(typeAndDots.dots);
            padSeg.addElement(staff.getIndex(), rest);
        }

        insertSegmentBeforeBarline(measure, padSeg);
    }

    private static void insertSegmentBeforeBarline(Measure measure, Segment segment) {
        segment.setParent(measure);
        int insertIdx = measure.getSegments().size();
        for (int i = 0; i < measure.getSegments().size(); i++) {
            if (measure.getSegments().get(i).getType() == SegmentType.BARLINE) {
                insertIdx = i;
                break;
            }
        }
        measure.getSegments().add(insertIdx, segment);
    }

    public static int calculateTicks(NoteType type, int dots) {
        if (type == null) return 960;
        int base = type.getTicks();
        if (dots == 1) base += base / 2;
        else if (dots == 2) base += base / 2 + base / 4;
        return base;
    }

    public static NoteTypeAndDots resolveExactNoteTypeAndDots(int ticks) {
        switch (ticks) {
            case 3840: return new NoteTypeAndDots(NoteType.WHOLE, 0);
            case 2880: return new NoteTypeAndDots(NoteType.HALF, 1);
            case 1920: return new NoteTypeAndDots(NoteType.HALF, 0);
            case 1440: return new NoteTypeAndDots(NoteType.QUARTER, 1);
            case 960:  return new NoteTypeAndDots(NoteType.QUARTER, 0);
            case 720:  return new NoteTypeAndDots(NoteType.EIGHTH, 1);
            case 480:  return new NoteTypeAndDots(NoteType.EIGHTH, 0);
            case 360:  return new NoteTypeAndDots(NoteType.SIXTEENTH, 1);
            case 240:  return new NoteTypeAndDots(NoteType.SIXTEENTH, 0);
            case 120:  return new NoteTypeAndDots(NoteType.THIRTY_SECOND, 0);
            default:
                if (ticks >= 3840) return new NoteTypeAndDots(NoteType.WHOLE, 0);
                if (ticks >= 1920) return new NoteTypeAndDots(NoteType.HALF, 0);
                if (ticks >= 960)  return new NoteTypeAndDots(NoteType.QUARTER, 0);
                if (ticks >= 480)  return new NoteTypeAndDots(NoteType.EIGHTH, 0);
                if (ticks >= 240)  return new NoteTypeAndDots(NoteType.SIXTEENTH, 0);
                return new NoteTypeAndDots(NoteType.THIRTY_SECOND, 0);
        }
    }

    public static class NoteTypeAndDots {
        public final NoteType type;
        public final int dots;

        public NoteTypeAndDots(NoteType type, int dots) {
            this.type = type;
            this.dots = dots;
        }
    }

    private static class PlacedElement {
        final NoteData noteData;
        final int startTick;
        final int durationTicks;
        final int staffIndex;
        final int voice;
        final boolean isRest;

        PlacedElement(NoteData noteData, int startTick, int durationTicks, int staffIndex, int voice, boolean isRest) {
            this.noteData = noteData;
            this.startTick = startTick;
            this.durationTicks = durationTicks;
            this.staffIndex = staffIndex;
            this.voice = voice;
            this.isRest = isRest;
        }
    }

    private static class NoteData {
        final boolean isRest;
        final PitchStep step;
        final int alter;
        final int octave;
        final int voice;
        final BeamType beam;
        final Note originalNote;
        boolean tieStart;
        boolean tieStop;
        int durationTicks;
        final int staffIndex;

        NoteData(Note note, int durationTicks, int staffIndex, int voice) {
            this.isRest = false;
            Pitch p = note.getPitch();
            this.step = (p != null) ? p.getStep() : PitchStep.C;
            this.alter = (p != null) ? p.getAlter() : 0;
            this.octave = (p != null) ? p.getOctave() : 4;
            this.voice = voice;
            this.beam = note.getBeam();
            this.tieStart = note.isTieStart();
            this.tieStop = note.isTieStop();
            this.originalNote = note;
            this.durationTicks = durationTicks;
            this.staffIndex = staffIndex;
        }

        NoteData(Rest rest, int durationTicks, int staffIndex, int voice) {
            this.isRest = true;
            this.step = PitchStep.C;
            this.alter = 0;
            this.octave = 4;
            this.voice = voice;
            this.beam = null;
            this.tieStart = false;
            this.tieStop = false;
            this.originalNote = null;
            this.durationTicks = durationTicks;
            this.staffIndex = staffIndex;
        }

        NoteData(NoteData copy) {
            this.isRest = copy.isRest;
            this.step = copy.step;
            this.alter = copy.alter;
            this.octave = copy.octave;
            this.voice = copy.voice;
            this.beam = copy.beam;
            this.tieStart = copy.tieStart;
            this.tieStop = copy.tieStop;
            this.originalNote = copy.originalNote;
            this.durationTicks = copy.durationTicks;
            this.staffIndex = copy.staffIndex;
        }
    }
}