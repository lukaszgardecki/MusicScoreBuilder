package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class Note extends NoteRestElement {
    private Pitch pitch;
    private BeamType beam;
    private boolean tieStart;
    private boolean tieStop;

    @SuppressWarnings("unused")
    private Note() {
        super(null, 0, null);
    }

    public Note(int voice, PitchStep pitchStep, int alter, int octave, NoteType type, BeamType beam, int dots, Measure parent) {
        super(parent, voice, type);
        this.pitch = new Pitch(pitchStep, alter, octave);
        setBeam(beam);
        this.dots = dots;
    }

    public Note(int voice, PitchStep pitchStep, int alter, int octave, NoteType type, BeamType beam, Measure parent) {
        this(voice, pitchStep, alter, octave, type, beam, 0, parent);
    }

    public Pitch getPitch() { return pitch; }
    public BeamType getBeam() { return beam; }
    public boolean isTieStart() { return tieStart; }
    public boolean isTieStop() { return tieStop; }

    @JsonIgnore
    public boolean isBeamed() { return beam != null; }
    @JsonIgnore
    public PitchStep getStep() { return pitch.getStep(); }
    @JsonIgnore
    public int getStepValue() { return pitch.getStepValue(); }
    @JsonIgnore
    public int getAlter() { return pitch.getAlter(); }
    @JsonIgnore
    public int getOctave() { return pitch.getOctave(); }
    @JsonIgnore
    public boolean hasTie() { return isTieStart() || isTieStop(); }

    public void setPitch(Pitch pitch) {
        this.pitch = pitch;
    }
    public void setPitch(PitchStep step, int octave) {
        if (pitch == null) {
            pitch = new Pitch(step, 0, octave);
        } else {
            pitch.setStep(step);
            pitch.setOctave(octave);
        }
    }
    public void setBeam(BeamType beam) {
        this.beam = (beam == BeamType.NONE) ? null : beam;
    }
    public void setTieStart(boolean tieStart) { this.tieStart = tieStart; }
    public void setTieStop(boolean tieStop) { this.tieStop = tieStop; }
}