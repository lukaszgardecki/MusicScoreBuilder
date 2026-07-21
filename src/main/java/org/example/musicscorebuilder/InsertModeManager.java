package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.music.Mode;

public class InsertModeManager {
    private static InsertModeManager instance;
    private Mode mode = Mode.DISPLAY;

    private InsertModeManager() {}

    public static synchronized InsertModeManager getInstance() {
        if (instance == null) {
            instance = new InsertModeManager();
        }
        return instance;
    }

    public void deactivateInsertMode() {
        mode = Mode.INSERT;
        System.out.println("Deactivate Insert Mode");
    }

    public void activateInsertMode() {
        mode = Mode.DISPLAY;
        System.out.println("Activate Insert Mode");
    }
}