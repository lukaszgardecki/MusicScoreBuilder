package org.example.musicscorebuilder.components.music;

public enum KeySigType {
    F1(Leland.ACC_FLAT, new double[] { 2.0 }),
    F2(Leland.ACC_FLAT, new double[] { 2.0, 0.5 }),
    F3(Leland.ACC_FLAT, new double[] { 2.0, 0.5, 2.5 }),
    F4(Leland.ACC_FLAT, new double[] { 2.0, 0.5, 2.5, 1.0 }),
    F5(Leland.ACC_FLAT, new double[] { 2.0, 0.5, 2.5, 1.0, 3.0 }),
    F6(Leland.ACC_FLAT, new double[] { 2.0, 0.5, 2.5, 1.0, 3.0, 1.5 }),
    F7(Leland.ACC_FLAT, new double[] { 2.0, 0.5, 2.5, 1.0, 3.0, 1.5, 3.5 }),

    S1(Leland.ACC_SHARP, new double[] { 0.0 }),
    S2(Leland.ACC_SHARP, new double[] { 0.0, 1.5 }),
    S3(Leland.ACC_SHARP, new double[] { 0.0, 1.5, -0.5 }),
    S4(Leland.ACC_SHARP, new double[] { 0.0, 1.5, -0.5, 1.0 }),
    S5(Leland.ACC_SHARP, new double[] { 0.0, 1.5, -0.5, 1.0, 2.5 }),
    S6(Leland.ACC_SHARP, new double[] { 0.0, 1.5, -0.5, 1.0, 2.5, 0.5 }),
    S7(Leland.ACC_SHARP, new double[] { 0.0, 1.5, -0.5, 1.0, 2.5, 0.5, 2.0 });

    private final Leland fontData;
    private final double[] offsetsY;
    private static final PitchStep[] SHARP_ORDER = {
            PitchStep.F, PitchStep.C, PitchStep.G, PitchStep.D, PitchStep.A, PitchStep.E, PitchStep.B
    };

    private static final PitchStep[] FLAT_ORDER = {
            PitchStep.B, PitchStep.E, PitchStep.A, PitchStep.D, PitchStep.G, PitchStep.C, PitchStep.F
    };

    KeySigType(Leland fontData, double[] offsetY) {
        this.fontData = fontData;
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

    public Leland getFontData() { return fontData; }
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
        int count = offsetsY.length;

        if (fontData == Leland.ACC_SHARP) {
            for (int i = 0; i < count; i++) {
                if (SHARP_ORDER[i] == step) return 1; // Ten dźwięk jest podwyższony przez tonację
            }
        } else if (fontData == Leland.ACC_FLAT) {
            for (int i = 0; i < count; i++) {
                if (FLAT_ORDER[i] == step) return -1; // Ten dźwięk jest obniżony przez tonację
            }
        }
        return 0; // Ten dźwięk nie jest zmieniany przez tę tonację
    }
}