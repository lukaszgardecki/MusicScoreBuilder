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
        boolean targetFound = false;

        for (Segment seg : measure.getSegments()) {
            if (seg.getType() == SegmentType.NOTEREST) {
                if (seg == targetSegment) {
                    targetFound = true;
                }
                if (!targetFound) {
                    startTick += seg.getDuration();
                }
                totalMeasureTicks += seg.getDuration();
            }
        }

        if (!targetFound) return null;

        int targetEndTick = startTick + newTicks;
        if (targetEndTick > totalMeasureTicks) {
            return null; // Nuta przekracza ramy taktu
        }

        // 2. Tniemy siatkę idealnie na końcu naszej nowej nuty (aby następna miała gdzie wylądować)
        ensureSegmentBoundaryAt(measure, targetEndTick, staff, voice);

        // 3. Wycinamy wszystko w edytowanym głosie, co wchodzi w kolizję z naszą nową nutą
        int currentTick = 0;
        for (Segment seg : measure.getSegments()) {
            if (seg.getType() == SegmentType.NOTEREST) {
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

        // 5. INTELIGENTNE WYPŁNIANIE TAKTU: Naprawia braki pauz i nigdy nie rozpycha siatki!
        fillVoiceGaps(measure, staff, voice);

        measure.updateResolutionFromSegments();
        measure.setDirty(true);

        return findNextFreeSegment(measure, targetEndTick);
    }

    /**
     * Skanuje cały takt i wstawia idealnie docięte pauzy tam, gdzie głos jest "pusty".
     * Szanuje nuty trwające przez kilka segmentów.
     */
    private static void fillVoiceGaps(Measure measure, Staff staff, int voice) {
        int currentTick = 0;
        int activeUntil = 0; // Śledzi, do kiedy trwa dźwięk w tym głosie

        for (Segment seg : measure.getSegments()) {
            if (seg.getType() == SegmentType.NOTEREST) {
                int segDuration = seg.getDuration();

                List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(staff, voice);

                if (!nres.isEmpty()) {
                    // Jeśli segment zawiera nutę/pauzę w tym głosie, aktualizujemy czas aktywności
                    activeUntil = Math.max(activeUntil, currentTick + nres.get(0).getType().getTicks());
                } else {
                    // Jeśli w tym segmencie głos jest fizycznie pusty, sprawdzamy, czy w tle brzmi jakaś nuta
                    if (currentTick >= activeUntil && segDuration > 0) {
                        // Głos milczy całkowicie. Wstawiamy pauzę dociętą EXACTLY do długości segmentu.
                        // Dzięki temu NIGDY nie rozepchniemy siatki.
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

    /**
     * Dba o to, by we wskazanym ticku istniało fizyczne cięcie w liście segmentów.
     */
    private static void ensureSegmentBoundaryAt(Measure measure, int targetTick, Staff targetStaff, int targetVoice) {
        List<Segment> segments = measure.getSegments();
        int currentTick = 0;

        for (int i = 0; i < segments.size(); i++) {
            Segment seg = segments.get(i);
            if (seg.getType() != SegmentType.NOTEREST) {
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

                NoteType firstType = part1Types.get(0);

                // A. Modyfikujemy dotychczasowy segment
                for (Staff s : measure.getStaves()) {
                    for (int v = 1; v <= 4; v++) {
                        List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(s, v);
                        boolean isTarget = (s.equals(targetStaff) && v == targetVoice);

                        if (!nres.isEmpty()) {
                            NoteRestElement oldEl = nres.get(0);
                            if (oldEl instanceof Rest) {
                                seg.removeNoteRest(s, oldEl);
                                seg.addElement(s, new Rest(v, firstType, measure));
                            }
                        } else if (isTarget) {
                            // Stabilizator siatki dla edytowanego głosu
                            seg.addElement(s, new Rest(v, firstType, measure));
                        }
                    }
                }

                // B. Tworzymy nowe segmenty (dla reszty czasu)
                int insertIndex = i + 1;
                for (int k = 1; k < allTypesCombined(part1Types, part2Types).size(); k++) {
                    NoteType subType = allTypesCombined(part1Types, part2Types).get(k);
                    Segment fillSeg = new Segment(SegmentType.NOTEREST, measure);

                    for (Staff s : measure.getStaves()) {
                        for (int v = 1; v <= 4; v++) {
                            List<NoteRestElement> nres = seg.getNoteRestByStaffAndVoice(s, v);
                            if (!nres.isEmpty()) {
                                NoteRestElement oldEl = nres.get(0);
                                if (oldEl instanceof Rest) {
                                    fillSeg.addElement(s, new Rest(v, subType, measure));
                                }
                            }
                        }
                    }

                    segments.add(insertIndex, fillSeg);
                    insertIndex++;
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