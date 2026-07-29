package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.music.util.MeasureDurationEditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ScoreStateManager {
    private static ScoreStateManager instance;
    private final List<ScoreChangeListener> scoreChangeListeners = new ArrayList<>();
    private final List<Selectable> selectedItems = new ArrayList<>();
    private int currentModeIndex = 0;
    private final List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();
    private Consumer<ScoreLayout> postRefreshAction;

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

    public void applyPostRefreshAction(ScoreLayout layout) {
        if (postRefreshAction != null && layout != null) {
            var action = postRefreshAction;
            postRefreshAction = null;
            action.accept(layout);
        }
    }

    public void changeSelectedElementDuration(NoteType type) {
        Selectable selected = getSelectedItem();
        if (selected == null) return;

        SegmentLayout segLayout = selected.getSegment();
        StaffLayout staffLayout = selected.getStaff();
        if (segLayout == null || staffLayout == null) return;

        final Segment targetSegment = segLayout.getSegment();
        if (targetSegment == null) return;

        final Staff staff = staffLayout.getStaff();
        final Measure measure = targetSegment.getParent();

        final NoteRestElement elementToChange;
        final int targetVoice;

        if (selected instanceof NoteLayout nl) {
            elementToChange = nl.getNote();
            targetVoice = nl.getNote().getVoice();
        } else if (selected instanceof RestLayout rl) {
            elementToChange = rl.getRest();
            targetVoice = rl.getRest().getVoice();
        } else {
            return;
        }

        if (elementToChange == null || measure == null || staff == null) return;
        if (elementToChange.getType() == type) return;

        postRefreshAction = layout -> {
            var staffElements = targetSegment.getElementsByStaff(staff);
            if (staffElements != null) {
                for (Element el : staffElements) {
                    if (el instanceof NoteRestElement nre && nre.getVoice() == targetVoice) {
                        Selectable newLayout = LayoutHitTester.findSelectableForElement(layout.getPages(), targetSegment, staff, nre);
                        if (newLayout != null) {
                            setSelected(newLayout);
                        }
                        break;
                    }
                }
            }
        };

        measure.changeElementDuration(targetSegment, staff, elementToChange, type);
        notifyScoreChanged();
    }
}