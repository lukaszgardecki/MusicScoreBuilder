package org.example.musicscorebuilder.components.music;

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

        // =========================================================================
        // ŚCISŁA BRAMKA: Maksymalna dozwolona pojemność taktu w tickach!
        // Pobieramy sztywny limit z metrum (TimeSignature) lub z bazowej rozdzielczości startowej.
        // Żaden takt nie może przekroczyć tego limitu
        // =========================================================================
        int strictMaxMeasureTicks = getStrictMaxMeasureTicks(measure);

        // Jeśli zmniejszamy (diff < 0)
        if (diff < 0) {
            targetSegment.removeNoteRest(staff, elementToChange);
            Element replacement = (elementToChange instanceof Note note)
                    ? new Note(voice, note.getStep(), note.getAlter(), note.getOctave(), newType, note.getBeam(), measure)
                    : new Rest(voice, newType, measure);
            targetSegment.addElement(staff, replacement);

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

            measure.updateResolutionFromSegments();
            measure.setDirty(true);
            return;
        }

        // =========================================================================
        // JEŚLI POWIĘKSZAMY (diff > 0):
        // Sprawdzamy absolutny limit i pożeramy tylko tyle, ile trzeba, pilnując sumy!
        // =========================================================================

        int accumulatedTicks = 0;
        int lastSegmentIndexToConsume = targetIndex;

        for (int i = targetIndex; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.getType() != SegmentType.NOTEREST) break;

            int voiceTicksInSeg = getVoiceTicksInSegmentForStaff(seg, staff, voice);

            var staffElements = seg.getElementsByStaff(staff);
            if (staffElements != null && i > targetIndex) {
                boolean hasNoteToAvoid = false;
                for (Element el : staffElements) {
                    if (el instanceof Note && el instanceof NoteRestElement nre && nre.getVoice() == voice) {
                        hasNoteToAvoid = true;
                        break;
                    }
                }
                if (hasNoteToAvoid) {
                    break;
                }
            }

            accumulatedTicks += (i == targetIndex) ? oldTicks : voiceTicksInSeg;
            lastSegmentIndexToConsume = i;

            if (accumulatedTicks >= newTicks) {
                break;
            }
        }

        if (accumulatedTicks < newTicks) {
            return; // Za mało miejsca / napotkano nutę
        }

        // 1. Usuwamy stare elementy dla tego głosu z pożartych segmentów
        for (int i = lastSegmentIndexToConsume; i >= targetIndex; i--) {
            Segment seg = segments.get(i);
            var staffElements = seg.getElementsByStaff(staff);
            if (staffElements != null) {
                var copy = new java.util.ArrayList<>(staffElements);
                for (Element el : copy) {
                    if (el instanceof NoteRestElement nre && nre.getVoice() == voice) {
                        seg.removeNoteRest(staff, nre);
                    }
                }
            }

            boolean isEmpty = true;
            for (Staff s : measure.getStaves()) {
                var els = seg.getElementsByStaff(s);
                if (els != null && !els.isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty && i != targetIndex) {
                segments.remove(i);
            }
        }

        // 2. Wstawiamy powiększony element do targetSegment
        Element replacement = (elementToChange instanceof Note note)
                ? new Note(voice, note.getStep(), note.getAlter(), note.getOctave(), newType, note.getBeam(), measure)
                : new Rest(voice, newType, measure);
        targetSegment.addElement(staff, replacement);

        // 3. Zwracamy nadmiar ticków jako pauzę, ALE PILNUJEMY, ABY SUMA W TAKCIE NIGDY NIE PRZEKROCZYŁA strictMaxMeasureTicks!
        int excessTicks = accumulatedTicks - newTicks;
        if (excessTicks > 0) {
            // Najpierw sprawdzamy aktualną sumę ticków w takcie dla tego głosu
            int currentVoiceTotal = 0;
            for (Segment seg : segments) {
                if (seg.getType() == SegmentType.NOTEREST) {
                    currentVoiceTotal += getVoiceTicksInSegmentForStaff(seg, staff, voice);
                }
            }

            // Dopuszczamy do wstawienia tylko tyle, ile faktycznie mieści się w limicie taktu!
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

        measure.updateResolutionFromSegments();
        measure.setDirty(true);
    }

    private static int getStrictMaxMeasureTicks(Measure measure) {
        try {
            var timeSig = measure.getTimeSignature();
            if (timeSig != null) {
                return timeSig.getTotalTicks(); // Zazwyczaj 3840 dla 4/4
            }
        } catch (Exception ignored) {}

        return 3840; // Awaryjny standardowy limit 4/4
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
        if (ticks <= 0) {
            return NoteType.THIRTY_SECOND;
        }

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