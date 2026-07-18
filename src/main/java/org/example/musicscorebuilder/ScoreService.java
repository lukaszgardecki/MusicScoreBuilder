package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.ModeFactory;
import org.example.musicscorebuilder.components.music.Score;

public class ScoreService {
    private static ScoreService instance;
    private Score score;

    private ScoreService() {}

    public static synchronized ScoreService getInstance() {
        if (instance == null) {
            instance = new ScoreService();
        }
        return instance;
    }

    public Score getScore() {
        if (score != null) return score;
        Score newScore = new Score();
        newScore.add(ModeFactory.createSoloMode());
        newScore.add(ModeFactory.createHarmonyMode());
        this.score = newScore;
        return score;
    }
}
