package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.music.Mode;
import org.example.musicscorebuilder.components.music.Score;

import java.util.ArrayList;
import java.util.List;

public class ScoreStateManager {
    private static ScoreStateManager instance;
    private final List<ScoreChangeListener> scoreChangeListeners = new ArrayList<>();
    private Selectable selectedItem = null;
    private int currentModeIndex = 0;

    private ScoreStateManager() {}

    public static synchronized ScoreStateManager getInstance() {
        if (instance == null) {
            instance = new ScoreStateManager();
        }
        return instance;
    }

    public void setCurrentModeIndex(int index) {
        this.currentModeIndex = index;
        notifyScoreChanged();
    }

    public Mode getCurrentMode(Score score) {
        if (score == null || score.getModes().isEmpty()) return null;
        if (currentModeIndex < 0 || currentModeIndex >= score.getModes().size()) {
            return score.getModes().getFirst();
        }
        return score.getModes().get(currentModeIndex);
    }



    public void setSelectedItem(Selectable item) {
        if (this.selectedItem != null) {
            this.selectedItem.setSelected(false);
        }

        this.selectedItem = item;

        if (this.selectedItem != null) {
            this.selectedItem.setSelected(true);
        }
    }

    public Selectable getSelectedItem() {
        return selectedItem;
    }

    public void clearSelection() {
        setSelectedItem(null);
    }




    public void addScoreChangeListener(ScoreChangeListener listener) {
        scoreChangeListeners.add(listener);
    }

    public void notifyScoreChanged() {
        for (ScoreChangeListener l : scoreChangeListeners) {
            l.onScoreChanged();
        }
    }
}