package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.InstrumentFactory;
import org.example.musicscorebuilder.components.music.Score;

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
        Score score = new Score("Utwór", "Kompozytor");

        score.add(InstrumentFactory.createPiano());
        score.add(InstrumentFactory.createPiano());
        score.add(InstrumentFactory.createOrgan());
        score.addMeasures(10);

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
