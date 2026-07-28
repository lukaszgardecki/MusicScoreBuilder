package org.example.musicscorebuilder;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        } else if (event.getCode() == KeyCode.ESCAPE) {
            scoreStateManager.clearSelection();
            if (modeManager.isInsertMode()) { modeManager.toggleInsertMode(); }
            event.consume();
        } else if (modeManager.isInsertMode()) {
            if (event.getCode() == KeyCode.LEFT) {
                scoreNavigator.moveCursorPrev();
                event.consume();
            } else if (event.getCode() == KeyCode.RIGHT) {
                scoreNavigator.moveCursorNext();
                event.consume();
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
            scoreStateManager.changeSelectedElementDuration(targetType);
            event.consume();
        }
    }
}