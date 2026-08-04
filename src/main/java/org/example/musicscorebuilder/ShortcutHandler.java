package org.example.musicscorebuilder;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
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
        if (event.getCode() == KeyCode.N) {
            modeManager.toggleInsertMode();
            event.consume();
            return;
        } else if (event.getCode() == KeyCode.ESCAPE) {
            scoreStateManager.clearSelection();
            if (modeManager.isInsertMode()) { modeManager.toggleInsertMode(); }
            event.consume();
            return;
        } else if (event.getCode() == KeyCode.DIGIT0 || event.getCode() == KeyCode.NUMPAD0) {
            if (modeManager.isInsertMode()) {

            } else {
                scoreStateManager.convertSelectedNoteToRest();
            }
            event.consume();
            return;
        } else if (event.getCode() == KeyCode.T) {
            if (modeManager.isInsertMode()) {

            } else {
                scoreStateManager.toggleTieForSelectedNote();
            }
            event.consume();
            return;
        } else if (event.getCode() == KeyCode.PERIOD || event.getCode() == KeyCode.DECIMAL) {
            if (modeManager.isInsertMode()) {
                modeManager.toggleDot();
            } else {
                Selectable selected = scoreStateManager.getSelectedItem();
                NoteRestElement currentNRE = null;
                if (selected instanceof NoteLayout nl) {
                    currentNRE = nl.getNote();
                } else if (selected instanceof RestLayout rl) {
                    currentNRE = rl.getRest();
                }

                if (currentNRE != null) {
                    int newDots = currentNRE.getDots() > 0 ? 0 : 1;
                    scoreStateManager.changeSelectedElementDots(newDots);
                }
            }
            event.consume();
            return;
        } else if (modeManager.isInsertMode()) {
            if (event.getCode() == KeyCode.LEFT) {
                scoreNavigator.moveCursorPrev();
                event.consume();
                return;
            } else if (event.getCode() == KeyCode.RIGHT) {
                scoreNavigator.moveCursorNext();
                event.consume();
                return;
            }
        }

        NoteType targetType = switch (event.getCode()) {
            case DIGIT2, NUMPAD2 -> NoteType.THIRTY_SECOND;
            case DIGIT3, NUMPAD3 -> NoteType.SIXTEENTH;
            case DIGIT4, NUMPAD4 -> NoteType.EIGHTH;
            case DIGIT5, NUMPAD5 -> NoteType.QUARTER;
            case DIGIT6, NUMPAD6 -> NoteType.HALF;
            case DIGIT7, NUMPAD7 -> NoteType.WHOLE;
            default -> null;
        };

        if (targetType != null) {
            if (modeManager.isInsertMode()) {
                modeManager.setCurrentNoteType(targetType);
            } else {
                Selectable selected = scoreStateManager.getSelectedItem();
                NoteRestElement currentNRE = null;
                if (selected instanceof NoteLayout nl) {
                    currentNRE = nl.getNote();
                } else if (selected instanceof RestLayout rl) {
                    currentNRE = rl.getRest();
                }

                if (currentNRE != null && currentNRE.getType() == targetType) {
                    event.consume();
                    return;
                }

                scoreStateManager.changeSelectedElementDuration(targetType);
            }
            event.consume();
        }
    }
}