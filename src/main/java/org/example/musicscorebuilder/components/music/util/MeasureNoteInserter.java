package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.*;

import java.util.List;

public class MeasureNoteInserter {

    /**
     * Wstawia nutę do wskazanego segmentu na danej pięciolinii i w danym głosie,
     * dostosowując strukturę segmentów w takcie (dzielenie na pauzy lub rozpychanie).
     */
    public static void insertNote(Measure measure, Segment targetSegment, Staff staff, Note newNote) {
        if (targetSegment.getType() != SegmentType.NOTEREST) return;

        List<Segment> segments = measure.getSegments();
        int targetIndex = segments.indexOf(targetSegment);
        if (targetIndex == -1) return;

        int voice = newNote.getVoice();
        int newTicks = newNote.getType().getTicks();

        // Pobieramy czas obecnego elementu w tym głosie (lub ogólny czas trwania segmentu)
        List<NoteRestElement> existingElements = targetSegment.getNoteRestByStaffAndVoice(staff, voice);
        int currentSegmentTicks = !existingElements.isEmpty()
                ? existingElements.get(0).getType().getTicks()
                : targetSegment.getDuration();

        // CASE 1: Nuta ma dokładnie taki sam czas jak dotychczasowy element/segment
        if (newTicks == currentSegmentTicks) {
            targetSegment.insertNote(staff, newNote);
            measure.updateResolutionFromSegments();
            measure.setDirty(true);
            return;
        }

        // CASE 2: Nuta jest MNIEJSZA od dotychczasowej wartości (podział na pauze)
        if (newTicks < currentSegmentTicks) {
            // Aktualizujemy segment docelowy na wszystkich pięcioliniach
            updateSegmentForVoice(targetSegment, measure, voice, newNote.getType(), staff, newNote);

            int remainingTicks = currentSegmentTicks - newTicks;
            int insertIdx = targetIndex + 1;

            // Dopełniamy brakujący czas nowymi segmentami z pauzami
            while (remainingTicks > 0) {
                NoteType fit = MeasureTimeSignatureAdjuster.findLargestFittingNoteType(remainingTicks);
                if (fit == null || fit.getTicks() <= 0) break;

                Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
                for (Staff s : measure.getStaves()) {
                    fillSeg.addElement(s, new Rest(voice, fit, measure));
                }

                if (insertIdx >= segments.size()) {
                    segments.add(fillSeg);
                } else {
                    segments.add(insertIdx, fillSeg);
                }
                insertIdx++;
                remainingTicks -= fit.getTicks();
            }

            measure.updateResolutionFromSegments();
            measure.setDirty(true);
            return;
        }

        // CASE 3: Nuta jest WIĘKSZA (rozpycha się i wchłania kolejne segmenty)
        int accumulatedTicks = 0;
        int lastSegmentIndexToConsume = targetIndex;

        for (int i = targetIndex; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.getType() != SegmentType.NOTEREST) break;

            int voiceTicksInSeg = getVoiceTicksInSegmentForStaff(seg, staff, voice);
            accumulatedTicks += voiceTicksInSeg;
            lastSegmentIndexToConsume = i;

            if (accumulatedTicks >= newTicks) {
                break;
            }
        }

        // Jeśli brakuje miejsca w takcie – blokujemy operację
        if (accumulatedTicks < newTicks) {
            return;
        }

        // Usuwamy elementy z wchłoniętych kolejnych segmentów
        for (int i = lastSegmentIndexToConsume; i > targetIndex; i--) {
            Segment seg = segments.get(i);
            List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);
            for (NoteRestElement nre : nres) {
                seg.removeNoteRest(staff, nre);
            }

            // Usunięcie segmentu jeśli po wyczyszczeniu stał się pusty na wszystkich pięcioliniach
            boolean isEmpty = true;
            for (Staff s : measure.getStaves()) {
                List<Element> els = seg.getElementsByStaff(s);
                if (els != null && !els.isEmpty()) {
                    isEmpty = false;
                    break;
                }
            }
            if (isEmpty) {
                segments.remove(i);
            }
        }

        // Wstawiamy powiększoną nutę w segment docelowy i wyrównujemy pozostałe pięciolinie
        updateSegmentForVoice(targetSegment, measure, voice, newNote.getType(), staff, newNote);

        // Jeśli po wchłonięciu powstał naddatek tików – dopełniamy go pauzami
        int excessTicks = accumulatedTicks - newTicks;
        if (excessTicks > 0) {
            int insertIdx = segments.indexOf(targetSegment) + 1;
            while (excessTicks > 0) {
                NoteType fit = MeasureTimeSignatureAdjuster.findLargestFittingNoteType(excessTicks);
                if (fit == null || fit.getTicks() <= 0) break;

                Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
                for (Staff s : measure.getStaves()) {
                    fillSeg.addElement(s, new Rest(voice, fit, measure));
                }

                if (insertIdx >= segments.size()) {
                    segments.add(fillSeg);
                } else {
                    segments.add(insertIdx, fillSeg);
                }
                insertIdx++;
                excessTicks -= fit.getTicks();
            }
        }

        measure.updateResolutionFromSegments();
        measure.setDirty(true);
    }

    /**
     * Ustawia odpowiednią nutę na docelowej pięciolinii oraz pauzy dopasowane do nowej długości na pozostałych pięcioliniach.
     */
    private static void updateSegmentForVoice(Segment targetSegment, Measure measure, int voice, NoteType targetType, Staff targetStaff, Note newNote) {
        for (Staff s : measure.getStaves()) {
            if (s.equals(targetStaff)) {
                targetSegment.insertNote(s, newNote);
            } else {
                List<NoteRestElement> nres = targetSegment.getNoteRestByStaffAndVoice(s, voice);
                for (NoteRestElement nre : nres) {
                    targetSegment.removeNoteRest(s, nre);
                }
                targetSegment.addElement(s, new Rest(voice, targetType, measure));
            }
        }
    }

    private static int getVoiceTicksInSegmentForStaff(Segment seg, Staff staff, int voice) {
        List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(staff, voice);
        if (!elements.isEmpty()) {
            return elements.get(0).getType().getTicks();
        }
        return seg.getDuration();
    }
}