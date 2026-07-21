package org.example.musicscorebuilder.components.music;

public class Note extends Element {
    private Pitch pitch;
    private int voice;
    private NoteType type;
    private int duration;

    public Note(int voice, PitchStep pitchStep, int alter, int octave, NoteType type) {
        this.pitch = new Pitch(pitchStep, alter, octave);
        this.voice = voice;
        this.type = type;
        this.duration = type.getSegments();
    }

    public Pitch getPitch() { return pitch; }
    public int getDuration() { return duration; }
    public int getVoice() { return voice; }
    public NoteType getType() { return type; }
    public PitchStep getStep() { return pitch.getStep(); }
    public int getStepValue() { return pitch.getStepValue(); }
    public int getAlter() { return pitch.getAlter(); }
    public int getOctave() { return pitch.getOctave(); }

    public void setPitch(PitchStep step, int octave) {
        pitch.setStep(step);
        pitch.setOctave(octave);
    }
    public void setType(NoteType type) {
        this.type = type;
        this.duration = type.getSegments();
    }
}