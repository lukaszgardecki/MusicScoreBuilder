package org.example.musicscorebuilder.components.music;

public class ModeFactory {

    public static Mode createSoloMode() {
        Mode mode = new Mode(ModeType.SOLO);
        mode.appendMeasures(10);
        return mode;
    }

    public static Mode createHarmonyMode() {
        Mode mode = new Mode(ModeType.HARMONY);
        mode.appendMeasures(10);
        return mode;
    }
}
