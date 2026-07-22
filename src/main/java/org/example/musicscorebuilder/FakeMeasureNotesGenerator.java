package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.*;

import java.util.*;

public class FakeMeasureNotesGenerator {

    public static void fillMeasureWithTwoVoices(Measure measure, int totalCapacity) {
        var staves = measure.getStaves();
        List<Note> s1v1 = generateNotesForVoice(1, totalCapacity, 5);
        List<Note> s1v2 = generateNotesForVoice(2, totalCapacity, 4);

        List<Note> s2v1 = staves.size() == 2 ? generateNotesForVoice(1, totalCapacity, 3) : List.of();
        List<Note> s2v2 = staves.size() == 2 ? generateNotesForVoice(2, totalCapacity, 2) : List.of();

        buildSegmentsFromVoices(measure, s1v1, s1v2, s2v1, s2v2);
    }

    public static int getMeasureCapacityInSegments(TimeSignature timeSig) {
        int segmentsPerBeat = switch (timeSig.getBeatType()) {
            case 1 -> NoteType.WHOLE.getSegments();
            case 2 -> NoteType.HALF.getSegments();
            case 4 -> NoteType.QUARTER.getSegments();
            case 8 -> NoteType.EIGHTH.getSegments();
            default -> 2;
        };
        return timeSig.getBeat() * segmentsPerBeat;
    }

    private static List<Note> generateNotesForVoice(int voice, int totalCapacity, int octave) {
        List<Note> notes = new ArrayList<>();
        int remainingSegments = totalCapacity;

        while (remainingSegments > 0) {
            NoteType randomType = NoteType.getRandomFitting(remainingSegments);
            PitchStep randomStep = PitchStep.values()[(int) (Math.random() * PitchStep.values().length)];

            notes.add(new Note(voice, randomStep, 0, octave, randomType));
            remainingSegments -= randomType.getSegments();
        }
        return notes;
    }

    private static void buildSegmentsFromVoices(Measure measure,
                                         List<Note> s1v1, List<Note> s1v2,
                                         List<Note> s2v1, List<Note> s2v2) {
        var staves = measure.getStaves();
        Map<Integer, List<Note>> s1Map = groupNotesByOffset(s1v1, s1v2);
        Map<Integer, List<Note>> s2Map = groupNotesByOffset(s2v1, s2v2);

        Set<Integer> sortedOffsets = new TreeSet<>();
        sortedOffsets.addAll(s1Map.keySet());
        sortedOffsets.addAll(s2Map.keySet());

        for (int offset : sortedOffsets) {
            Segment seg = new Segment(SegmentType.CHORDREST, staves);

            if (s1Map.containsKey(offset)) {
                for (Note note : s1Map.get(offset)) {
                    seg.addElement(staves.getFirst(), note);
                }
            }

            if (staves.size() == 2 && s2Map.containsKey(offset)) {
                for (Note note : s2Map.get(offset)) {
                    seg.addElement(staves.get(1), note);
                }
            }

            measure.getSegments().add(seg);
        }
    }

    private static Map<Integer, List<Note>> groupNotesByOffset(List<Note> voice1, List<Note> voice2) {
        Map<Integer, List<Note>> map = new HashMap<>();
        addVoiceToMap(map, voice1);
        addVoiceToMap(map, voice2);
        return map;
    }

    private static void addVoiceToMap(Map<Integer, List<Note>> map, List<Note> voiceNotes) {
        int currentOffset = 0;
        for (Note note : voiceNotes) {
            map.computeIfAbsent(currentOffset, k -> new ArrayList<>()).add(note);
            currentOffset += note.getType().getSegments();
        }
    }
}
