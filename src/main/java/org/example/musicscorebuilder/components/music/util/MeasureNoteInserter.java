package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public class MeasureNoteInserter {

    /**
     * Wstawia nutę do wskazanego segmentu i zwraca NASTĘPNY WOLNY segment (dla kursora).
     */
    public static Segment insertNote(Measure measure, Segment targetSegment, Staff staff, Note newNote) {
        if (targetSegment.getType() != SegmentType.NOTEREST) return null;

        List<Segment> segments = measure.getSegments();
        int targetIndex = segments.indexOf(targetSegment);
        if (targetIndex == -1) return null;

        int voice = newNote.getVoice();
        int newTicks = newNote.getType().getTicks();

        // 1. Obliczamy bezwzględny czas rozpoczęcia docelowego segmentu w takcie (w tickach)
        int startTick = 0;
        for (int i = 0; i < targetIndex; i++) {
            startTick += segments.get(i).getDuration();
        }
        int targetEndTick = startTick + newTicks;

        // Pobieramy czas obecnego elementu w tym głosie na tej pięciolinii
        List<NoteRestElement> existingElements = targetSegment.getNoteRestByStaffAndVoice(staff, voice);
        int currentSegmentTicks = !existingElements.isEmpty()
                ? existingElements.get(0).getType().getTicks()
                : targetSegment.getDuration();

        // CASE 1: Identyczny czas trwania — zwykła podmiana w segmencie
        if (newTicks == currentSegmentTicks) {
            targetSegment.insertNote(staff, newNote);
            measure.updateResolutionFromSegments();
            measure.setDirty(true);
            return findNextFreeSegment(measure, targetEndTick);
        }

        // CASE 2: Nuta jest MNIEJSZA (podział pauzy/elementu)
        if (newTicks < currentSegmentTicks) {
            targetSegment.insertNote(staff, newNote);
            adjustOtherStavesInSegment(targetSegment, measure, staff, voice, newNote.getType());

            int remainingTicks = currentSegmentTicks - newTicks;
            int insertIdx = targetIndex + 1;

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
            return findNextFreeSegment(measure, targetEndTick);
        }

        // CASE 3: Nuta jest WIĘKSZA (wchłania kolejne segmenty)
        int accumulatedTicks = 0;
        int lastSegmentIndexToConsume = targetIndex;

        for (int i = targetIndex; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.getType() != SegmentType.NOTEREST) break;

            accumulatedTicks += getVoiceTicksInSegmentForStaff(seg, staff, voice);
            lastSegmentIndexToConsume = i;

            if (accumulatedTicks >= newTicks) break;
        }

        // Brak miejsca w takcie na tak dużą nutę
        if (accumulatedTicks < newTicks) return null;

        // Usuwamy stare elementy z wchłoniętych kolejnych segmentów
        for (int i = lastSegmentIndexToConsume; i > targetIndex; i--) {
            Segment seg = segments.get(i);
            List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);
            for (NoteRestElement nre : nres) {
                seg.removeNoteRest(staff, nre);
            }

            if (isSegmentEmpty(seg, measure)) {
                segments.remove(i);
            }
        }

        targetSegment.insertNote(staff, newNote);

        // Dopełniamy ewentualny nadmiar czasu pauzami
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

        return findNextFreeSegment(measure, targetEndTick);
    }




    /**
     * Wyszukuje pierwszy segment zaczynający się w punkcie czasowym >= targetEndTick.
     * Jeśli w bieżącym takcie nie ma takiego segmentu, przechodzi do następnego taktu.
     */
    public static Segment findNextFreeSegment(Measure measure, int targetEndTick) {
        int currentTick = 0;

        // 1. Szukamy pierwszego wolnego segmentu w BIERZĄCYM takcie
        for (Segment seg : measure.getSegments()) {
            if (seg.getType() == SegmentType.NOTEREST) {
                if (currentTick >= targetEndTick) {
                    return seg;
                }
            }
            currentTick += seg.getDuration();
        }

        // 2. Jeśli czas wykracza poza ten takt, pobieramy KOLEJNY takt z aktywnego widoku/trybu
        Score score = ScoreService.getInstance().getScore();
        ScoreMode activeMode = ScoreStateManager.getInstance().getCurrentMode(score);

        if (activeMode != null) {
            List<Measure> measures = activeMode.getMeasures();
            int currentIndex = measures.indexOf(measure);

            if (currentIndex != -1 && currentIndex + 1 < measures.size()) {
                Measure nextMeasure = measures.get(currentIndex + 1);
                for (Segment seg : nextMeasure.getSegments()) {
                    if (seg.getType() == SegmentType.NOTEREST) {
                        return seg;
                    }
                }
            }
        }

        return null;
    }

    private static void adjustOtherStavesInSegment(Segment targetSegment, Measure measure, Staff targetStaff, int voice, NoteType newType) {
        for (Staff s : measure.getStaves()) {
            if (!s.equals(targetStaff)) {
                List<NoteRestElement> nres = targetSegment.getNoteRestByStaffAndVoice(s, voice);
                if (!nres.isEmpty() && nres.get(0).getType().getTicks() > newType.getTicks()) {
                    targetSegment.removeNoteRest(s, nres.get(0));
                    targetSegment.addElement(s, new Rest(voice, newType, measure));
                }
            }
        }
    }

    private static boolean isSegmentEmpty(Segment seg, Measure measure) {
        for (Staff s : measure.getStaves()) {
            List<Element> els = seg.getElementsByStaff(s);
            if (els != null && !els.isEmpty()) return false;
        }
        return true;
    }

    private static int getVoiceTicksInSegmentForStaff(Segment seg, Staff staff, int voice) {
        List<NoteRestElement> elements = seg.getNoteRestByStaffAndVoice(staff, voice);
        if (!elements.isEmpty()) {
            return elements.get(0).getType().getTicks();
        }
        return seg.getDuration();
    }
}