package org.example.musicscorebuilder;

import javafx.fxml.FXML;

public class ToolbarController {
    private final ScoreService scoreService;

    public ToolbarController() {
        scoreService = ScoreService.getInstance();
    }

    @FXML
    private void addMeasure() {
        scoreService.getScore().addNewMeasure();
        scoreService.notifyListeners();
    }

    @FXML
    private void removeMeasure() {
        scoreService.getScore().removeLastMeasure();
        scoreService.notifyListeners();
    }
}
