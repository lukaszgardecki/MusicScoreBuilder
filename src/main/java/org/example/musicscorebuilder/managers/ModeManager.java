package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.components.music.Mode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModeManager {
    private static ModeManager instance;
    private Mode mode = Mode.DISPLAY;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final List<Consumer<Boolean>> listeners = new ArrayList<>();
    private GhostNoteLayout ghostNote;
    private int currentVoice = 1;

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

    public void setGhostNote(GhostNoteLayout ghostNote) { this.ghostNote = ghostNote; }
    public void clearGhostNote() { this.ghostNote = null; }
    public GhostNoteLayout getGhostNote() { return ghostNote; }
    public boolean isShowGhost() { return isInsertMode() && ghostNote != null; }

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
            clearGhostNote();
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

    public boolean isInsertMode() { return mode == Mode.INSERT; }
    public int getCurrentVoice() { return currentVoice; }

    public void setCurrentVoice(int voice) {
        this.currentVoice = voice;
        if (ghostNote != null) {
            clearGhostNote();
        }
        notifyListeners();
    }
}