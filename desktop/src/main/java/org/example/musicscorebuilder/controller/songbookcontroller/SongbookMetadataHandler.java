package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.data.FileService;
import org.example.musicscorebuilder.data.StorageService;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class SongbookMetadataHandler {
    private final VBox metadataContainer;
    private final TextField numberField;
    private final TextField oldNumberField;
    private final TextField titleField;
    private final TextField subtitleField;
    private final TextField composerField;

    private final StorageService storageService = StorageService.getInstance();
    private final FileService fileService = FileService.getInstance();
    private final PauseTransition liveUpdateDebounce = new PauseTransition(Duration.millis(200));

    private boolean isUpdatingFields = false;
    private Consumer<File> onFileRenamedCallback;

    public SongbookMetadataHandler(
            VBox metadataContainer, TextField numberField, TextField oldNumberField,
            TextField titleField, TextField subtitleField, TextField composerField
    ) {
        this.metadataContainer = metadataContainer;
        this.numberField = numberField;
        this.oldNumberField = oldNumberField;
        this.titleField = titleField;
        this.subtitleField = subtitleField;
        this.composerField = composerField;
    }

    public void init(Consumer<File> onFileRenamedCallback) {
        this.onFileRenamedCallback = onFileRenamedCallback;
        setupPlaceholders();
        setupMetadataAutoSave();
        ScoreStateManager.getInstance().addScoreChangeListener(this::updatePanel);
        clearAndDisable();
    }

    public void updatePanel() {
        Score activeScore = storageService.getScore();
        if (activeScore != null) {
            isUpdatingFields = true;
            try {
                numberField.setText(activeScore.getNumberNew() != null ? activeScore.getNumberNew() : "");
                oldNumberField.setText(activeScore.getNumberOld() != null ? activeScore.getNumberOld() : "");
                titleField.setText(activeScore.getTitle() != null ? activeScore.getTitle() : "");
                subtitleField.setText(activeScore.getSubtitle() != null ? activeScore.getSubtitle() : "");
                composerField.setText(activeScore.getComposer() != null ? activeScore.getComposer() : "");

                setFieldsDisabled(false);
                metadataContainer.setVisible(true);
            } finally {
                isUpdatingFields = false;
            }
        } else {
            clearAndDisable();
        }
    }

    public void clearAndDisable() {
        liveUpdateDebounce.stop();
        isUpdatingFields = true;
        try {
            numberField.clear();
            oldNumberField.clear();
            titleField.clear();
            subtitleField.clear();
            composerField.clear();

            setFieldsDisabled(true);
            metadataContainer.setVisible(true);
        } finally {
            isUpdatingFields = false;
        }
    }

    private void setFieldsDisabled(boolean disabled) {
        numberField.setDisable(disabled);
        oldNumberField.setDisable(disabled);
        titleField.setDisable(disabled);
        subtitleField.setDisable(disabled);
        composerField.setDisable(disabled);
    }

    private void setupPlaceholders() {
        numberField.setPromptText("123");
        oldNumberField.setPromptText("123");
        titleField.setPromptText("Tytuł");
        subtitleField.setPromptText("Podtytuł");
        composerField.setPromptText("Kompozytor");
    }

    private void setupMetadataAutoSave() {
        liveUpdateDebounce.setOnFinished(e -> {
            if (isUpdatingFields) return;
            Score activeScore = storageService.getScore();
            if (activeScore != null) {
                applyFieldsToScore(activeScore);
                ScoreStateManager.getInstance().notifyScoreChanged();
            }
        });

        ChangeListener<String> liveUpdateListener = (obs, oldVal, newVal) -> {
            if (isUpdatingFields) return;
            liveUpdateDebounce.playFromStart();
        };

        numberField.textProperty().addListener(liveUpdateListener);
        oldNumberField.textProperty().addListener(liveUpdateListener);
        titleField.textProperty().addListener(liveUpdateListener);
        subtitleField.textProperty().addListener(liveUpdateListener);
        composerField.textProperty().addListener(liveUpdateListener);

        ChangeListener<Boolean> focusChangeListener = (obs, wasFocused, isFocused) -> {
            if (!isFocused && !isUpdatingFields) {
                saveMetadataChanges();
            }
        };

        numberField.focusedProperty().addListener(focusChangeListener);
        oldNumberField.focusedProperty().addListener(focusChangeListener);
        titleField.focusedProperty().addListener(focusChangeListener);
        subtitleField.focusedProperty().addListener(focusChangeListener);
        composerField.focusedProperty().addListener(focusChangeListener);

        numberField.setOnAction(e -> saveMetadataChanges());
        oldNumberField.setOnAction(e -> saveMetadataChanges());
        titleField.setOnAction(e -> saveMetadataChanges());
        subtitleField.setOnAction(e -> saveMetadataChanges());
        composerField.setOnAction(e -> saveMetadataChanges());
    }

    private void applyFieldsToScore(Score activeScore) {
        activeScore.setNumberNew(numberField.getText());
        activeScore.setNumberOld(oldNumberField.getText());
        activeScore.setTitle(titleField.getText());
        activeScore.setSubtitle(subtitleField.getText());
        activeScore.setComposer(composerField.getText());
    }

    private void saveMetadataChanges() {
        File currentOpenedFile = storageService.getCurrentFile();
        if (currentOpenedFile == null || isUpdatingFields) return;

        liveUpdateDebounce.stop();
        Score activeScore = storageService.getScore();
        if (activeScore == null) return;

        applyFieldsToScore(activeScore);

        File parentDir = currentOpenedFile.getParentFile();
        if (parentDir == null) return;

        File targetFile = fileService.getTargetFile(activeScore, parentDir);

        if (!targetFile.equals(currentOpenedFile) && targetFile.exists()) {
            SongbookDialogHelper.showErrorAlert("Błąd zapisu", "Plik o nazwie '" + targetFile.getName() + "' już istnieje!");
            return;
        }

        try {
            if (!targetFile.equals(currentOpenedFile)) {
                fileService.deleteRecursively(currentOpenedFile);
            }

            storageService.saveScoreFile(activeScore, parentDir);
            storageService.setCurrentFile(targetFile);

            ScoreStateManager.getInstance().notifyScoreChanged();

            if (onFileRenamedCallback != null) {
                onFileRenamedCallback.accept(targetFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
            SongbookDialogHelper.showErrorAlert("Błąd zapisu", "Nie udało się zapisać zmian metadanych: " + e.getMessage());
        }
    }
}