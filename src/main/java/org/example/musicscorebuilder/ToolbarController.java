package org.example.musicscorebuilder;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class ToolbarController {
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final InsertModeManager modeManager = InsertModeManager.getInstance();

    @FXML private Button modeButton;

    @FXML
    private void toggleMode() {
        ObservableList<String> styleClasses = modeButton.getStyleClass();
        if (styleClasses.contains("active")) {
            styleClasses.remove("active");
            modeManager.deactivateInsertMode();
        } else {
            styleClasses.add("active");
            modeManager.activateInsertMode();
        }
    }

    @FXML
    private void addMeasure() {
        scoreService.getScore().addNewMeasure();
        stateManager.notifyScoreChanged();
    }

    @FXML
    private void removeMeasure() {
        scoreService.getScore().removeLastMeasure();
        stateManager.notifyScoreChanged();
    }
}
