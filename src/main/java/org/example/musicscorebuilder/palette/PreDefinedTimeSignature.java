package org.example.musicscorebuilder.palette;

import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.TimeSignature;

public enum PreDefinedTimeSignature {
    TWO_FOUR(2, 4, TimeSignature.Type.FRACTIONAL),
    THREE_FOUR(3, 4, TimeSignature.Type.FRACTIONAL),
    FOUR_FOUR(4, 4, TimeSignature.Type.FRACTIONAL),
    FIVE_FOUR(5, 4, TimeSignature.Type.FRACTIONAL),
    SIX_FOUR(6, 4, TimeSignature.Type.FRACTIONAL),
    THREE_EIGHT(3, 8, TimeSignature.Type.FRACTIONAL),
    FOUR_EIGHT(4, 8, TimeSignature.Type.FRACTIONAL),
    FIVE_EIGHT(5, 8, TimeSignature.Type.FRACTIONAL),
    SIX_EIGHT(6, 8, TimeSignature.Type.FRACTIONAL),
    SEVEN_EIGHT(7, 8, TimeSignature.Type.FRACTIONAL),
    NINE_EIGHT(9, 8, TimeSignature.Type.FRACTIONAL),
    TWO_TWO(2, 2, TimeSignature.Type.FRACTIONAL),
    THREE_TWO(3, 2, TimeSignature.Type.FRACTIONAL),

    COMMON(4, 4, TimeSignature.Type.COMMON),
    CUT(2, 2, TimeSignature.Type.CUT);

    private final int beat;
    private final int beatType;
    private final TimeSignature.Type type;

    PreDefinedTimeSignature(int beat, int beatType, TimeSignature.Type type) {
        this.beat = beat;
        this.beatType = beatType;
        this.type = type;
    }

    public int getBeat() { return beat; }
    public int getBeatType() { return beatType; }
    public TimeSignature.Type getType() { return type; }
    public boolean isFractional() { return type == TimeSignature.Type.FRACTIONAL; }

    public String getTopGlyph() {
        if (!isFractional()) {
            return type == TimeSignature.Type.COMMON ? Leland.TIME_COMMON.getCode() : Leland.TIME_CUT.getCode();
        }
        return getDigitGlyph(beat).getCode();
    }

    public String getBottomGlyph() {
        if (!isFractional()) return null;
        return getDigitGlyph(beatType).getCode();
    }

    private Leland getDigitGlyph(int digit) {
        return switch (digit) {
            case 0 -> Leland.TIME_0;
            case 1 -> Leland.TIME_1;
            case 2 -> Leland.TIME_2;
            case 3 -> Leland.TIME_3;
            case 4 -> Leland.TIME_4;
            case 5 -> Leland.TIME_5;
            case 6 -> Leland.TIME_6;
            case 7 -> Leland.TIME_7;
            case 8 -> Leland.TIME_8;
            case 9 -> Leland.TIME_9;
            default -> throw new IllegalArgumentException("Unsupported time signature digit: " + digit);
        };
    }
}