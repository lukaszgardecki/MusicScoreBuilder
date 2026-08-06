package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MeasureDurationEditor {

    private static class TieInfo {
        boolean tieStop = false;
        boolean tieStart = false;
    }

    public static void changeElementDuration(Measure measure, Segment targetSegment, int staffId, NoteRestElement elementToChange, NoteType newType) {
        if (targetSegment == null || targetSegment.getType() != SegmentType.NOTEREST) return;
        if (elementToChange == null || elementToChange.getType() == newType) return;

        int voice = elementToChange.getVoice();
        int newTicks = newType.getTicks();

        int startTick = getStartTickOfSegment(measure, targetSegment);
        if (startTick < 0) return;

        int totalMeasureTicks = measure.getTimeSignature().getTotalTicks();
        int availableInMeasure = totalMeasureTicks - startTick;

        int ticksForCurrentMeasure = Math.min(newTicks, availableInMeasure);
        int remainingTicks = newTicks - ticksForCurrentMeasure;

        Measure nextMeasure = null;
        if (remainingTicks > 0) {
            nextMeasure = getNextMeasure(measure);
            if (nextMeasure == null) {
                // Brak następnego taktu -> obcinamy wartość do końca bieżącego taktu
                ticksForCurrentMeasure = availableInMeasure;
                remainingTicks = 0;
            }
        }

        // 1. Odczytujemy informacje o łukach z zastępowanych obszarów przed ich usunięciem
        TieInfo tieInfoM1 = collectTieInfo(measure, staffId, voice, startTick, startTick + ticksForCurrentMeasure);
        TieInfo tieInfoM2 = (remainingTicks > 0 && nextMeasure != null)
                ? collectTieInfo(nextMeasure, staffId, voice, 0, remainingTicks)
                : null;

        boolean entryTieStop = tieInfoM1.tieStop;
        boolean exitTieStart = (tieInfoM2 != null) ? tieInfoM2.tieStart : tieInfoM1.tieStart;

        // 2. Przygotowujemy elementy do wstawienia
        List<NoteRestElement> elementsM1 = createElementsForTicks(elementToChange, ticksForCurrentMeasure, measure);
        List<NoteRestElement> elementsM2 = (remainingTicks > 0 && nextMeasure != null)
                ? createElementsForTicks(elementToChange, remainingTicks, nextMeasure)
                : Collections.emptyList();

        List<NoteRestElement> allInsertedElements = new ArrayList<>();

        // 3. Aktualizacja BIEŻĄCEGO taktu
        int currentStartTick = startTick;
        for (NoteRestElement el : elementsM1) {
            int elTicks = getElementTicks(el);
            ensureSegmentBoundaryAt(measure, currentStartTick);
            ensureSegmentBoundaryAt(measure, currentStartTick + elTicks);

            Segment actualTarget = findSegmentAtTick(measure, currentStartTick);
            if (actualTarget != null) {
                removeCollisions(measure, staffId, voice, currentStartTick, currentStartTick + elTicks);
                actualTarget.addElement(staffId, el);
                allInsertedElements.add(el);
            }
            currentStartTick += elTicks;
        }

        cleanAndFillVoiceGaps(measure);
        measure.setDirty(true);

        // 4. Aktualizacja NASTĘPNEGO taktu (jeśli element wykraczał poza bieżący)
        int nextMeasureEndTick = 0;
        if (!elementsM2.isEmpty() && nextMeasure != null) {
            for (NoteRestElement el : elementsM2) {
                int elTicks = getElementTicks(el);
                ensureSegmentBoundaryAt(nextMeasure, nextMeasureEndTick);
                ensureSegmentBoundaryAt(nextMeasure, nextMeasureEndTick + elTicks);

                Segment actualTarget = findSegmentAtTick(nextMeasure, nextMeasureEndTick);
                if (actualTarget != null) {
                    removeCollisions(nextMeasure, staffId, voice, nextMeasureEndTick, nextMeasureEndTick + elTicks);
                    actualTarget.addElement(staffId, el);
                    allInsertedElements.add(el);
                }
                nextMeasureEndTick += elTicks;
            }

            cleanAndFillVoiceGaps(nextMeasure);
            nextMeasure.setDirty(true);
        }

        // 5. Przypisanie poprawnych flag łuków na brzegach nowego ciągu nut
        if (!allInsertedElements.isEmpty()) {
            if (allInsertedElements.getFirst() instanceof Note firstNote) {
                firstNote.setTieStop(entryTieStop);
            }
            if (allInsertedElements.getLast() instanceof Note lastNote) {
                lastNote.setTieStart(exitTieStart);
            }
        }

        // 6. Łączenie wewnętrzne (jeśli zmiana wartości rozbiła nutę na kilka mniejszych)
        for (int i = 0; i < allInsertedElements.size() - 1; i++) {
            if (allInsertedElements.get(i) instanceof Note n1 && allInsertedElements.get(i + 1) instanceof Note n2) {
                linkNotesWithTie(n1, n2);
            }
        }
    }

    /**
     * Zbiera stan łuków z nut, które zostaną nadpisane/usunięte w danym przedziale czasowym.
     */
    private static TieInfo collectTieInfo(Measure measure, int staffId, int voice, int startTick, int targetEndTick) {
        TieInfo info = new TieInfo();
        List<Note> notesInRange = new ArrayList<>();

        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDur = seg.getDuration();
                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staffId, voice);

                for (NoteRestElement el : nres) {
                    if (el instanceof Note note) {
                        int elStart = currentTick;
                        int elEnd = currentTick + getElementTicks(note);

                        if (elStart < targetEndTick && elEnd > startTick) {
                            notesInRange.add(note);
                        }
                    }
                }
                currentTick += segDur;
            }
        }

        if (!notesInRange.isEmpty()) {
            info.tieStop = notesInRange.getFirst().isTieStop();
            info.tieStart = notesInRange.getLast().isTieStart();
        }

        return info;
    }

    private static List<NoteRestElement> createElementsForTicks(NoteRestElement template, int totalTicks, Measure targetMeasure) {
        List<NoteType> types = decomposeTicksToNoteTypes(totalTicks);
        List<NoteRestElement> elements = new ArrayList<>();

        for (NoteType type : types) {
            if (template instanceof Note noteTemplate) {
                Note n = new Note(
                        noteTemplate.getVoice(),
                        noteTemplate.getStep(),
                        noteTemplate.getAlter(),
                        noteTemplate.getOctave(),
                        type,
                        BeamType.NONE,
                        0,
                        targetMeasure
                );
                elements.add(n);
            } else if (template instanceof Rest restTemplate) {
                Rest r = new Rest(restTemplate.getVoice(), type, targetMeasure);
                elements.add(r);
            }
        }
        return elements;
    }

    private static void linkNotesWithTie(Note note1, Note note2) {
        if (note1 == null || note2 == null) return;
        note1.setTieStart(true);
        note2.setTieStop(true);
    }

    private static int getStartTickOfSegment(Measure measure, Segment targetSegment) {
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg == targetSegment) return currentTick;
            if (seg.isNoteRest()) {
                currentTick += seg.getDuration();
            }
        }
        return -1;
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

    private static void removeCollisions(Measure measure, int staffId, int voice, int startTick, int targetEndTick) {
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDur = seg.getDuration();
                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staffId, voice);

                for (NoteRestElement el : new ArrayList<>(nres)) {
                    int elStart = currentTick;
                    int elEnd = currentTick + getElementTicks(el);

                    if (elStart < targetEndTick && elEnd > startTick) {
                        seg.removeNoteRest(staffId, el);
                    }
                }
                currentTick += segDur;
            }
        }
    }

    private static void cleanAndFillVoiceGaps(Measure measure) {
        for (Staff staff : measure.getStaves()) {
            int staffId = staff.getIndex();
            for (int voice = 1; voice <= 4; voice++) {
                boolean hasElements = hasElementsInVoice(measure, staffId, voice);
                boolean active = (voice == 1) || hasElements;

                if (!active) {
                    removeAllElementsInVoice(measure, staffId, voice);
                } else {
                    fillGapsForActiveVoice(measure, staffId, voice);
                }
            }
        }

        updateBeams(measure);
    }

    private static void fillGapsForActiveVoice(Measure measure, int staffId, int voice) {
        int totalTicks = measure.getTimeSignature().getTotalTicks();

        List<int[]> occupiedIntervals = new ArrayList<>();
        int currentTick = 0;

        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDur = seg.getDuration();
                for (NoteRestElement el : seg.getNoteRestByStaffAndVoice(staffId, voice)) {
                    int elStart = currentTick;
                    int elEnd = currentTick + getElementTicks(el);
                    occupiedIntervals.add(new int[]{elStart, elEnd});
                }
                currentTick += segDur;
            }
        }

        occupiedIntervals.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> mergedOccupied = new ArrayList<>();
        for (int[] interval : occupiedIntervals) {
            if (mergedOccupied.isEmpty()) {
                mergedOccupied.add(interval);
            } else {
                int[] last = mergedOccupied.get(mergedOccupied.size() - 1);
                if (interval[0] <= last[1]) {
                    last[1] = Math.max(last[1], interval[1]);
                } else {
                    mergedOccupied.add(interval);
                }
            }
        }

        List<int[]> gapIntervals = new ArrayList<>();
        int searchStart = 0;
        for (int[] occInt : mergedOccupied) {
            if (occInt[0] > searchStart) {
                gapIntervals.add(new int[]{searchStart, occInt[0]});
            }
            searchStart = Math.max(searchStart, occInt[1]);
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
                    seg.addElement(staffId, new Rest(voice, restType, measure));
                }
                restStartTick += restType.getTicks();
            }
        }
    }

    private static boolean hasElementsInVoice(Measure measure, int staffId, int voice) {
        for (Segment seg : measure.getSegments()) {
            for (NoteRestElement nre : seg.getNoteRestByStaffAndVoice(staffId, voice)) {
                return true;
            }
        }
        return false;
    }

    private static void removeAllElementsInVoice(Measure measure, int staffId, int voice) {
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staffId, voice);
                for (NoteRestElement el : new ArrayList<>(nres)) {
                    seg.removeNoteRest(staffId, el);
                }
            }
        }
    }

    private static void updateBeams(Measure measure) {
        for (Staff staff : measure.getStaves()) {
            for (int voice = 1; voice <= 4; voice++) {
                updateBeamsForVoice(measure, staff.getIndex(), voice);
            }
        }
    }

    private static void updateBeamsForVoice(Measure measure, int staffId, int voice) {
        List<Note> currentGroup = new ArrayList<>();
        int beatTicks = getBeatUnitTicks(measure.getTimeSignature());
        int currentTick = 0;

        for (Segment seg : measure.getSegments()) {
            if (!seg.isNoteRest()) {
                currentTick += seg.getDuration();
                continue;
            }

            List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staffId, voice);
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