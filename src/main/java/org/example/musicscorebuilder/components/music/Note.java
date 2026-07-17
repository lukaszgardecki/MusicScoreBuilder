package org.example.musicscorebuilder.components.music;

public class Note {
    private Pitch pitch;
    private int duration;
    private Voice voice;
    private NoteType type;

    public Note(Voice voice) {
        pitch = new Pitch(PitchStep.F, 0, 4);
        duration = 1;
        this.voice = voice;
        type = NoteType.QUARTER;
    }
}

class Pitch {
    private final PitchStep step;
    private final int alter;
    private final int octave;

    public Pitch(PitchStep step, int alter, int octave) {
        this.step = step;
        this.alter = alter;
        this.octave = octave;
    }
}

enum PitchStep {
    C, D, E, F, G, A, H
}

enum NoteType {
    EIGHTH, QUARTER, HALF, WHOLE
}