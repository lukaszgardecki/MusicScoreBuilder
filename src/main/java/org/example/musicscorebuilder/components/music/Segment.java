package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Segment {
    private final SegmentType type;
    private final List<Element> elements = new ArrayList<>();

    public Segment(SegmentType type) {
        this.type = type;

        if (type == SegmentType.CHORDREST) {
            elements.add(createDefaultVoice());
        }
    }

    public void addElement(Element element) {
        elements.add(element);
    }

    public List<Element> getElements() { return elements; }
    public SegmentType getType() { return type; }

    private Voice createDefaultVoice() {
        var voice = new Voice(1);
        voice.add(createDefaultChord(voice));
        voice.add(createDefaultChord(voice));
        voice.add(createDefaultChord(voice));
        voice.add(createDefaultChord(voice));

        return voice;
    }

    private Chord createDefaultChord(Voice voice) {
        Chord chord = new Chord();
        chord.add(new Note(voice, PitchStep.A, 0, 4));
        chord.add(new Note(voice, PitchStep.D, 0, 4));
        return chord;
    }
}
