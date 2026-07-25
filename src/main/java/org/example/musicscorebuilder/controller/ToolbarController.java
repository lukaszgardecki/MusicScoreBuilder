package org.example.musicscorebuilder.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

public class ToolbarController {
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();

    @FXML private Button modeButton;
    @FXML private Button voice1Button;
    @FXML private Button voice2Button;

    @FXML
    public void initialize() {
        modeManager.addModeChangeListener(isInsert -> {
            ObservableList<String> modeBtnClasses = modeButton.getStyleClass();
            ObservableList<String> voice1BtnClasses = voice1Button.getStyleClass();
            ObservableList<String> voice2BtnClasses = voice2Button.getStyleClass();

            voice1BtnClasses.remove("active");
            voice2BtnClasses.remove("active");

            if (isInsert) {
                if (!modeBtnClasses.contains("active")) modeBtnClasses.add("active");

                int voice = modeManager.getCurrentVoice();
                switch (voice) {
                    case 1 -> voice1BtnClasses.add("active");
                    case 2 -> voice2BtnClasses.add("active");
                }
            } else {
                modeBtnClasses.remove("active");
            }
        });
    }

    @FXML
    private void toggleMode() {
        modeManager.toggleInsertMode();
    }

    @FXML
    private void activateVoice1() {
        modeManager.setCurrentVoice(1);
        modeManager.clearGhostNote();
        if (!modeManager.isInsertMode()) {
            modeManager.toggleInsertMode();
        } else {
            scoreNavigator.switchToVoice(1);
        }
        updateVoiceButtonStyles(1);
    }

    @FXML
    private void activateVoice2() {
        modeManager.setCurrentVoice(2);
        modeManager.clearGhostNote();
        if (!modeManager.isInsertMode()) {
            modeManager.toggleInsertMode();
        } else {
            scoreNavigator.switchToVoice(2);
        }
        updateVoiceButtonStyles(2);
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

    private void updateVoiceButtonStyles(int activeVoice) {
        voice1Button.getStyleClass().remove("active");
        voice2Button.getStyleClass().remove("active");
        if (activeVoice == 1) {
            voice1Button.getStyleClass().add("active");
        } else if (activeVoice == 2) {
            voice2Button.getStyleClass().add("active");
        }
    }
}
