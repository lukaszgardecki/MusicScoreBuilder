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


}

enum NoteType {
    EIGHTH, QUARTER, HALF, WHOLE
}