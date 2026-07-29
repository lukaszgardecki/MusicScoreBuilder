package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum NoteType {
    THIRTY_SECOND(120),
    SIXTEENTH(240),
    EIGHTH(480),
    QUARTER(960),
    HALF(1920),
    WHOLE(3840);

    private static final NoteType[] VALUES = values();
    private final int ticks;

    NoteType(int ticks) {
        this.ticks = ticks;
    }

    public static NoteType fromTicks(int ticks) {
        for (NoteType type : NoteType.values()) {
            if (type.getTicks() == ticks) {
                return type;
            }
        }
        throw new IllegalArgumentException("Nieznana wartość ticków dla NoteType: " + ticks);
    }

    public int getTicks() { return ticks; }
    public boolean isEighth() { return this == EIGHTH; }
    public boolean isHalf() { return this == HALF; }
    public boolean isBlack() { return  this != HALF && this != WHOLE; }
    public static NoteType getRandom() { return VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)]; }

    public static NoteType getRandomFitting(int maxSegments) {
        List<NoteType> fitting = new ArrayList<>();
        for (NoteType type : VALUES) {
            if (type.getTicks() <= maxSegments) {
                fitting.add(type);
            }
        }
        if (fitting.isEmpty()) return EIGHTH;
        return fitting.get(ThreadLocalRandom.current().nextInt(fitting.size()));
    }
}