package org.example.musicscorebuilder.components.music;

import java.util.stream.IntStream;

public class InstrumentFactory {

    public static Part createPiano() {
        Part piano = new Part("Piano");
        IntStream.range(0, 2).forEach(i -> piano.add(new Staff()));
        return piano;
    }

    public static Part createOrgan() {
        Part piano = new Part("Organ");
        IntStream.range(0, 3).forEach(i -> piano.add(new Staff()));
        return piano;
    }
}
