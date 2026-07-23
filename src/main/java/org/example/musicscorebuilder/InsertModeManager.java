package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.Mode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InsertModeManager {
    private static InsertModeManager instance;
    private Mode mode = Mode.DISPLAY;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();

    private double currentMouseX = -1;
    private double currentMouseY = -1;
    private final List<Runnable> mouseMoveListeners = new ArrayList<>();

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

    public void activateInsertMode() {
        if (mode != Mode.INSERT) {
            mode = Mode.INSERT;
            stateManager.clearSelection();
            stateManager.notifyScoreChanged();
            notifyListeners();
            System.out.println("Activate Insert Mode");
        }
    }

    public void deactivateInsertMode() {
        if (mode != Mode.DISPLAY) {
            mode = Mode.DISPLAY;
            currentMouseX = -1;
            currentMouseY = -1;
            notifyListeners();
            System.out.println("Deactivate Insert Mode");
        }
    }

    public void updateMousePosition(double x, double y) {
        if (!isInsertMode()) return;
        this.currentMouseX = x;
        this.currentMouseY = y;
        for (Runnable listener : mouseMoveListeners) {
            listener.run();
        }
    }

    public double getCurrentMouseX() { return currentMouseX; }
    public double getCurrentMouseY() { return currentMouseY; }

    public void addMouseMoveListener(Runnable listener) {
        mouseMoveListeners.add(listener);
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