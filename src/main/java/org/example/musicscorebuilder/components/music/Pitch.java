package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Pitch {
    private static final int[] BASE_SEMITONES = { 0, 2, 4, 5, 7, 9, 11 };
    private PitchStep step;
    private int alter;
    private int octave;

    @SuppressWarnings("unused")
    private Pitch() {}

    @JsonCreator
    public Pitch(
            @JsonProperty("step") PitchStep step,
            @JsonProperty("alter") int alter,
            @JsonProperty("octave") int octave
    ) {
        this.step = step;
        this.alter = alter;
        this.octave = octave;
    }

    public void transpose(int diatonicShift, int chromaticShift) {
        int currentStepIndex = this.step.getValue();
        int totalSteps = currentStepIndex + diatonicShift;

        int newStepIndex = totalSteps % 7;
        int octaveShift = totalSteps / 7;

        if (newStepIndex < 0) {
            newStepIndex += 7;
            octaveShift -= 1;
        }

        PitchStep newStep = PitchStep.values()[newStepIndex];
        int newOctave = this.octave + octaveShift;
        int currentSemitones = (this.octave * 12) + BASE_SEMITONES[currentStepIndex] + this.alter;
        int targetSemitones = currentSemitones + chromaticShift;
        int newBaseSemitones = (newOctave * 12) + BASE_SEMITONES[newStepIndex];

        this.alter = targetSemitones - newBaseSemitones;
        this.step = newStep;
        this.octave = newOctave;
    }

    public PitchStep getStep() { return step; }
    public int getAlter() { return alter; }
    public int getOctave() { return octave; }

    @JsonIgnore
    public int getStepValue() { return step.getValue(); }

    @JsonIgnore
    public int getAbsoluteDiatonicStep() { return octave * 7 + step.getValue(); }

    public void setStep(PitchStep step) { this.step = step; }
    public void setAlter(int alter) { this.alter = alter; }
    public void setOctave(int octave) { this.octave = octave; }
}