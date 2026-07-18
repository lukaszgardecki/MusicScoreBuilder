package org.example.musicscorebuilder.components.music;

public class Pitch {
    private final PitchStep step;
    private final int alter;
    private final int octave;

    public Pitch(PitchStep step, int alter, int octave) {
        this.step = step;
        this.alter = alter;
        this.octave = octave;
    }

    public PitchStep getStep() { return step; }
    public int getStepValue() { return step.getValue(); }
    public int getAlter() { return alter; }
    public int getOctave() { return octave; }
}