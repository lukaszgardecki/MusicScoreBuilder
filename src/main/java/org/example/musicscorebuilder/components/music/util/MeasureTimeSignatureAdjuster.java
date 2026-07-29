package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class MeasureTimeSignatureAdjuster {

    /**
     * Dopasowuje segmenty taktu do wybranego metrum (skracanie nadmiarowych segmentów / wypełnianie pauzami).
     */
    public static void adjustSegmentsToTimeSignature(Measure measure) {
        TimeSignature timeSignature = measure.getTimeSignature();
        if (timeSignature == null) return;

        int targetTicks = timeSignature.getTotalTicks();
        int currentTicks = 0;

        List<Segment> segments = measure.getSegments();
        List<Staff> staves = measure.getStaves();
        List<Segment> validSegments = new ArrayList<>();

        // 1. Zachowujemy istniejące segmenty, dopóki mieszczą się w nowym limicie tików
        for (Segment seg : segments) {
            int segTicks = seg.getDuration();
            if (currentTicks + segTicks <= targetTicks) {
                validSegments.add(seg);
                currentTicks += segTicks;
            } else {
                break;
            }
        }

        segments.clear();
        segments.addAll(validSegments);

        // 2. Jeśli takt był pusty – budujemy wzorzec pauz dedykowany dla danego metrum
        if (currentTicks == 0) {
            List<NoteType> pattern = generateDefaultRestPattern(timeSignature.getBeat(), timeSignature.getBeatType());
            for (NoteType type : pattern) {
                Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
                for (Staff staff : staves) {
                    fillSeg.addElement(staff, new Rest(1, type, measure));
                }
                segments.add(fillSeg);
            }
            return;
        }

        // 3. Jeśli takt był częściowo wypełniony – dopełniamy brakujące miejsce
        int remainingTicks = targetTicks - currentTicks;
        while (remainingTicks > 0) {
            NoteType fit = findLargestFittingNoteType(remainingTicks);
            if (fit == null || fit.getTicks() <= 0) break;

            Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
            for (Staff staff : staves) {
                fillSeg.addElement(staff, new Rest(1, fit, measure));
            }
            segments.add(fillSeg);

            remainingTicks -= fit.getTicks();
        }
    }

    /**
     * Generator wzorca pauz dla pustego taktu według ściśle określonych schematów.
     */
    public static List<NoteType> generateDefaultRestPattern(int beat, int beatType) {
        List<NoteType> pattern = new ArrayList<>();

        if (beatType == 8) {
            int groupsOfThree = beat / 3;
            int remainder = beat % 3;

            for (int i = 0; i < groupsOfThree; i++) {
                pattern.add(NoteType.QUARTER);
                pattern.add(NoteType.EIGHTH);
            }

            if (remainder == 1) {
                pattern.add(NoteType.EIGHTH);
            } else if (remainder == 2) {
                pattern.add(NoteType.QUARTER);
            }

        } else if (beatType == 2) {
            for (int i = 0; i < beat; i++) {
                pattern.add(NoteType.HALF);
            }

        } else if (beatType == 4) {
            if (beat < 4) {
                for (int i = 0; i < beat; i++) {
                    pattern.add(NoteType.QUARTER);
                }
            } else {
                int rem = beat;
                while (rem >= 4) {
                    pattern.add(NoteType.WHOLE);
                    rem -= 4;
                }
                if (rem >= 2) {
                    pattern.add(NoteType.HALF);
                    rem -= 2;
                }
                if (rem == 1) {
                    pattern.add(NoteType.QUARTER);
                    rem -= 1;
                }
            }

        } else {
            int ticks = beat * (3840 / beatType);
            while (ticks > 0) {
                NoteType fit = findLargestFittingNoteType(ticks);
                if (fit == null) break;
                pattern.add(fit);
                ticks -= fit.getTicks();
            }
        }

        return pattern;
    }

    /**
     * Znajduje największy NoteType, który mieści się w podanej liczbie tików.
     */
    public static NoteType findLargestFittingNoteType(int remainingTicks) {
        NoteType best = null;
        for (NoteType type : NoteType.values()) {
            if (type.getTicks() <= remainingTicks) {
                if (best == null || type.getTicks() > best.getTicks()) {
                    best = type;
                }
            }
        }
        return best;
    }
}