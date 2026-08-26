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

            Map<Integer, List<NoteData>> streams = new HashMap<>();
            Map<Note, Note> oldToNewStartMap = new HashMap<>();
            Map<Note, Note> oldToNewEndMap = new HashMap<>();

            Measure curr = startMeasure;
            List<Measure> processedMeasures = new ArrayList<>();

            while (curr != null) {
                if (curr.getTimeSignature() == null) {
                    curr.setTimeSignature(new TimeSignature(
                            masterTimeSig.getBeat(),
                            masterTimeSig.getBeatType(),
                            masterTimeSig.getType(),
                            curr
                    ), false);
                }

                int targetTicks = (curr.getTimeSignature() != null) ? curr.getTimeSignature().getTotalTicks() : masterTargetTicks;

                extractStreamsFromSingleMeasure(curr, streams);
                trimTrailingRests(streams);
                ensureAllStavesHaveVoice1(curr, streams);
                fillMeasureParallelOptimized(curr, streams, targetTicks, oldToNewStartMap, oldToNewEndMap);
                curr.setDirty(true);
                processedMeasures.add(curr);

                if (!hasRemainingNotes(streams)) {
                    break;
                }

                curr = curr.getNext();
            }

            while (hasRemainingNotes(streams)) {
                ScoreMode parent = (parentMode != null) ? parentMode : startMeasure.getParentMode();
                if (parent == null) break;

                parent.appendMeasure();
                Measure lastM = parent.getMeasures().get(parent.getMeasures().size() - 1);

                lastM.setTimeSignature(new TimeSignature(
                        masterTimeSig.getBeat(),
                        masterTimeSig.getBeatType(),
                        masterTimeSig.getType(),
                        lastM
                ), false);

                trimTrailingRests(streams);
                ensureAllStavesHaveVoice1(lastM, streams);
                fillMeasureParallelOptimized(lastM, streams, masterTargetTicks, oldToNewStartMap, oldToNewEndMap);
                lastM.setDirty(true);
                processedMeasures.add(lastM);
            }

            if (parentMode != null && !parentMode.getSlurs().isEmpty() && !oldToNewStartMap.isEmpty()) {
                List<Slur> slurs = parentMode.getSlurs();
                for (int i = 0; i < slurs.size(); i++) {
                    Slur oldSlur = slurs.get(i);
                    Note newStart = oldToNewStartMap.get(oldSlur.getStartNote());
                    Note newEnd = oldToNewEndMap.get(oldSlur.getEndNote());

                    if (newStart != null && newEnd != null) {
                        slurs.set(i, new Slur(newStart, newEnd));
                    }
                }
            }

            for (int i = 0; i < processedMeasures.size(); i++) {
                updateBeamsForMeasure(processedMeasures.get(i));
            }

        } finally {
            isAdjusting = false;
        }
    }

    private static void extractStreamsFromSingleMeasure(Measure m, Map<Integer, List<NoteData>> streams) {
        List<Segment> segments = m.getSegments();
        List<Staff> staves = m.getStaves();

        for (int sg = 0; sg < segments.size(); sg++) {
            Segment seg = segments.get(sg);
            if (!seg.isNoteRest()) continue;

            for (int st = 0; st < staves.size(); st++) {
                int staffIdx = staves.get(st).getIndex();
                for (int voice = 1; voice <= 4; voice++) {
                    List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staffIdx, voice);
                    if (nres.isEmpty()) continue;

                    int key = (staffIdx << 4) | voice;
                    List<NoteData> stream = streams.computeIfAbsent(key, k -> new ArrayList<>());

                    for (int e = 0; e < nres.size(); e++) {
                        NoteRestElement el = nres.get(e);
                        if (el instanceof Note note) {
                            int ticks = calculateTicks(note.getType(), note.getDots());
                            stream.add(new NoteData(note, ticks, staffIdx, voice));
                        } else if (el instanceof Rest rest) {
                            int ticks = calculateTicks(rest.getType(), rest.getDots());
                            stream.add(new NoteData(rest, ticks, staffIdx, voice));
                        }
                    }
                }
            }
        }

        segments.removeIf(Segment::isNoteRest);
    }

    private static void trimTrailingRests(Map<Integer, List<NoteData>> streams) {
        for (List<NoteData> stream : streams.values()) {
            while (!stream.isEmpty() && stream.get(stream.size() - 1).isRest) {
                stream.remove(stream.size() - 1);
            }
        }
    }

    private static void ensureAllStavesHaveVoice1(Measure m, Map<Integer, List<NoteData>> streams) {
        List<Staff> staves = m.getStaves();
        for (int i = 0; i < staves.size(); i++) {
            int staffIdx = staves.get(i).getIndex();
            int key = (staffIdx << 4) | 1;
            streams.computeIfAbsent(key, k -> new ArrayList<>());
        }
    }

    private static boolean hasRemainingNotes(Map<Integer, List<NoteData>> streams) {
        for (List<NoteData> stream : streams.values()) {
            for (int i = 0; i < stream.size(); i++) {
                if (!stream.get(i).isRest) return true;
            }
        }
        return false;
    }

    private static void fillMeasureParallelOptimized(
            Measure m,
            Map<Integer, List<NoteData>> streams,
            int targetTicks,
            Map<Note, Note> oldToNewStartMap,
            Map<Note, Note> oldToNewEndMap
    ) {
        Map<Integer, List<PlacedElement>> placedPerKey = new HashMap<>();
        List<Integer> tickBoundaries = new ArrayList<>(16);
        addUniqueSorted(tickBoundaries, 0);
        addUniqueSorted(tickBoundaries, targetTicks);

        for (Map.Entry<Integer, List<NoteData>> entry : streams.entrySet()) {
            int key = entry.getKey();
            int staffIdx = key >> 4;
            int voice = key & 0xF;
            List<NoteData> stream = entry.getValue();
            List<PlacedElement> placedList = new ArrayList<>();

            int currentTicks = 0;
            while (currentTicks < targetTicks) {
                if (stream.isEmpty()) {
                    int padTicks = targetTicks - currentTicks;
                    if (padTicks > 0) {
                        placedList.add(new PlacedElement(null, currentTicks, padTicks, staffIdx, voice, true));
                        addUniqueSorted(tickBoundaries, currentTicks);
                    }
                    currentTicks = targetTicks;
                    break;
                }

                NoteData nd = stream.get(0);
                int remainingSpace = targetTicks - currentTicks;
                int fitTicks = Math.min(nd.durationTicks, remainingSpace);

                if (nd.isRest) {
                    placedList.add(new PlacedElement(nd, currentTicks, fitTicks, staffIdx, voice, true));
                    addUniqueSorted(tickBoundaries, currentTicks);

                    if (fitTicks < nd.durationTicks) {
                        nd.durationTicks -= fitTicks;
                    } else {
                        stream.remove(0);
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
                        stream.remove(0);
                    }

                    placedList.add(new PlacedElement(activeNd, currentTicks, fitTicks, staffIdx, voice, false));
                    addUniqueSorted(tickBoundaries, currentTicks);
                }

                currentTicks += fitTicks;
            }

            placedPerKey.put(key, placedList);
        }

        for (int i = 0; i < tickBoundaries.size() - 1; i++) {
            int segStart = tickBoundaries.get(i);
            int segEnd = tickBoundaries.get(i + 1);
            int segDuration = segEnd - segStart;

            Segment seg = new Segment(SegmentType.NOTEREST, m);
            seg.setDuration(segDuration);

            for (Map.Entry<Integer, List<PlacedElement>> entry : placedPerKey.entrySet()) {
                int key = entry.getKey();
                int staffIdx = key >> 4;
                int voice = key & 0xF;
                List<PlacedElement> elements = entry.getValue();

                for (int peIdx = 0; peIdx < elements.size(); peIdx++) {
                    PlacedElement pe = elements.get(peIdx);
                    if (pe.startTick == segStart) {
                        NoteTypeAndDots typeAndDots = resolveExactNoteTypeAndDots(pe.durationTicks);

                        if (pe.isRest) {
                            Rest rest = new Rest(voice, typeAndDots.type, m);
                            rest.setDots(typeAndDots.dots);
                            seg.addElement(staffIdx, rest);
                        } else {
                            NoteData nd = pe.noteData;

                            Note newNote = new Note(
                                    voice,
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

                            seg.addElement(staffIdx, newNote);
                        }
                    }
                }
            }

            insertSegmentBeforeBarline(m, seg);
        }
    }

    private static void addUniqueSorted(List<Integer> list, int val) {
        for (int i = 0; i < list.size(); i++) {
            int item = list.get(i);
            if (item == val) return;
            if (item > val) {
                list.add(i, val);
                return;
            }
        }
        list.add(val);
    }

    private static void updateBeamsForMeasure(Measure m) {
        TimeSignature ts = m.getTimeSignature();
        int beatType = (ts != null) ? ts.getBeatType() : 4;
        int beats = (ts != null) ? ts.getBeat() : 2;

        int beatTicks = 3840 / beatType;
        if (beatType == 8 && beats % 3 == 0) {
            beatTicks = 1440;
        }

        List<Staff> staves = m.getStaves();
        for (int s = 0; s < staves.size(); s++) {
            int staffIdx = staves.get(s).getIndex();
            for (int voice = 1; voice <= 4; voice++) {
                updateBeamsForVoice(m, staffIdx, voice, beatTicks);
            }
        }
    }

    private static void updateBeamsForVoice(Measure m, int staffIndex, int voice, int beatTicks) {
        List<NoteTickInfo> items = new ArrayList<>();
        int currentTick = 0;

        List<Segment> segments = m.getSegments();
        for (int s = 0; s < segments.size(); s++) {
            Segment seg = segments.get(s);
            if (!seg.isNoteRest()) continue;

            List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(staffIndex, voice);
            if (elements != null) {
                for (int e = 0; e < elements.size(); e++) {
                    NoteRestElement el = elements.get(e);
                    if (el instanceof Note note) {
                        items.add(new NoteTickInfo(note, currentTick));
                    } else if (el instanceof Rest) {
                        items.add(new NoteTickInfo(null, currentTick));
                    }
                }
            }
            currentTick += seg.getDuration();
        }

        if (items.isEmpty()) return;

        List<Note> currentGroup = new ArrayList<>();
        int currentBeatIndex = -1;

        for (int i = 0; i < items.size(); i++) {
            NoteTickInfo item = items.get(i);
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
            group.get(0).setBeam(BeamType.NONE);
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

    private static boolean isBeamable(NoteType type) {
        return type == NoteType.EIGHTH || type == NoteType.SIXTEENTH || type == NoteType.THIRTY_SECOND;
    }

    private static void insertSegmentBeforeBarline(Measure measure, Segment segment) {
        segment.setParent(measure);
        List<Segment> segs = measure.getSegments();
        int insertIdx = segs.size();
        for (int i = 0; i < segs.size(); i++) {
            if (segs.get(i).getType() == SegmentType.BARLINE) {
                insertIdx = i;
                break;
            }
        }
        segs.add(insertIdx, segment);
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

    private static class NoteTickInfo {
        final Note note;
        final int startTick;

        NoteTickInfo(Note note, int startTick) {
            this.note = note;
            this.startTick = startTick;
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