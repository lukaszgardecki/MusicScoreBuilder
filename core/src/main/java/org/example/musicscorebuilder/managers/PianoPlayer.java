package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.music.Pitch;

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
            case C -> 0;
            case D -> 2;
            case E -> 4;
            case F -> 5;
            case G -> 7;
            case A -> 9;
            case B -> 11;
        };

        return (pitch.getOctave() + 1) * 12 + stepOffset + pitch.getAlter();
    }
}