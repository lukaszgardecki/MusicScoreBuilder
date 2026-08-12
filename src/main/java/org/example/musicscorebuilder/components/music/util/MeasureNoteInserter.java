package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.NoteRestElement;
import org.example.musicscorebuilder.components.music.Segment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MeasureNoteInserter {

    private static class NonNoteRestPos {
        final Segment segment;
        final int tick;

        NonNoteRestPos(Segment segment, int tick) {
            this.segment = segment;
            this.tick = tick;
        }
    }

    public static Segment insertNote(Measure measure, Segment targetSegment, int staffId, Note newNote) {
        if (measure == null || targetSegment == null || newNote == null) return null;
        if (!targetSegment.isNoteRest()) return null;

        int voice = newNote.getVoice();
        int newTicks = MeasureTimeSignatureAdjuster.calculateTicks(newNote.getType(), newNote.getDots());

        int startTick = getStartTickOfSegment(measure, targetSegment);
        if (startTick < 0) return null;

        int endTick = startTick + newTicks;

        Map<Measure, List<NonNoteRestPos>> savedPositions = new HashMap<>();
        Measure curr = measure;
        while (curr != null) {
            savedPositions.put(curr, saveNonNoteRestPositions(curr));
            curr = curr.getNext();
        }

        removeCollisions(measure, staffId, voice, startTick, endTick);

        targetSegment.addElement(staffId, newNote);

        MeasureTimeSignatureAdjuster.adjustFromMeasure(measure);

        for (Map.Entry<Measure, List<NonNoteRestPos>> entry : savedPositions.entrySet()) {
            restoreNonNoteRestPositions(entry.getKey(), entry.getValue());
        }

        return findNextFreeSegment(measure, endTick);
    }

    private static List<NonNoteRestPos> saveNonNoteRestPositions(Measure measure) {
        List<NonNoteRestPos> saved = new ArrayList<>();
        if (measure == null || measure.getSegments() == null) return saved;

        for (Segment seg : measure.getSegments()) {
            if (!seg.isNoteRest()) {
                int tick = getStartTickOfSegment(measure, seg);
                if (tick >= 0) {
                    saved.add(new NonNoteRestPos(seg, tick));
                }
            }
        }
        return saved;
    }

    private static void restoreNonNoteRestPositions(Measure measure, List<NonNoteRestPos> saved) {
        if (measure == null || measure.getSegments() == null || saved == null || saved.isEmpty()) return;

        List<Segment> segments = measure.getSegments();
        segments.removeIf(seg -> !seg.isNoteRest());

        for (NonNoteRestPos pos : saved) {
            int targetTick = pos.tick;
            int currentTick = 0;
            int insertIndex = 0;

            while (insertIndex < segments.size()) {
                Segment seg = segments.get(insertIndex);
                if (seg.isNoteRest()) {
                    if (currentTick >= targetTick) {
                        break;
                    }
                    currentTick += seg.getDuration();
                }
                insertIndex++;
            }

            segments.add(insertIndex, pos.segment);
        }
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

    private static void removeCollisions(Measure measure, int staffId, int voice, int startTick, int targetEndTick) {
        Measure currentMeasure = measure;
        int currentMeasureStartTick = 0;

        while (currentMeasure != null && currentMeasureStartTick < targetEndTick) {
            int currentTickInMeasure = 0;
            int measureTotalTicks = (currentMeasure.getTimeSignature() != null)
                    ? currentMeasure.getTimeSignature().getTotalTicks()
                    : 1920;

            for (Segment seg : new ArrayList<>(currentMeasure.getSegments())) {
                if (!seg.isNoteRest()) continue;

                int segDur = seg.getDuration();
                int segStartGlobal = currentMeasureStartTick + currentTickInMeasure;
                int segEndGlobal = segStartGlobal + segDur;

                List<NoteRestElement> elements = new ArrayList<>(seg.getNoteRestByStaffAndVoice(staffId, voice));
                for (NoteRestElement el : elements) {
                    if (segStartGlobal < targetEndTick && segEndGlobal > startTick) {
                        seg.removeNoteRest(staffId, el);
                    }
                }

                currentTickInMeasure += segDur;
            }

            currentMeasureStartTick += measureTotalTicks;
            if (currentMeasureStartTick < targetEndTick) {
                currentMeasure = currentMeasure.getNext();
            } else {
                break;
            }
        }
    }

    public static Segment findNextFreeSegment(Measure measure, int targetEndTick) {
        Measure currentMeasure = measure;
        int currentMeasureStartTick = 0;

        while (currentMeasure != null) {
            int currentTickInMeasure = 0;

            for (Segment seg : currentMeasure.getSegments()) {
                if (!seg.isNoteRest()) continue;

                int segStartGlobal = currentMeasureStartTick + currentTickInMeasure;
                if (segStartGlobal >= targetEndTick) {
                    return seg;
                }
                currentTickInMeasure += seg.getDuration();
            }

            int measureTotalTicks = (currentMeasure.getTimeSignature() != null)
                    ? currentMeasure.getTimeSignature().getTotalTicks()
                    : 1920;

            currentMeasureStartTick += measureTotalTicks;
            currentMeasure = currentMeasure.getNext();
        }

        return null;
    }
}