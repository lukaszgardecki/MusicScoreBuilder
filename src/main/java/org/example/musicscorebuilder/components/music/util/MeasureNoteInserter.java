package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class MeasureNoteInserter {

    public static Segment insertNote(Measure measure, Segment targetSegment, int staffId, Note newNote) {
        if (measure == null || targetSegment == null || newNote == null) return null;
        if (!targetSegment.isNoteRest()) return null;

        int voice = newNote.getVoice();
        int newTicks = MeasureTimeSignatureAdjuster.calculateTicks(newNote.getType(), newNote.getDots());

        int startTick = getStartTickOfSegment(measure, targetSegment);
        if (startTick < 0) return null;

        int endTick = startTick + newTicks;

        removeCollisions(measure, staffId, voice, startTick, endTick);

        targetSegment.addElement(staffId, newNote);

        MeasureTimeSignatureAdjuster.adjustFromMeasure(measure);

        return findNextFreeSegment(measure, endTick);
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