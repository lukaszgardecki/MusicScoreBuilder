package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Staff;

public class ToolbarController {
    private final ScoreService scoreService;

    public ToolbarController() {
        scoreService = ScoreService.getInstance();
    }

    @FXML
    private void addStaff() {
        Part part = new Part("asdf");
        part.add(new Staff(3));
        part.add(new Staff(3));
        scoreService.getScore().add(part);
        ScoreService.getInstance().notifyListeners();
    }

    @FXML
    private void removeStaff() {
        scoreService.getScore().removeLast();
        ScoreService.getInstance().notifyListeners();
    }
}
