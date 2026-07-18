package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Measure {
    private Barline rightBarline;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();

    public Measure(BarlineStyle barlineStyle, List<Staff> staves) {
        this.staves = staves;
        this.rightBarline = new Barline(barlineStyle, Barline.Type.END);
    }

    public void addEndBarlineSegment(BarlineStyle style) {
        var element = new Barline(style, Barline.Type.END);
        Segment seg = new Segment(SegmentType.BARLINE, staves);

        for (Staff staff : staves) {
            seg.addElement(staff, element);
        }
        segments.add(seg);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }

    public void addChordRestSegmentAtEnd() {
        Segment seg = new Segment(SegmentType.CHORDREST, staves);

        var staff1 = staves.get(0);
        seg.addElement(staff1, createDefaultVoice(staff1, 4));

        if (staves.size() == 2) {
            var staff2 = staves.get(1);
            seg.addElement(staff2, createDefaultVoice(staff2, 3));
        }

        if (segments.isEmpty()) {
            segments.add(seg);
        } else {
            segments.add(segments.size() - 1, seg);
        }
    }

    private Voice createDefaultVoice(Staff staff, int octave) {
        Voice voice = new Voice(1, staff);
        voice.add(createDefaultChord(voice, octave));
        voice.add(createDefaultChord(voice, octave));
        voice.add(createDefaultChord(voice, octave));
        return voice;
    }

    private Chord createDefaultChord(Voice voice, int octave) {
        Chord chord = new Chord();
        PitchStep[] steps = PitchStep.values();
        var random = ThreadLocalRandom.current();

        PitchStep randomP1 = steps[random.nextInt(steps.length)];
        PitchStep randomP2;

        do {
            randomP2 = steps[random.nextInt(steps.length)];
        } while (randomP2 == randomP1);

        chord.add(new Note(voice, randomP1, 0, octave));
        chord.add(new Note(voice, randomP2, 0, octave));
        return chord;
    }
}