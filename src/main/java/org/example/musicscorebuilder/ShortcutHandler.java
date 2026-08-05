package org.example.musicscorebuilder;

import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.RestLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.music.NoteRestElement;
import org.example.musicscorebuilder.components.music.NoteType;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

public class ShortcutHandler {
    private final ScoreStateManager scoreStateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();

    public void register(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    public void unregister(Scene scene) {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent event) {
        boolean handled = true;

        switch (event.getCode()) {
            case N               -> modeManager.toggleInsertMode();
            case ESCAPE          -> handleEscape();
            case DIGIT0, NUMPAD0 -> handleZero();
            case T               -> handleTie();
            case PERIOD, DECIMAL -> handleDot();
            case LEFT            -> scoreNavigator.movePrev();
            case RIGHT           -> scoreNavigator.moveNext();
            case UP              -> scoreStateManager.transposeSelectedNoteUp();
            case DOWN            -> scoreStateManager.transposeSelectedNoteDown();

            case DIGIT2, NUMPAD2 -> handleNoteDuration(NoteType.THIRTY_SECOND);
            case DIGIT3, NUMPAD3 -> handleNoteDuration(NoteType.SIXTEENTH);
            case DIGIT4, NUMPAD4 -> handleNoteDuration(NoteType.EIGHTH);
            case DIGIT5, NUMPAD5 -> handleNoteDuration(NoteType.QUARTER);
            case DIGIT6, NUMPAD6 -> handleNoteDuration(NoteType.HALF);
            case DIGIT7, NUMPAD7 -> handleNoteDuration(NoteType.WHOLE);

            default -> handled = false;
        }

        if (handled) {
            event.consume();
        }
    }

    private void handleEscape() {
        scoreStateManager.clearSelection();
        if (modeManager.isInsertMode()) {
            modeManager.toggleInsertMode();
        }
    }

    private void handleZero() {
        if (modeManager.isInsertMode()) {
            // Miejsce na obsługę 0 w trybie wprowadzania
        } else {
            scoreStateManager.convertSelectedNoteToRest();
        }
    }

    private void handleTie() {
        if (modeManager.isInsertMode()) {
            // Miejsce na obsługę łuku w trybie wprowadzania
        } else {
            scoreStateManager.toggleTieForSelectedNote();
        }
    }

    private void handleDot() {
        if (modeManager.isInsertMode()) {
            modeManager.toggleDot();
        } else {
            NoteRestElement currentNRE = getSelectedNoteRestElement();
            if (currentNRE != null) {
                int newDots = currentNRE.getDots() > 0 ? 0 : 1;
                scoreStateManager.changeSelectedElementDots(newDots);
            }
        }
    }

    private void handleNoteDuration(NoteType targetType) {
        if (modeManager.isInsertMode()) {
            modeManager.setCurrentNoteType(targetType);
        } else {
            NoteRestElement currentNRE = getSelectedNoteRestElement();
            if (currentNRE != null && currentNRE.getType() != targetType) {
                scoreStateManager.changeSelectedElementDuration(targetType);
            }
        }
    }

    private NoteRestElement getSelectedNoteRestElement() {
        Selectable selected = scoreStateManager.getSelectedItem();
        if (selected instanceof NoteLayout nl) {
            return nl.getNote();
        } else if (selected instanceof RestLayout rl) {
            return rl.getRest();
        }
        return null;
    }
}