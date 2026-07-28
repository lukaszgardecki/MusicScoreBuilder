package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class MeasureDurationEditor {

    public static void changeElementDuration(Measure measure, Segment targetSegment, Staff staff, NoteRestElement elementToChange, NoteType newType) {
        if (targetSegment.getType() != SegmentType.NOTEREST) return;

        int targetIndex = measure.getSegments().indexOf(targetSegment);
        if (targetIndex == -1) return;

        var context = new OperationContext(measure, targetSegment, targetIndex, staff, elementToChange, newType);
        context.execute();
    }

    private static class OperationContext {
        private final Measure measure;
        private final List<Segment> segments;
        private final Segment targetSegment;
        private final int targetIndex;
        private final Staff staff;
        private final NoteRestElement elementToChange;
        private final NoteType newType;
        private final int voice;
        private final int newTicks;
        private final int currentSegmentTicks;

        public OperationContext(Measure measure, Segment targetSegment, int targetIndex, Staff staff, NoteRestElement elementToChange, NoteType newType) {
            this.measure = measure;
            this.segments = measure.getSegments();
            this.targetSegment = targetSegment;
            this.targetIndex = targetIndex;
            this.staff = staff;
            this.elementToChange = elementToChange;
            this.newType = newType;
            this.voice = elementToChange.getVoice();
            this.newTicks = newType.getTicks();
            this.currentSegmentTicks = targetSegment.getDuration();
        }

        public void execute() {
            targetSegment.removeElement(staff, elementToChange);

            if (newTicks > currentSegmentTicks) {
                expand();
            } else if (newTicks < currentSegmentTicks) {
                shrink();
            } else {
                replace(newType);
            }

            measure.setDirty(true);
        }

        private void expand() {
            int accumulatedTicks = currentSegmentTicks;
            List<Segment> segmentsToRemove = new ArrayList<>();

            for (int i = targetIndex + 1; i < segments.size() && accumulatedTicks < newTicks; i++) {
                Segment nextSeg = segments.get(i);
                if (nextSeg.getType() != SegmentType.NOTEREST) break;

                int nextSegTicks = nextSeg.getDuration();

                if (accumulatedTicks + nextSegTicks > newTicks) {
                    int excessTicks = (accumulatedTicks + nextSegTicks) - newTicks;

                    for (NoteRestElement el : nextSeg.getNoteRestByStaffAndVoice(staff, voice)) {
                        nextSeg.removeElement(staff, el);
                    }

                    NoteType excessType = findBestFitNoteType(excessTicks);
                    nextSeg.addElement(staff, new Rest(voice, excessType, measure));

                    accumulatedTicks = newTicks;
                    break;
                }

                for (NoteRestElement el : nextSeg.getNoteRestByStaffAndVoice(staff, voice)) {
                    nextSeg.removeElement(staff, el);
                }

                accumulatedTicks += nextSegTicks;
                segmentsToRemove.add(nextSeg);
            }

            segments.removeAll(segmentsToRemove);

            NoteType finalType = findBestFitNoteType(accumulatedTicks);
            replace(finalType);
        }

        private void shrink() {
            replace(newType);

            int segmentTicks = currentSegmentTicks;
            while (segmentTicks > newTicks) {
                int halfTicks = segmentTicks / 2;
                NoteType halfType = findClosestNoteType(halfTicks);
                if (halfType == null) break;

                Segment secondHalf = new Segment(SegmentType.NOTEREST, measure);
                secondHalf.addElement(staff, new Rest(voice, halfType, measure));

                segments.add(targetIndex + 1, secondHalf);
                segmentTicks = halfTicks;
            }
        }

        private void replace(NoteType type) {
            Element replacement = createElement(type);
            targetSegment.addElement(staff, replacement);
        }

        private Element createElement(NoteType type) {
            if (elementToChange instanceof Note note) {
                return new Note(voice, note.getStep(), note.getAlter(), note.getOctave(), type, note.getBeam(), measure);
            }
            return new Rest(voice, type, measure);
        }
    }

    private static NoteType findBestFitNoteType(int ticks) {
        for (NoteType type : NoteType.values()) {
            if (type.getTicks() == ticks) {
                return type;
            }
        }
        return findClosestNoteType(ticks);
    }

    private static NoteType findClosestNoteType(int ticks) {
        try {
            return NoteType.fromTicks(ticks);
        } catch (IllegalArgumentException e) {
            for (NoteType type : NoteType.values()) {
                if (type.getTicks() <= ticks) {
                    return type;
                }
            }
            return NoteType.QUARTER;
        }
    }
}