package org.example.musicscorebuilder;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ShortcutHandler {
    private final InsertModeManager insertModeManager = InsertModeManager.getInstance();

    public void register(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    public void unregister(Scene scene) {
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.N) {
            insertModeManager.toggleInsertMode();
            event.consume();
        }
    }
}