package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.ElementLayout;

import java.util.ArrayList;
import java.util.List;

public class ScoreStateManager {
    private static ScoreStateManager instance;
    private final List<ScoreChangeListener> scoreChangeListeners = new ArrayList<>();
    private ElementLayout selectedElement = null;

    private ScoreStateManager() {}

    public static synchronized ScoreStateManager getInstance() {
        if (instance == null) {
            instance = new ScoreStateManager();
        }
        return instance;
    }

    public void setSelectedElement(ElementLayout element) {
        this.selectedElement = element;
    }

    public ElementLayout getSelectedElement() {
        return selectedElement;
    }

    public void clearSelection() {
        this.selectedElement = null;
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