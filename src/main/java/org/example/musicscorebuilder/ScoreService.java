package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;

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
        Part piano = new Part("Piano1");
        piano.add(new Staff());
        piano.add(new Staff());

        Part piano2 = new Part("Piano2");
        piano2.add(new Staff());
        piano2.add(new Staff());
        piano2.add(new Staff());

        Part piano3 = new Part("Piano3");
        piano3.add(new Staff());
        piano3.add(new Staff());

        score.add(piano);
        score.add(piano2);
        score.add(piano3);

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
