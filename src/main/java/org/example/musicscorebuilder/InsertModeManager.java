package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.music.Mode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InsertModeManager {
    private static InsertModeManager instance;
    private Mode mode = Mode.DISPLAY;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();
    private SegmentLayout editedSegment = null;

    private InsertModeManager() {}

    public static synchronized InsertModeManager getInstance() {
        if (instance == null) {
            instance = new InsertModeManager();
        }
        return instance;
    }

    public void toggleInsertMode() {
        if (isInsertMode()) {
            deactivateInsertMode();
        } else {
            activateInsertMode();
        }
    }

    public void setEditedSegment(SegmentLayout newSegment) {
        if (this.editedSegment != newSegment) {
            if (this.editedSegment != null) {
                this.editedSegment.setHighlighted(false);
            }
            this.editedSegment = newSegment;
        }

        if (this.editedSegment != null) {
            this.editedSegment.setHighlighted(true);
        }
    }

    public SegmentLayout getEditedSegment() {
        return editedSegment;
    }


    private void activateInsertMode() {
        if (mode != Mode.INSERT) {
            mode = Mode.INSERT;
            stateManager.clearSelection();
            stateManager.notifyScoreChanged();

            if (this.editedSegment != null) {
                this.editedSegment.setHighlighted(true);
            }

            notifyListeners();
            System.out.println("Activate Insert Mode");
        }
    }

    private void deactivateInsertMode() {
        if (mode != Mode.DISPLAY) {
            mode = Mode.DISPLAY;
            if (this.editedSegment != null) {
                this.editedSegment.setHighlighted(false);
            }
            notifyListeners();
            System.out.println("Deactivate Insert Mode");
        }
    }

    private void notifyListeners() {
        boolean active = isInsertMode();
        for (Consumer<Boolean> listener : listeners) {
            listener.accept(active);
        }
    }

    public void addModeChangeListener(Consumer<Boolean> listener) {
        listeners.add(listener);
        listener.accept(isInsertMode());
    }

    public boolean isInsertMode() {
        return mode == Mode.INSERT;
    }
}