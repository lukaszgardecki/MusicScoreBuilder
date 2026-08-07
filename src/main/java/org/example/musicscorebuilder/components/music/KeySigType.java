package org.example.musicscorebuilder.components.music;

import java.util.EnumSet;

public enum KeySigType {
    F1(new double[] { 2.0 }),
    F2(new double[] { 2.0, 0.5 }),
    F3(new double[] { 2.0, 0.5, 2.5 }),
    F4(new double[] { 2.0, 0.5, 2.5, 1.0 }),
    F5(new double[] { 2.0, 0.5, 2.5, 1.0, 3.0 }),
    F6(new double[] { 2.0, 0.5, 2.5, 1.0, 3.0, 1.5 }),
    F7(new double[] { 2.0, 0.5, 2.5, 1.0, 3.0, 1.5, 3.5 }),

    S1(new double[] { 0.0 }),
    S2(new double[] { 0.0, 1.5 }),
    S3(new double[] { 0.0, 1.5, -0.5 }),
    S4(new double[] { 0.0, 1.5, -0.5, 1.0 }),
    S5(new double[] { 0.0, 1.5, -0.5, 1.0, 2.5 }),
    S6(new double[] { 0.0, 1.5, -0.5, 1.0, 2.5, 0.5 }),
    S7(new double[] { 0.0, 1.5, -0.5, 1.0, 2.5, 0.5, 2.0 });

    private final double[] offsetsY;
    private static final EnumSet<KeySigType> FLATS = EnumSet.of(F1, F2, F3, F4, F5, F6, F7);
    private static final PitchStep[] SHARP_ORDER = {
            PitchStep.F, PitchStep.C, PitchStep.G, PitchStep.D, PitchStep.A, PitchStep.E, PitchStep.B
    };
    private static final PitchStep[] FLAT_ORDER = {
            PitchStep.B, PitchStep.E, PitchStep.A, PitchStep.D, PitchStep.G, PitchStep.C, PitchStep.F
    };

    KeySigType(double[] offsetY) {
        this.offsetsY = offsetY;
    }

    public static KeySigType of(int value) {
        if (value == 0) return null;
        if (value < -7 || value > 7) {
            throw new IllegalArgumentException("Key signature value must be between -7 and 7, but was: " + value);
        }

        if (value < 0) return KeySigType.valueOf("F" + Math.abs(value));
        else return KeySigType.valueOf("S" + value);
    }

    public boolean isFlat() {
        return FLATS.contains(this);
    }

    public int getFifths() {
        return isFlat() ? -offsetsY.length : offsetsY.length;
    }

    public Leland getFontData() {
        return isFlat() ? Leland.ACC_FLAT : Leland.ACC_SHARP;
    }
    public double[] getOffsetsY(ClefType type) {
        double[] result = new double[offsetsY.length];
        double shift = switch(type) {
            case F -> 1.0;
            case C -> 0.5;
            case G -> 0.0;
        };

        for (int i = 0; i < offsetsY.length; i++) {
            result[i] = offsetsY[i] + shift;
        }

        return result;
    }

    public int getAlterForStep(PitchStep step) {
        PitchStep[] order = isFlat() ? FLAT_ORDER : SHARP_ORDER;
        int alter = isFlat() ? -1 : 1;

        for (int i = 0; i < offsetsY.length; i++) {
            if (order[i] == step) {
                return alter;
            }
        }
        return 0;
    }
}