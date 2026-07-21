package org.example.musicscorebuilder.components.music;

import java.util.concurrent.ThreadLocalRandom;

public enum NoteType {
    EIGHTH, QUARTER, HALF, WHOLE;

    public static NoteType getRandom() {
        NoteType[] values = NoteType.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(values.length);
        return values[randomIndex];
    }
}
