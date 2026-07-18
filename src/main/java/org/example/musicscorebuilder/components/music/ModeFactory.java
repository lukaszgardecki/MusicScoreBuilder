package org.example.musicscorebuilder.components.music;

public class ModeFactory {
    private static final int measuresCount = 5;

    public static Mode createSoloMode() {
        Mode mode = new Mode(ModeType.SOLO);
        mode.appendMeasures(measuresCount);
        return mode;
    }

    public static Mode createHarmonyMode() {
        Mode mode = new Mode(ModeType.HARMONY);
        mode.appendMeasures(measuresCount);
        return mode;
    }
}
