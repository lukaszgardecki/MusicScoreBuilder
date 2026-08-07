package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Pitch {
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