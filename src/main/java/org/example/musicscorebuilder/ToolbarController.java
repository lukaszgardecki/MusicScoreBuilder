package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import org.example.musicscorebuilder.components.music.Measure;

public class ToolbarController {
    private final ScoreService scoreService;

    public ToolbarController() {
        scoreService = ScoreService.getInstance();
    }

    @FXML
    private void addMeasure() {
        scoreService.getScore().add(new Measure());
        scoreService.notifyListeners();
    }

    @FXML
    private void removeMeasure() {
        scoreService.getScore().removeLastMeasure();
        scoreService.notifyListeners();
    }
}
