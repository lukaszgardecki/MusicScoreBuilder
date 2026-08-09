package org.example.musicscorebuilder.controller.util.audio;

import org.example.musicscorebuilder.components.music.Pitch;
import org.example.musicscorebuilder.components.music.PitchStep;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PianoPlayer {
    private static PianoPlayer instance;
    private Synthesizer synthesizer;
    private MidiChannel pianoChannel;

    private PianoPlayer() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            pianoChannel = synthesizer.getChannels()[0];
            setInstrument(MidiInstrument.ACOUSTIC_GRAND_PIANO);
        } catch (Exception e) {
            System.err.println("Nie udało się zainicjować MIDI: " + e.getMessage());
        }
    }

    public static PianoPlayer getInstance() {
        if (instance == null) {
            instance = new PianoPlayer();
        }
        return instance;
    }

    public void playNote(Pitch pitch) {
        final int durationMs = 300;
        final int midiNoteNumber = getMidi(pitch);
        if (pianoChannel == null) return;

        int velocity = 80; // (0-127)
        pianoChannel.noteOn(midiNoteNumber, velocity);

        CompletableFuture.delayedExecutor(durationMs, TimeUnit.MILLISECONDS)
                .execute(() -> pianoChannel.noteOff(midiNoteNumber));
    }

    public void setInstrument(MidiInstrument instrument) {
        int number = instrument.getProgramNumber();
        if (pianoChannel != null && number >= 0 && number <= 127) {
            pianoChannel.programChange(number);
        }
    }


    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
    }

    private int getMidi(Pitch pitch) {
        int stepOffset = switch (pitch.getStep()) {
            case PitchStep.C -> 0;
            case PitchStep.D -> 2;
            case PitchStep.E -> 4;
            case PitchStep.F -> 5;
            case PitchStep.G -> 7;
            case PitchStep.A -> 9;
            case PitchStep.B -> 11;
        };

        return (pitch.getOctave() + 1) * 12 + stepOffset + pitch.getAlter();
    }
}