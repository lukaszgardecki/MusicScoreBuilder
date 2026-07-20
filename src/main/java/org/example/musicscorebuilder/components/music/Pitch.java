package org.example.musicscorebuilder.components.music;

public class Pitch {
    private PitchStep step;
    private int alter;
    private int octave;

    public Pitch(PitchStep step, int alter, int octave) {
        this.step = step;
        this.alter = alter;
        this.octave = octave;
    }

    public PitchStep getStep() { return step; }
    public int getStepValue() { return step.getValue(); }
    public int getAbsoluteDiatonicStep() { return octave * 7 + step.getValue(); }
    public int getAlter() { return alter; }
    public int getOctave() { return octave; }

    public void setStep(PitchStep step) { this.step = step; }
    public void setAlter(int alter) { this.alter = alter; }
    public void setOctave(int octave) { this.octave = octave; }
}