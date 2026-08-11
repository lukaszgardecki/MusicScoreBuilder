package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.components.music.Mode;
import org.example.musicscorebuilder.components.music.NoteType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModeManager {
    private static ModeManager instance;
    private Mode mode = Mode.DISPLAY;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();

    private final List<Consumer<Boolean>> modeListeners = new ArrayList<>();
    private final List<Consumer<NoteType>> noteTypeListeners = new ArrayList<>();

    private GhostNoteLayout ghostNote;
    private int currentVoice = 1;
    private NoteType currentNoteType = NoteType.QUARTER;
    private boolean dotted = false;

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

    public void toggleEditLyricsMode() {
        if (mode == Mode.EDIT_TEXT) {
            mode = Mode.DISPLAY;
        } else {
            mode = Mode.EDIT_TEXT;
        }
    }

    public NoteType getCurrentNoteType() {
        return currentNoteType;
    }

    public boolean isDotted() {
        return dotted;
    }

    public void toggleDot() {
        setDotted(!this.dotted);
    }

    public void setDotted(boolean dotted) {
        this.dotted = dotted;
        rebuildGhostNote();
        notifyNoteTypeListeners();
        stateManager.notifyScoreChanged();
        notifyListeners();
    }

    public void clearGhostNote() { this.ghostNote = null; }
    public GhostNoteLayout getGhostNote() { return ghostNote; }
    public boolean isShowGhost() { return isInsertMode() && ghostNote != null; }

    public void addModeChangeListener(Consumer<Boolean> listener) {
        modeListeners.add(listener);
        listener.accept(isInsertMode());
    }

    public void addNoteTypeChangeListener(Consumer<NoteType> listener) {
        noteTypeListeners.add(listener);
        listener.accept(currentNoteType);
    }

    public boolean isInsertMode() { return mode == Mode.INSERT; }
    public boolean isEditTextMode() { return mode == Mode.EDIT_TEXT; }
    public int getCurrentVoice() { return currentVoice; }

    public void setGhostNote(GhostNoteLayout ghostNote) { this.ghostNote = ghostNote; }

    public void setCurrentVoice(int voice) {
        this.currentVoice = voice;
        if (ghostNote != null) {
            clearGhostNote();
        }
        notifyListeners();
    }

    public void setCurrentNoteType(NoteType noteType) {
        this.currentNoteType = noteType;
        this.dotted = false;
        rebuildGhostNote();

        notifyNoteTypeListeners();
        stateManager.notifyScoreChanged();
        notifyListeners();
    }

    private void rebuildGhostNote() {
        if (ghostNote != null) {
            StaffLayout staffLayout = ghostNote.getStaff();
            SegmentLayout segmentLayout = ghostNote.getSegment();

            if (staffLayout != null && segmentLayout != null) {
                this.ghostNote = new GhostNoteLayout(segmentLayout, staffLayout, ghostNote.getY());
            }
        }
    }

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
        for (Consumer<Boolean> listener : modeListeners) {
            listener.accept(active);
        }
    }

    private void notifyNoteTypeListeners() {
        for (Consumer<NoteType> listener : noteTypeListeners) {
            listener.accept(currentNoteType);
        }
    }
}