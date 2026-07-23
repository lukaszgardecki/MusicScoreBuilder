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
    public void initialize() {
        modeManager.addModeChangeListener(isInsert -> {
            ObservableList<String> styleClasses = modeButton.getStyleClass();
            if (isInsert) {
                if (!styleClasses.contains("active")) {
                    styleClasses.add("active");
                }
            } else {
                styleClasses.remove("active");
            }
        });
    }

    @FXML
    private void toggleMode() {
        modeManager.toggleInsertMode();
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
