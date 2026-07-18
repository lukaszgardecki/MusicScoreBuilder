package org.example.musicscorebuilder.components.music;

public class Note {
    private Pitch pitch;
    private int duration;
    private Voice voice;
    private NoteType type;

    public Note(Voice voice, PitchStep pitchStep, int alter, int octave) {
        pitch = new Pitch(pitchStep, alter,  octave);
        duration = 1;
        this.voice = voice;
        type = NoteType.QUARTER;
    }

    public Pitch getPitch() { return pitch; }
    public int getDuration() { return duration; }
    public Voice getVoice() { return voice; }
    public NoteType getType() { return type; }
    public PitchStep getStep() { return pitch.getStep(); }
    public int getStepValue() { return pitch.getStepValue(); }
    public int getAlter() { return pitch.getAlter(); }
    public int getOctave() { return pitch.getOctave(); }

    public void setPitch(PitchStep step, int octave) {
        pitch.setStep(step);
        pitch.setOctave(octave);
    }
}

enum NoteType {
    EIGHTH, QUARTER, HALF, WHOLE
}