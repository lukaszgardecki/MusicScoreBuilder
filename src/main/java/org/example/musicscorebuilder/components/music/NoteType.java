package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public enum NoteType {
    EIGHTH(1),
    QUARTER(2),
    HALF(4),
    WHOLE(8);

    private static final NoteType[] VALUES = values();
    private final int segments;

    NoteType(int segments) {
        this.segments = segments;
    }

    public int getSegments() { return segments; }
    public boolean isHalf() { return this == HALF; }
    public boolean isBlack() { return  this != HALF && this != WHOLE; }
    public static NoteType getRandom() { return VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)]; }

    public static NoteType getRandomFitting(int maxSegments) {
        List<NoteType> fitting = new ArrayList<>();
        for (NoteType type : VALUES) {
            if (type.getSegments() <= maxSegments) {
                fitting.add(type);
            }
        }
        if (fitting.isEmpty()) return EIGHTH;
        return fitting.get(ThreadLocalRandom.current().nextInt(fitting.size()));
    }
}