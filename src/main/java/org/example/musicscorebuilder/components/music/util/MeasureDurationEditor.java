package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.*;

public class MeasureDurationEditor {

    public static void changeElementDuration(Measure measure, Segment targetSegment, Staff staff, NoteRestElement elementToChange, NoteType newType) {
        if (targetSegment.getType() != SegmentType.NOTEREST) return;

        var segments = measure.getSegments();
        int targetIndex = segments.indexOf(targetSegment);
        if (targetIndex == -1) return;

        int voice = elementToChange.getVoice();
        int oldTicks = elementToChange.getType().getTicks();
        int newTicks = newType.getTicks();
        int diff = newTicks - oldTicks;

        if (diff == 0) return;

        int strictMaxMeasureTicks = getStrictMaxMeasureTicks(measure);

        // Zbieramy elementy dla tego głosu (na wypadek, gdyby jednak było więcej nut)
        java.util.List<NoteRestElement> voiceElements = new java.util.ArrayList<>();
        var staffEls = targetSegment.getElementsByStaff(staff);
        if (staffEls != null) {
            for (Element el : staffEls) {
                if (el instanceof NoteRestElement nre && nre.getVoice() == voice) {
                    voiceElements.add(nre);
                }
            }
        }

        // Zabezpieczenie, jeśli jakimś cudem lista byłaby pusta
        if (voiceElements.isEmpty()) voiceElements.add(elementToChange);

        if (diff < 0) {
            for (NoteRestElement oldEl : voiceElements) {
                targetSegment.removeNoteRest(staff, oldEl);
                Element replacement;
                if (oldEl instanceof Note note) {
                    // Czyścimy ew. powiązanie z belką (null), by uniknąć błędów rysowania
                    replacement = new Note(voice, note.getStep(), note.getAlter(), note.getOctave(), newType, null, measure);
                } else {
                    replacement = new Rest(voice, newType, measure);
                }
                targetSegment.addElement(staff, replacement);
            }

            int remaining = -diff;
            int insertIndex = targetIndex + 1;

            while (remaining > 0) {
                NoteType fit = findStrictFitting(remaining);
                if (fit.getTicks() <= 0) break;

                Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
                fillSeg.addElement(staff, new Rest(voice, fit, measure));

                if (insertIndex >= segments.size()) {
                    segments.add(fillSeg);
                } else {
                    segments.add(insertIndex, fillSeg);
                }
                insertIndex++;
                remaining -= fit.getTicks();
            }

            measure.setDirty(true);
            return;
        }

        // JEŚLI POWIĘKSZAMY (diff > 0)
        int accumulatedTicks = 0;
        int lastSegmentIndexToConsume = targetIndex;

        for (int i = targetIndex; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.getType() != SegmentType.NOTEREST) break;

            int voiceTicksInSeg = getVoiceTicksInSegmentForStaff(seg, staff, voice);

            accumulatedTicks += (i == targetIndex) ? oldTicks : voiceTicksInSeg;
            lastSegmentIndexToConsume = i;

            if (accumulatedTicks >= newTicks) {
                break;
            }
        }

        if (accumulatedTicks < newTicks) {
            return; // Za mało miejsca w takcie na powiększenie (blokada krawędzi taktu)
        }

        // Usuwanie elementów, które wchłaniamy z kolejnych segmentów
        for (int i = lastSegmentIndexToConsume; i >= targetIndex; i--) {
            Segment seg = segments.get(i);
            var currentStaffElements = seg.getElementsByStaff(staff);
            if (currentStaffElements != null) {
                var copy = new java.util.ArrayList<>(currentStaffElements);
                for (Element el : copy) {
                    if (el instanceof NoteRestElement nre && nre.getVoice() == voice) {
                        seg.removeNoteRest(staff, nre);
                    }
                }
            }

            if (i != targetIndex) {
                boolean isEmpty = true;
                for (Staff s : measure.getStaves()) {
                    var els = seg.getElementsByStaff(s);
                    if (els != null && !els.isEmpty()) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    segments.remove(i);
                }
            }
        }

        // Wstawiamy powiększoną nutę / pauzę
        for (NoteRestElement oldEl : voiceElements) {
            Element replacement;
            if (oldEl instanceof Note note) {
                replacement = new Note(voice, note.getStep(), note.getAlter(), note.getOctave(), newType, null, measure);
            } else {
                replacement = new Rest(voice, newType, measure);
            }
            targetSegment.addElement(staff, replacement);
        }

        // Dopełnianie reszty pauzami
        int excessTicks = accumulatedTicks - newTicks;
        if (excessTicks > 0) {
            int currentVoiceTotal = 0;
            for (Segment seg : segments) {
                if (seg.getType() == SegmentType.NOTEREST) {
                    currentVoiceTotal += getVoiceTicksInSegmentForStaff(seg, staff, voice);
                }
            }

            int allowedExcess = Math.min(excessTicks, strictMaxMeasureTicks - currentVoiceTotal);

            if (allowedExcess > 0) {
                int insertIndex = targetSegment.equals(segments.get(targetIndex)) ? targetIndex + 1 : targetIndex + 2;
                if (insertIndex > segments.size()) insertIndex = segments.size();

                while (allowedExcess > 0) {
                    NoteType fit = findStrictFitting(allowedExcess);
                    if (fit.getTicks() <= 0) break;

                    Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
                    fillSeg.addElement(staff, new Rest(voice, fit, measure));

                    segments.add(insertIndex, fillSeg);
                    insertIndex++;
                    allowedExcess -= fit.getTicks();
                }
            }
        }
        measure.setDirty(true);
    }

    private static int getStrictMaxMeasureTicks(Measure measure) {
        try {
            var timeSig = measure.getTimeSignature();
            if (timeSig != null) {
                return timeSig.getTotalTicks();
            }
        } catch (Exception ignored) {}
        return 3840;
    }

    private static int getVoiceTicksInSegmentForStaff(Segment seg, Staff staff, int voice) {
        int ticks = 0;
        var elementsForStaff = seg.getElementsByStaff(staff);
        if (elementsForStaff != null) {
            for (Element el : elementsForStaff) {
                if (el instanceof NoteRestElement nre && nre.getVoice() == voice) {
                    ticks += nre.getType().getTicks();
                }
            }
        }
        return ticks;
    }

    private static NoteType findStrictFitting(int ticks) {
        if (ticks <= 0) return NoteType.THIRTY_SECOND;

        NoteType best = NoteType.THIRTY_SECOND;
        int minRemainder = Integer.MAX_VALUE;

        for (NoteType type : NoteType.values()) {
            if (type.getTicks() <= ticks) {
                int remainder = ticks % type.getTicks();
                if (remainder < minRemainder) {
                    minRemainder = remainder;
                    best = type;
                } else if (remainder == minRemainder && type.getTicks() > best.getTicks()) {
                    best = type;
                }
            }
        }
        return best;
    }
}