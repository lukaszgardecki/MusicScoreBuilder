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

        int startTick = measure.getStartTickOfSegment(targetSegment);
        if (startTick < 0) return null;

        int totalMeasureTicks = measure.getTimeSignature().getTotalTicks();
        int availableInMeasure = totalMeasureTicks - startTick;

        int ticksForCurrentMeasure = Math.min(newTicks, availableInMeasure);
        int remainingTicks = newTicks - ticksForCurrentMeasure;

        Measure nextMeasure = null;
        if (remainingTicks > 0) {
            nextMeasure = getNextMeasure(measure);
            if (nextMeasure == null) {
                ticksForCurrentMeasure = availableInMeasure;
                remainingTicks = 0;
            }
        }

        List<Note> notesM1 = (remainingTicks == 0)
                ? List.of(newNote)
                : createNotesForTicks(newNote, ticksForCurrentMeasure, measure);

        List<Note> notesM2 = (remainingTicks > 0)
                ? createNotesForTicks(newNote, remainingTicks, nextMeasure)
                : Collections.emptyList();

        List<Note> allInsertedNotes = new ArrayList<>();

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

        cleanAndFillVoiceGaps(measure, staff);
        measure.setDirty(true);

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

            cleanAndFillVoiceGaps(nextMeasure, staff);
            nextMeasure.setDirty(true);
        }

        for (int i = 0; i < allInsertedNotes.size() - 1; i++) {
            linkNotesWithTie(allInsertedNotes.get(i), allInsertedNotes.get(i + 1));
        }

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
            notes.add(createNoteFromTemplate(templateNote, type, targetMeasure));
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

    private static void cleanAndFillVoiceGaps(Measure measure, Staff targetStaff) {
        for (int voice = 1; voice <= 4; voice++) {
            boolean hasElements = hasElementsInVoice(measure, targetStaff, voice);
            boolean active = (voice == 1) || hasElements;

            if (!active) {
                removeAllElementsInVoice(measure, targetStaff, voice);
            } else {
                fillGapsForActiveVoice(measure, targetStaff, voice);
            }
        }

        updateBeamsForStaff(measure, targetStaff);
    }

    private static void updateBeamsForStaff(Measure measure, Staff targetStaff) {
        for (int voice = 1; voice <= 4; voice++) {
            updateBeamsForVoice(measure, targetStaff, voice);
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

        List<int[]> coveredIntervals = new ArrayList<>();
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDur = seg.getDuration();
                List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(staff, voice);
                for (NoteRestElement el : elements) {
                    int elStart = currentTick;
                    int elEnd = currentTick + getElementTicks(el);
                    coveredIntervals.add(new int[]{elStart, elEnd});
                }
                currentTick += segDur;
            }
        }

        coveredIntervals.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> mergedIntervals = new ArrayList<>();
        for (int[] interval : coveredIntervals) {
            if (mergedIntervals.isEmpty()) {
                mergedIntervals.add(new int[]{interval[0], interval[1]});
            } else {
                int[] last = mergedIntervals.get(mergedIntervals.size() - 1);
                if (interval[0] <= last[1]) {
                    last[1] = Math.max(last[1], interval[1]);
                } else {
                    mergedIntervals.add(new int[]{interval[0], interval[1]});
                }
            }
        }

        List<int[]> gapIntervals = new ArrayList<>();
        int searchStart = 0;
        for (int[] covered : mergedIntervals) {
            if (covered[0] > searchStart) {
                gapIntervals.add(new int[]{searchStart, covered[0]});
            }
            searchStart = Math.max(searchStart, covered[1]);
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

    private static boolean hasElementsInVoice(Measure measure, Staff staff, int voice) {
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest() && !seg.getNoteRestByStaffAndVoice(staff, voice).isEmpty()) {
                return true;
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

            if (currentTick == targetTick) {
                return;
            }

            if (targetTick > currentTick && targetTick < segEnd) {
                int part1Ticks = targetTick - currentTick;
                int part2Ticks = segEnd - targetTick;

                Segment seg1 = new Segment(SegmentType.NOTEREST, measure);
                seg1.setDuration(part1Ticks);

                Segment seg2 = new Segment(SegmentType.NOTEREST, measure);
                seg2.setDuration(part2Ticks);

                for (Staff s : measure.getStaves()) {
                    for (int voice = 1; voice <= 4; voice++) {
                        List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(s, voice);
                        for (NoteRestElement el : elements) {
                            seg1.addElement(s, el);
                        }
                    }
                }

                segments.remove(i);
                segments.add(i, seg1);
                segments.add(i + 1, seg2);
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