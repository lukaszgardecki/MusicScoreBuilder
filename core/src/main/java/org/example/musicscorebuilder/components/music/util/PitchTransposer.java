package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.Pitch;
import org.example.musicscorebuilder.components.music.PitchStep;

public class PitchTransposer {

    public static void transposeUp(Pitch pitch) {
        // 1. Jeśli nuta ma bemol (alter < 0), skasuj bemol (zwiększ alter o 1)
        if (pitch.getAlter() < 0) {
            pitch.setAlter(pitch.getAlter() + 1);
            return;
        }

        // 2. Jeśli nuta jest czysta (alter == 0)
        if (pitch.getAlter() == 0) {
            // Półtony naturalne: E -> F oraz B -> C (pomijamy E# i B#)
            if (pitch.getStep() == PitchStep.E) {
                pitch.setStep(PitchStep.F);
            } else if (pitch.getStep() == PitchStep.B) {
                pitch.setStep(PitchStep.C);
                pitch.setOctave(pitch.getOctave() + 1);
            } else {
                // Dla C, D, F, G, A dodajemy krzyżyk (#)
                pitch.setAlter(1);
            }
            return;
        }

        // 3. Jeśli nuta miała już krzyżyk (alter > 0), przechodzimy do następnego stopnia z alter = 0
        if (pitch.getAlter() > 0) {
            pitch.setAlter(0);
            moveToNextDiatonicStep(pitch);
        }
    }

    public static void transposeDown(Pitch pitch) {
        // 1. Jeśli nuta ma krzyżyk (alter > 0), skasuj krzyżyk (zmniejsz alter o 1)
        if (pitch.getAlter() > 0) {
            pitch.setAlter(pitch.getAlter() - 1);
            return;
        }

        // 2. Jeśli nuta jest czysta (alter == 0)
        if (pitch.getAlter() == 0) {
            // Półtony naturalne: F -> E oraz C -> B (pomijamy Fb i Cb)
            if (pitch.getStep() == PitchStep.F) {
                pitch.setStep(PitchStep.E);
            } else if (pitch.getStep() == PitchStep.C) {
                pitch.setStep(PitchStep.B);
                pitch.setOctave(pitch.getOctave() - 1);
            } else {
                // Dla D, E, G, A, B dodajemy bemol (b)
                pitch.setAlter(-1);
            }
            return;
        }

        // 3. Jeśli nuta miała już bemol (alter < 0), przechodzimy do poprzedniego stopnia z alter = 0
        if (pitch.getAlter() < 0) {
            pitch.setAlter(0);
            moveToPrevDiatonicStep(pitch);
        }
    }

    private static void moveToNextDiatonicStep(Pitch pitch) {
        switch (pitch.getStep()) {
            case C -> pitch.setStep(PitchStep.D);
            case D -> pitch.setStep(PitchStep.E);
            case E -> pitch.setStep(PitchStep.F);
            case F -> pitch.setStep(PitchStep.G);
            case G -> pitch.setStep(PitchStep.A);
            case A -> pitch.setStep(PitchStep.B);
            case B -> {
                pitch.setStep(PitchStep.C);
                pitch.setOctave(pitch.getOctave() + 1);
            }
        }
    }

    private static void moveToPrevDiatonicStep(Pitch pitch) {
        switch (pitch.getStep()) {
            case C -> {
                pitch.setStep(PitchStep.B);
                pitch.setOctave(pitch.getOctave() - 1);
            }
            case D -> pitch.setStep(PitchStep.C);
            case E -> pitch.setStep(PitchStep.D);
            case F -> pitch.setStep(PitchStep.E);
            case G -> pitch.setStep(PitchStep.F);
            case A -> pitch.setStep(PitchStep.G);
            case B -> pitch.setStep(PitchStep.A);
        }
    }
}