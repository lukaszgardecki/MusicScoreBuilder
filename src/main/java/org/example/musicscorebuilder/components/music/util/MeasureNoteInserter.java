package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.ArrayList;
import java.util.List;

public class MeasureNoteInserter {

    public static Segment insertNote(Measure measure, Segment targetSegment, Staff staff, Note newNote) {
        if (targetSegment.getType() != SegmentType.NOTEREST) return null;

        int voice = newNote.getVoice();
        int newTicks = newNote.getType().getTicks();

        // 1. Obliczamy punkt startowy oraz całkowity czas taktu oparty w 100% na gridzie
        int startTick = 0;
        int totalMeasureTicks = 0;
//        int totalMeasureTicks = measure.getTimeSignature().getTotalTicks();
        boolean targetFound = false;

        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                if (seg == targetSegment) {
                    targetFound = true;
                }
                if (!targetFound) {
                    startTick += seg.getDuration();
                }
                totalMeasureTicks += seg.getDuration();
            }
        }
        System.out.println("startTick: " + startTick);
        System.out.println("totalMeasureTicks: " + totalMeasureTicks);
        if (!targetFound) return null;

        int targetEndTick = startTick + newTicks;
        System.out.println("targetEndTick: " + targetEndTick);
        if (targetEndTick > totalMeasureTicks) return null; // Nuta przekracza ramy taktu





//        int startTick = measure.getStartTickOfSegment(targetSegment);
//        System.out.println("startTick: " + startTick);
//        if (startTick < 0) return null;
//
//        int totalMeasureTicks = measure.getTimeSignature().getTotalTicks();
//        System.out.println("totalMeasureTicks: " + totalMeasureTicks);
//        int targetEndTick = startTick + newTicks;
//        System.out.println("targetEndTick: " + targetEndTick);
//        if (targetEndTick > totalMeasureTicks) return null; // Nuta przekracza ramy taktu
//        System.out.println("Przechodzi nulla");











        // 2. Tniemy siatkę idealnie na końcu naszej nowej nuty (aby następna miała gdzie wylądować)
        ensureSegmentBoundaryAt(measure, targetEndTick, staff, voice);

        // 3. Wycinamy wszystko w edytowanym głosie, co wchodzi w kolizję z naszą nową nutą
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDuration = seg.getDuration();

                if (currentTick >= startTick && currentTick < targetEndTick) {
                    List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);
                    for (NoteRestElement el : new ArrayList<>(nres)) {
                        seg.removeNoteRest(staff, el);
                    }
                }
                currentTick += segDuration;
            }
        }

        // 4. Czyste wstawienie nowej nuty
        targetSegment.insertNote(staff, newNote);

        measure.getSegments().removeIf(seg -> seg.isNoteRest() && seg.isEmpty());

        // 5. INTELIGENTNE WYPŁNIANIE TAKTU: Naprawia braki pauz i nigdy nie rozpycha siatki!
        fillVoiceGaps(measure, staff, voice);

        measure.updateResolutionFromSegments();
        measure.setDirty(true);


        System.out.println("SEGMENTY:");
        int i = 0;
        for (Segment s : measure.getSegments()) {
            System.out.println(i++ + ": dur=" + s.getDuration() + " type=" + s.getType());
        }

        return findNextFreeSegment(measure, targetEndTick);
    }

    private static void fillVoiceGaps(Measure measure, Staff staff, int voice) {
        int currentTick = 0;
        int activeUntil = 0;

        for (Segment seg : measure.getSegments()) {
            if (seg.isNoteRest()) {
                int segDuration = seg.getDuration();

                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);

                if (!nres.isEmpty()) {
                    activeUntil = Math.max(activeUntil, currentTick + nres.get(0).getType().getTicks());
                } else {
                    if (currentTick >= activeUntil && segDuration > 0) {
                        NoteType fillType = MeasureTimeSignatureAdjuster.findLargestFittingNoteType(segDuration);
                        if (fillType != null) {
                            seg.addElement(staff, new Rest(voice, fillType, measure));
                            activeUntil = currentTick + fillType.getTicks();
                        }
                    }
                }
                currentTick += segDuration;
            }
        }
    }

    private static void ensureSegmentBoundaryAt(Measure measure, int targetTick, Staff targetStaff, int targetVoice) {
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

            if (targetTick > currentTick && targetTick < segEnd) {
                int part1Ticks = targetTick - currentTick;
                int part2Ticks = segEnd - targetTick;

                List<NoteType> part1Types = decomposeTicksToNoteTypes(part1Ticks);
                List<NoteType> part2Types = decomposeTicksToNoteTypes(part2Ticks);

                if (part1Types.isEmpty()) return;
                NoteType firstType = part1Types.getFirst();

                // A. Modyfikujemy dotychczasowy segment
                for (Staff s : measure.getStaves()) {
                    for (int v = 1; v <= 4; v++) {
                        List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(s, v);
                        boolean isTarget = (s == targetStaff && v == targetVoice);

                        if (!nres.isEmpty() && nres.getFirst() instanceof Rest rest) {
                            seg.removeNoteRest(s, rest);
                            seg.addElement(s, new Rest(v, firstType, measure));
                        } else if (isTarget) {
                            // Stabilizator siatki dla edytowanego głosu
                            seg.addElement(s, new Rest(v, firstType, measure));
                        }
                    }
                }

                // B. Tworzymy nowe segmenty (dla reszty czasu)
                int insertIndex = i + 1;
                List<NoteType> combined = allTypesCombined(part1Types, part2Types);

                for (int k = 1; k < combined.size(); k++) {
                    NoteType subType = combined.get(k);
                    Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);
                    boolean addedAny = false;

                    for (Staff s : measure.getStaves()) {
                        for (int v = 1; v <= 4; v++) {
                            List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(s, v);
                            if (!nres.isEmpty()) {
                                NoteRestElement oldEl = nres.getFirst();
                                if (oldEl instanceof Rest) {
                                    fillSeg.addElement(s, new Rest(v, subType, measure));
                                    addedAny = true;
                                }
                            }
                        }
                    }

                    if (addedAny) {
                        segments.add(insertIndex, fillSeg);
                        insertIndex++;
                    }
                }
                return;
            }
            currentTick += segDuration;
        }
    }

    private static List<NoteType> allTypesCombined(List<NoteType> t1, List<NoteType> t2) {
        List<NoteType> combined = new ArrayList<>(t1);
        combined.addAll(t2);
        return combined;
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
            if (seg.getType() == SegmentType.NOTEREST) {
                if (currentTick >= targetEndTick) {
                    return seg;
                }
                currentTick += seg.getDuration();
            }
        }

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
}