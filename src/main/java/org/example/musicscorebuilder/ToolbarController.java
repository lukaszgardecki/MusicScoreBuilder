package org.example.musicscorebuilder;

import javafx.fxml.FXML;

public class ToolbarController {
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();

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
