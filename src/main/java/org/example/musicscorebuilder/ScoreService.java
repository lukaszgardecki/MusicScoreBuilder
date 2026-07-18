package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.ScoreFactory;
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
        this.score = ScoreFactory.createScore();
        return score;
    }
}
