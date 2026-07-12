package org.example.musicscorebuilder.components.music;

public class InstrumentFactory {

    public static Part createPiano() {
        Part piano = new Part("Piano");
        piano.add(new Staff(ClefType.G));
        piano.add(new Staff(ClefType.F));
        return piano;
    }

    public static Part createOrgan() {
        Part piano = new Part("Organ");
        piano.add(new Staff(ClefType.G));
        piano.add(new Staff(ClefType.F));
        piano.add(new Staff(ClefType.C));
        return piano;
    }
}
