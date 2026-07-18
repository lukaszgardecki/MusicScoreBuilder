package org.example.musicscorebuilder.components.music;

public class ScoreFactory {
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

    public static Score createScore() {
        Score score = new Score();
        score.add(createSoloMode());
        score.add(createHarmonyMode());
        return score;
    }
}
