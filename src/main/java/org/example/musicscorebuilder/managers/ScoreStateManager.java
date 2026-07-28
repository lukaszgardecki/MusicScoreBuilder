package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.RestLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.SelectionChangeListener;
import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ScoreStateManager {
    private static ScoreStateManager instance;
    private final List<ScoreChangeListener> scoreChangeListeners = new ArrayList<>();
    private final List<Selectable> selectedItems = new ArrayList<>();
    private int currentModeIndex = 0;
    private final List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();

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

        Selectable currentSelected = getSelectedItem();
        for (SelectionChangeListener listener : selectionChangeListeners) {
            listener.onSelectionChanged(currentSelected);
        }
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

    public Optional<Selectable> getFirstSelectedNoteRest() {
        if (selectedItems.isEmpty()) return Optional.empty();
        return selectedItems.stream()
                .filter(s -> s.getSegment().getType() == SegmentType.NOTEREST)
                .findFirst();
    }

    public void addScoreChangeListener(ScoreChangeListener listener) {
        scoreChangeListeners.add(listener);
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        selectionChangeListeners.add(listener);
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

    public void changeSelectedElementDuration(NoteType type) {
        Selectable selected = getSelectedItem();
        if (selected == null) return;

        Measure measure = null;
        Staff staff = null;
        Segment targetSegment = null;
        NoteRestElement elementToChange = null;

        if (selected instanceof NoteLayout nl) {
            targetSegment = nl.getParent().getSegment();
            measure = targetSegment.getParent();
            staff = nl.getStaff().getStaff();
            elementToChange = nl.getNote();
        } else if (selected instanceof RestLayout restLayout) {
            targetSegment = restLayout.getParent().getSegment();
            measure = targetSegment.getParent();
            staff = restLayout.getStaff().getStaff();
            elementToChange = restLayout.getRest();
        }

        if (measure == null || targetSegment == null || elementToChange == null) return;

        if (elementToChange.getType() == type) {
            return;
        }

        measure.changeElementDuration(targetSegment, staff, elementToChange, type);

        notifyScoreChanged();
        clearSelection();
    }
}