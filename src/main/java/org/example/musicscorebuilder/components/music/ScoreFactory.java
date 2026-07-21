package org.example.musicscorebuilder.components.music;

public class ScoreFactory {
    private static final int measuresCount = 32;

    public static ScoreMode createSoloMode() {
        ScoreMode scoreMode = new ScoreMode(ModeType.SOLO);
        scoreMode.appendMeasures(measuresCount);
        return scoreMode;
    }

    public static ScoreMode createHarmonyMode() {
        ScoreMode scoreMode = new ScoreMode(ModeType.HARMONY);
        scoreMode.appendMeasures(measuresCount);
        return scoreMode;
    }

    public static Score createScore() {
        Score score = new Score();
        score.add(createSoloMode());
        score.add(createHarmonyMode());
        return score;
    }
}
