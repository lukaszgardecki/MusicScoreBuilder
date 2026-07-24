package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.CursorLayout;
import org.example.musicscorebuilder.components.music.Mode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModeManager {
    private static ModeManager instance;
    private Mode mode = Mode.DISPLAY;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();
    private CursorLayout cursorLayout;

    private ModeManager() {}

    public static synchronized ModeManager getInstance() {
        if (instance == null) {
            instance = new ModeManager();
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

    public CursorLayout getLastCursor() { return cursorLayout; }
    public void setCursorLayout(CursorLayout newCursorLayout) { this.cursorLayout = newCursorLayout; }

    private void activateInsertMode() {
        if (mode != Mode.INSERT) {
            mode = Mode.INSERT;
            stateManager.notifyScoreChanged();
            notifyListeners();
        }
    }

    private void deactivateInsertMode() {
        if (mode != Mode.DISPLAY) {
            mode = Mode.DISPLAY;
            notifyListeners();
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