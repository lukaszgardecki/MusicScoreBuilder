package org.example.musicscorebuilder.palette;

import org.example.musicscorebuilder.components.music.TimeSignature;

public enum PreDefinedTimeSignature {
    TWO_FOUR(2, 4),
    THREE_FOUR(3, 4),
    FOUR_FOUR(4, 4),
    FIVE_FOUR(5, 4),
    SIX_FOUR(6, 4),
    THREE_EIGHT(3, 8),
    FOUR_EIGHT(4, 8),
    FIVE_EIGHT(5, 8),
    SIX_EIGHT(6, 8),
    SEVEN_EIGHT(7, 8),
    NINE_EIGHT(9, 8),
    TWO_TWO(2, 2),
    THREE_TWO(3, 2),

    COMMON(4, 4, TimeSignature.Type.COMMON),
    CUT(2, 2, TimeSignature.Type.CUT);

    private final int beat;
    private final int beatType;
    private final TimeSignature.Type type;

    PreDefinedTimeSignature(int beat, int beatType) {
        this.beat = beat;
        this.beatType = beatType;
        this.type = TimeSignature.Type.FRACTIONAL;
    }

    PreDefinedTimeSignature(int beat, int beatType, TimeSignature.Type type) {
        this.beat = beat;
        this.beatType = beatType;
        this.type = type;
    }

    public int getBeat() { return beat; }
    public int getBeatType() { return beatType; }
    public TimeSignature.Type getType() { return type; }
}