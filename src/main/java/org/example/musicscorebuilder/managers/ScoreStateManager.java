package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ScoreStateManager {
    private static ScoreStateManager instance;
    private final List<ScoreChangeListener> scoreChangeListeners = new ArrayList<>();
    private final List<Selectable> selectedItems = new ArrayList<>();
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

    public ScoreMode getCurrentMode(Score score) {
        if (score == null || score.getModes().isEmpty()) return null;
        if (currentModeIndex < 0 || currentModeIndex >= score.getModes().size()) {
            return score.getModes().getFirst();
        }
        return score.getModes().get(currentModeIndex);
    }


    public void setSelected(Selectable item) {
        List<Selectable> itemsToSelect = LayoutHitTester.resolveSelection(item);
        deselectAll();
        selectAll(itemsToSelect);
    }

    public void clearSelection() {
        setSelected(null);
    }

    public List<Selectable> getSelectedItems() {
        return selectedItems;
    }

    public Selectable getSelectedItem() {
        return selectedItems.isEmpty() ? null : selectedItems.getFirst();
    }

    public SegmentLayout getSelectedSegment() {
        if (selectedItems.isEmpty()) return null;
        return selectedItems.stream()
                .map(Selectable::getParentSegment)
                .filter(s -> s.getType() == SegmentType.NOTEREST)
                .findFirst().orElse(null);
    }

    public void addScoreChangeListener(ScoreChangeListener listener) {
        scoreChangeListeners.add(listener);
    }

    public void notifyScoreChanged() {
        for (ScoreChangeListener l : scoreChangeListeners) {
            l.onScoreChanged();
        }
    }

    private void selectAll(Collection<? extends Selectable> items) {
        if (items != null) {
            for (Selectable item : items) {
                if (item != null) {
                    selectedItems.add(item);
                    item.setSelected(true);
                }
            }
        }
    }

    private void deselectAll() {
        for (Selectable item : selectedItems) {
            if (item != null) {
                item.setSelected(false);
            }
        }
        selectedItems.clear();
    }
}