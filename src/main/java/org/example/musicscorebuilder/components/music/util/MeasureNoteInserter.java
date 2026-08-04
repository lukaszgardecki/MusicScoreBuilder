package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MeasureNoteInserter {

    public static Segment insertNote(Measure measure, Segment targetSegment, Staff staff, Note newNote) {
        if (targetSegment == null || !targetSegment.isNoteRest()) return null;

        int voice = newNote.getVoice();
        int newTicks = getElementTicks(newNote);

        // 1. Obliczamy pozycję startową docelowego segmentu
        int startTick = measure.getStartTickOfSegment(targetSegment);
        if (startTick < 0) return null;

        int totalMeasureTicks = measure.getTimeSignature().getTotalTicks();
        int availableInMeasure = totalMeasureTicks - startTick;

        int ticksForCurrentMeasure = Math.min(newTicks, availableInMeasure);
        int remainingTicks = newTicks - ticksForCurrentMeasure;

        // Jeśli nuta się nie mieści w bieżącym takcie, sprawdzamy istnienie następnego taktu
        Measure nextMeasure = null;
        if (remainingTicks > 0) {
            nextMeasure = getNextMeasure(measure);
            if (nextMeasure == null) {
                // Brak następnego taktu -> obcinamy nutę do końca bieżącego taktu
                ticksForCurrentMeasure = availableInMeasure;
                remainingTicks = 0;
            }
        }

        // Przygotowujemy grupy nut dla obu taktów
        List<Note> notesM1 = (remainingTicks == 0)
                ? List.of(newNote)
                : createNotesForTicks(newNote, ticksForCurrentMeasure, measure);

        List<Note> notesM2 = (remainingTicks > 0)
                ? createNotesForTicks(newNote, remainingTicks, nextMeasure)
                : Collections.emptyList();

        List<Note> allInsertedNotes = new ArrayList<>();

        // 2. Wstawianie do BIEŻĄCEGO taktu
        int currentStartTick = startTick;
        for (Note n : notesM1) {
            int nTicks = getElementTicks(n);
            ensureSegmentBoundaryAt(measure, currentStartTick);
            ensureSegmentBoundaryAt(measure, currentStartTick + nTicks);

            Segment actualTarget = findSegmentAtTick(measure, currentStartTick);
            if (actualTarget != null) {
                removeCollisions(measure, staff, voice, currentStartTick, currentStartTick + nTicks);
                actualTarget.insertNote(staff, n);
                allInsertedNotes.add(n);
            }
            currentStartTick += nTicks;
        }

        cleanAndFillVoiceGaps(measure);
        measure.setDirty(true);

        // 3. Wstawianie pozostałości do NASTĘPNEGO taktu (od ticku 0)
        int nextMeasureEndTick = 0;
        if (!notesM2.isEmpty() && nextMeasure != null) {
            for (Note n : notesM2) {
                int nTicks = getElementTicks(n);
                ensureSegmentBoundaryAt(nextMeasure, nextMeasureEndTick);
                ensureSegmentBoundaryAt(nextMeasure, nextMeasureEndTick + nTicks);

                Segment actualTarget = findSegmentAtTick(nextMeasure, nextMeasureEndTick);
                if (actualTarget != null) {
                    removeCollisions(nextMeasure, staff, voice, nextMeasureEndTick, nextMeasureEndTick + nTicks);
                    actualTarget.insertNote(staff, n);
                    allInsertedNotes.add(n);
                }
                nextMeasureEndTick += nTicks;
            }

            cleanAndFillVoiceGaps(nextMeasure);
            nextMeasure.setDirty(true);
        }

        // 4. Łączenie podzielonych nut w jeden ciąg za pomocą łuków (Tie)
        for (int i = 0; i < allInsertedNotes.size() - 1; i++) {
            linkNotesWithTie(allInsertedNotes.get(i), allInsertedNotes.get(i + 1));
        }

        // 5. Zwracamy wskaźnik na kolejny wolny segment po wstawionej nucie
        if (nextMeasure != null && !notesM2.isEmpty()) {
            return findNextFreeSegment(nextMeasure, nextMeasureEndTick);
        } else {
            return findNextFreeSegment(measure, currentStartTick);
        }
    }

    private static List<Note> createNotesForTicks(Note templateNote, int totalTicks, Measure targetMeasure) {
        List<NoteType> types = decomposeTicksToNoteTypes(totalTicks);
        List<Note> notes = new ArrayList<>();

        for (NoteType type : types) {
            Note n = createNoteFromTemplate(templateNote, type, targetMeasure);
            notes.add(n);
        }
        return notes;
    }

    private static Note createNoteFromTemplate(Note template, NoteType type, Measure targetMeasure) {
        return new Note(
                template.getVoice(),
                template.getStep(),
                template.getAlter(),
                template.getOctave(),
                type,
                BeamType.NONE,
                0,
                targetMeasure
        );
    }

    private static void linkNotesWithTie(Note note1, Note note2) {
        if (note1 == null || note2 == null) return;
        note1.setTieStart(true);
        note2.setTieStop(true);
    }

    private static Measure getNextMeasure(Measure measure) {
        Score score = ScoreService.getInstance().getScore();
        ScoreMode activeMode = ScoreStateManager.getInstance().getCurrentMode(score);

        if (activeMode != null) {
            List<Measure> measures = activeMode.getMeasures();
            int currentIndex = measures.indexOf(measure);

            if (currentIndex != -1 && currentIndex + 1 < measures.size()) {
                return measures.get(currentIndex + 1);
            }
        }
        return null;
    }

    private static void removeCollisions(Measure measure, Staff staff, int voice, int startTick, int targetEndTick) {
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDur = seg.getDuration();
                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);

                for (NoteRestElement el : new ArrayList<>(nres)) {
                    int elStart = currentTick;
                    int elEnd = currentTick + getElementTicks(el);

                    if (elStart < targetEndTick && elEnd > startTick) {
                        seg.removeNoteRest(staff, el);
                    }
                }
                currentTick += segDur;
            }
        }
    }

    private static void cleanAndFillVoiceGaps(Measure measure) {
        for (Staff staff : measure.getStaves()) {
            for (int voice = 1; voice <= 4; voice++) {
                boolean hasNotes = hasNotesInVoice(measure, staff, voice);
                boolean active = (voice == 1) || hasNotes;

                if (!active) {
                    removeAllElementsInVoice(measure, staff, voice);
                } else {
                    removeAllRestsInVoice(measure, staff, voice);
                    fillGapsForActiveVoice(measure, staff, voice);
                }
            }
        }

        updateBeams(measure);
    }

    private static void updateBeams(Measure measure) {
        for (Staff staff : measure.getStaves()) {
            for (int voice = 1; voice <= 4; voice++) {
                updateBeamsForVoice(measure, staff, voice);
            }
        }
    }

    private static void updateBeamsForVoice(Measure measure, Staff staff, int voice) {
        List<Note> currentGroup = new ArrayList<>();
        int beatTicks = getBeatUnitTicks(measure.getTimeSignature());
        int currentTick = 0;

        for (Segment seg : measure.getSegments()) {
            if (!seg.isNoteRest()) {
                currentTick += seg.getDuration();
                continue;
            }

            List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);
            for (NoteRestElement nre : nres) {
                if (nre instanceof Note note && isBeamable(note)) {
                    if (currentTick > 0 && currentTick % beatTicks == 0 && !currentGroup.isEmpty()) {
                        applyBeamTypeToGroup(currentGroup);
                        currentGroup.clear();
                    }
                    currentGroup.add(note);
                } else {
                    applyBeamTypeToGroup(currentGroup);
                    currentGroup.clear();

                    if (nre instanceof Note nonBeamableNote) {
                        nonBeamableNote.setBeamType(BeamType.NONE);
                    }
                }
            }
            currentTick += seg.getDuration();
        }

        applyBeamTypeToGroup(currentGroup);
    }

    private static int getBeatUnitTicks(TimeSignature ts) {
        if (ts == null) return NoteType.QUARTER.getTicks();

        int num = ts.getBeat();
        int den = ts.getBeatType();

        return switch (den) {
            case 8 -> (num % 3 == 0) ? 3 * NoteType.EIGHTH.getTicks() : NoteType.EIGHTH.getTicks();
            case 4 -> NoteType.QUARTER.getTicks();
            case 2 -> NoteType.HALF.getTicks();
            default -> NoteType.QUARTER.getTicks();
        };
    }

    private static boolean isBeamable(Note note) {
        return note != null && note.getType() != null && note.getType().hasFlag();
    }

    private static void applyBeamTypeToGroup(List<Note> group) {
        if (group.isEmpty()) return;

        if (group.size() == 1) {
            group.getFirst().setBeamType(BeamType.NONE);
        } else {
            for (int i = 0; i < group.size(); i++) {
                if (i == 0) {
                    group.get(i).setBeamType(BeamType.BEGIN);
                } else if (i == group.size() - 1) {
                    group.get(i).setBeamType(BeamType.END);
                } else {
                    group.get(i).setBeamType(BeamType.CONTINUE);
                }
            }
        }
    }

    private static void removeAllRestsInVoice(Measure measure, Staff staff, int voice) {
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);
                for (NoteRestElement el : new ArrayList<>(nres)) {
                    if (el instanceof Rest) {
                        seg.removeNoteRest(staff, el);
                    }
                }
            }
        }
    }

    private static void removeAllElementsInVoice(Measure measure, Staff staff, int voice) {
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);
                for (NoteRestElement el : new ArrayList<>(nres)) {
                    seg.removeNoteRest(staff, el);
                }
            }
        }
    }

    private static void fillGapsForActiveVoice(Measure measure, Staff staff, int voice) {
        int totalTicks = measure.getTimeSignature().getTotalTicks();

        List<int[]> noteIntervals = new ArrayList<>();
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDur = seg.getDuration();
                for (NoteRestElement el : seg.getNoteRestByStaffAndVoice(staff, voice)) {
                    if (el instanceof Note note) {
                        int noteStart = currentTick;
                        int noteEnd = currentTick + getElementTicks(note);
                        noteIntervals.add(new int[]{noteStart, noteEnd});
                    }
                }
                currentTick += segDur;
            }
        }

        noteIntervals.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> mergedNotes = new ArrayList<>();
        for (int[] interval : noteIntervals) {
            if (mergedNotes.isEmpty()) {
                mergedNotes.add(interval);
            } else {
                int[] last = mergedNotes.get(mergedNotes.size() - 1);
                if (interval[0] < last[1]) {
                    last[1] = Math.max(last[1], interval[1]);
                } else {
                    mergedNotes.add(interval);
                }
            }
        }

        List<int[]> gapIntervals = new ArrayList<>();
        int searchStart = 0;
        for (int[] noteInt : mergedNotes) {
            if (noteInt[0] > searchStart) {
                gapIntervals.add(new int[]{searchStart, noteInt[0]});
            }
            searchStart = Math.max(searchStart, noteInt[1]);
        }
        if (searchStart < totalTicks) {
            gapIntervals.add(new int[]{searchStart, totalTicks});
        }

        for (int[] gap : gapIntervals) {
            int gapStart = gap[0];
            int gapEnd = gap[1];
            int gapDuration = gapEnd - gapStart;

            List<NoteType> restTypes = decomposeTicksToNoteTypes(gapDuration).reversed();
            int restStartTick = gapStart;

            for (NoteType restType : restTypes) {
                ensureSegmentBoundaryAt(measure, restStartTick);
                Segment seg = findSegmentAtTick(measure, restStartTick);
                if (seg != null) {
                    seg.addElement(staff, new Rest(voice, restType, measure));
                }
                restStartTick += restType.getTicks();
            }
        }
    }

    private static boolean hasNotesInVoice(Measure measure, Staff staff, int voice) {
        for (Segment seg : measure.getSegments()) {
            for (NoteRestElement nre : seg.getNoteRestByStaffAndVoice(staff, voice)) {
                if (nre instanceof Note) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void ensureSegmentBoundaryAt(Measure measure, int targetTick) {
        if (targetTick <= 0 || targetTick >= measure.getTimeSignature().getTotalTicks()) return;

        List<Segment> segments = measure.getSegments();
        int currentTick = 0;

        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (!seg.isNoteRest()) {
                currentTick += seg.getDuration();
                continue;
            }

            int segDuration = seg.getDuration();
            int segEnd = currentTick + segDuration;

            if (targetTick > currentTick && targetTick < segEnd) {
                int part1Ticks = targetTick - currentTick;
                int part2Ticks = segEnd - targetTick;

                List<NoteType> part2Types = decomposeTicksToNoteTypes(part2Ticks);
                if (part2Types.isEmpty()) return;

                seg.setDuration(part1Ticks);

                int insertIndex = i + 1;
                for (NoteType subType : part2Types) {
                    Segment tailSeg = new Segment(SegmentType.NOTEREST, measure);
                    tailSeg.setDuration(subType.getTicks());
                    segments.add(insertIndex, tailSeg);
                    insertIndex++;
                }
                return;
            }
            currentTick += segDuration;
        }
    }

    private static Segment findSegmentAtTick(Measure measure, int targetTick) {
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                if (currentTick == targetTick) {
                    return seg;
                }
                currentTick += seg.getDuration();
            }
        }
        return null;
    }

    private static List<NoteType> decomposeTicksToNoteTypes(int totalTicks) {
        List<NoteType> result = new ArrayList<>();
        int remaining = totalTicks;
        while (remaining > 0) {
            NoteType fit = MeasureTimeSignatureAdjuster.findLargestFittingNoteType(remaining);
            if (fit == null || fit.getTicks() <= 0) break;
            result.add(fit);
            remaining -= fit.getTicks();
        }
        return result;
    }

    public static Segment findNextFreeSegment(Measure measure, int targetEndTick) {
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                if (currentTick >= targetEndTick) {
                    return seg;
                }
                currentTick += seg.getDuration();
            }
        }

        Measure nextMeasure = getNextMeasure(measure);
        if (nextMeasure != null) {
            for (Segment seg : nextMeasure.getSegments()) {
                if (seg.isNoteRest()) {
                    return seg;
                }
            }
        }

        return null;
    }

    private static int getElementTicks(NoteRestElement element) {
        if (element == null || element.getType() == null) return 0;
        int baseTicks = element.getType().getTicks();
        int dots = element.getDots();
        int totalTicks = baseTicks;
        int currentDotValue = baseTicks;

        for (int i = 0; i < dots; i++) {
            currentDotValue /= 2;
            totalTicks += currentDotValue;
        }
        return totalTicks;
    }
}