package org.example.musicscorebuilder.components.music;

import java.util.List;

public enum NoteType {
    THIRTY_SECOND(120, 3),
    SIXTEENTH(240, 2),
    EIGHTH(480, 1),
    QUARTER(960),
    HALF(1920),
    WHOLE(3840);

    private static final NoteType[] VALUES = values();
    private final int ticks;
    private final int beamCount;

    NoteType(int ticks) {
        this.ticks = ticks;
        this.beamCount = 0;
    }

    NoteType(int ticks, int beamCount) {
        this.ticks = ticks;
        this.beamCount = beamCount;
    }

    public int getTicks() { return ticks; }
    public int getBeamCount() { return beamCount; }
    public boolean isEighth() { return this == EIGHTH; }
    public boolean hasFlag() { return List.of(EIGHTH, SIXTEENTH, THIRTY_SECOND).contains(this); }
    public boolean isHalf() { return this == HALF; }
    public boolean isBlack() { return  this != HALF && this != WHOLE; }
}