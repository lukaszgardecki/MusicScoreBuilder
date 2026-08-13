package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.music.ModeType;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;

public class ScoreFactory {
    private static final int measuresCount = 32;

    public static ScoreMode createSoloMode(Score score) {
        ScoreMode scoreMode = new ScoreMode(score, ModeType.SOLO);
        scoreMode.appendMeasures(measuresCount);
        return scoreMode;
    }

    public static ScoreMode createHarmonyMode(Score score) {
        ScoreMode scoreMode = new ScoreMode(score, ModeType.HARMONY);
        scoreMode.appendMeasures(measuresCount);
        return scoreMode;
    }

    public static Score createScore() {
        Score score = new Score("000", "000", "Tytuł", "Podtytuł", "Kompozytor");
        score.add(createSoloMode(score));
        score.add(createHarmonyMode(score));
        return score;
    }
}
