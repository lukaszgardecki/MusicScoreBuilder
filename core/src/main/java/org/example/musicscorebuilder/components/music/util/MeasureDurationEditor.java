package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.*;

import java.util.List;

public class MeasureDurationEditor {

    public static void changeElementDuration(Measure measure, Segment targetSegment, int staffId, NoteRestElement elementToChange, NoteType newType) {
        changeElementDuration(measure, targetSegment, staffId, elementToChange, newType, 0);
    }

    public static void changeElementDuration(Measure measure, Segment targetSegment, int staffId, NoteRestElement elementToChange, NoteType newType, int dots) {
        if (measure == null || targetSegment == null || elementToChange == null || newType == null) return;
        if (targetSegment.getType() != SegmentType.NOTEREST) return;

        int voice = elementToChange.getVoice();
        int newTicks = MeasureTimeSignatureAdjuster.calculateTicks(newType, dots);

        int startTick = getStartTickOfSegment(measure, targetSegment);
        if (startTick < 0) return;

        int endTick = startTick + newTicks;

        removeCollisions(measure, staffId, voice, elementToChange, startTick, endTick);

        elementToChange.setType(newType);
        elementToChange.setDots(dots);

        MeasureTimeSignatureAdjuster.adjustFromMeasure(measure);
    }

    private static int getStartTickOfSegment(Measure measure, Segment targetSegment) {
        List<Segment> segments = measure.getSegments();
        int currentTick = 0;

        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg == targetSegment) return currentTick;
            if (seg.isNoteRest()) {
                currentTick += seg.getDuration();
            }
        }
        return -1;
    }

    private static void removeCollisions(Measure measure, int staffId, int voice, NoteRestElement elementToChange, int startTick, int targetEndTick) {
        Measure currentMeasure = measure;
        int currentMeasureStartTick = 0;

        while (currentMeasure != null && currentMeasureStartTick < targetEndTick) {
            int currentTickInMeasure = 0;
            int measureTotalTicks = (currentMeasure.getTimeSignature() != null)
                    ? currentMeasure.getTimeSignature().getTotalTicks()
                    : 1920;

            List<Segment> segments = currentMeasure.getSegments();

            for (int s = 0; s < segments.size(); s++) {
                Segment seg = segments.get(s);
                if (!seg.isNoteRest()) continue;

                int segDur = seg.getDuration();
                int segStartGlobal = currentMeasureStartTick + currentTickInMeasure;
                int segEndGlobal = segStartGlobal + segDur;

                boolean overlaps = (segStartGlobal < targetEndTick && segEndGlobal > startTick);

                if (overlaps) {
                    List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(staffId, voice);
                    if (!elements.isEmpty()) {
                        for (int i = elements.size() - 1; i >= 0; i--) {
                            NoteRestElement el = elements.get(i);
                            if (el != elementToChange) {
                                seg.removeNoteRest(staffId, el);
                            }
                        }
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
}