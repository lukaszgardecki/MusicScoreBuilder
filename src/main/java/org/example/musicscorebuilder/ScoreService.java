package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class ScoreService {
    private List<ScoreChangeListener> listeners = new ArrayList<>();
    private static ScoreService instance;
    private Score score;

    private ScoreService() {}

    public static ScoreService getInstance() {
        if (instance == null) instance = new ScoreService();
        return instance;
    }

    public Score getScore() {
        if  (score != null) return score;
        Score score = new Score();
        score.add(ModeFactory.createHarmonyMode());
        this.score = score;
        return score;
    }

    public void addListener(ScoreChangeListener listener) {
        listeners.add(listener);
    }

    public void notifyListeners() {
        for (ScoreChangeListener l : listeners) {
            l.onScoreChanged();
        }
    }
}
