package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.ArrayList;
import java.util.List;

public class VerseListController {
    private final ListView<String> listView;
    private final Button addVerseButton;
    private final Button deleteVerseButton;
    private final ScoreStateManager stateManager;

    private boolean isUpdatingList = false;

    public VerseListController(ListView<String> listView,
                               Button addVerseButton,
                               Button deleteVerseButton,
                               ScoreStateManager stateManager) {
        this.listView = listView;
        this.addVerseButton = addVerseButton;
        this.deleteVerseButton = deleteVerseButton;
        this.stateManager = stateManager;

        setupSection();
    }

    private void setupSection() {
        setupCellFactory();
        setupSelectionListener();

        stateManager.addScoreChangeListener(this::refresh);
        refresh();
    }

    private void setupCellFactory() {
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
    }

    private void setupSelectionListener() {
        listView.getSelectionModel().selectedIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            if (isUpdatingList) return;

            ScoreMode mode = stateManager.getCurrentMode();
            if (mode == null || newIdx == null || newIdx.intValue() < 0) return;

            List<Integer> verseNumbers = new ArrayList<>(mode.getVerses().keySet());
            if (newIdx.intValue() < verseNumbers.size()) {
                int selectedVerse = verseNumbers.get(newIdx.intValue());
                stateManager.setSelectedVerseNumber(selectedVerse);
            }
        });
    }

    public void refresh() {
        isUpdatingList = true;
        try {
            ScoreMode mode = stateManager.getCurrentMode();

            if (mode == null || mode.getVerses() == null || mode.getVerses().isEmpty()) {
                listView.getItems().clear();
                updateButtonsState(mode != null, false);
                return;
            }

            List<String> newPreviews = mode.getVerses().values().stream()
                    .map(verse -> verse.getPreviewText(12))
                    .toList();

            ObservableList<String> currentItems = listView.getItems();

            if (currentItems.size() == newPreviews.size()) {
                updateExistingItemsIfNeeded(currentItems, newPreviews);
            } else {
                listView.setItems(FXCollections.observableArrayList(newPreviews));
            }

            updateButtonsState(true, true);
        } finally {
            isUpdatingList = false;
        }

        syncSelectionFromState();
    }

    public void syncSelectionFromState() {
        ScoreMode mode = stateManager.getCurrentMode();
        if (mode == null || mode.getVerses() == null || mode.getVerses().isEmpty()) return;

        int selectedVerseNum = stateManager.getSelectedVerseNumber();
        List<Integer> verseNumbers = new ArrayList<>(mode.getVerses().keySet());

        int targetIndex = verseNumbers.indexOf(selectedVerseNum);

        if (targetIndex < 0 && !verseNumbers.isEmpty()) {
            targetIndex = 0;
            stateManager.setSelectedVerseNumber(verseNumbers.get(0));
        }

        if (targetIndex >= 0 && targetIndex < listView.getItems().size()) {
            isUpdatingList = true;
            try {
                listView.getSelectionModel().select(targetIndex);
            } finally {
                isUpdatingList = false;
            }
        }
    }

    private void updateExistingItemsIfNeeded(ObservableList<String> currentItems, List<String> newPreviews) {
        boolean hasChanges = false;
        for (int i = 0; i < newPreviews.size(); i++) {
            if (!newPreviews.get(i).equals(currentItems.get(i))) {
                currentItems.set(i, newPreviews.get(i));
                hasChanges = true;
            }
        }
        if (hasChanges) {
            listView.refresh();
        }
    }

    private void updateButtonsState(boolean enableAdd, boolean enableDelete) {
        if (addVerseButton != null) addVerseButton.setDisable(!enableAdd);
        if (deleteVerseButton != null) deleteVerseButton.setDisable(!enableDelete);
    }
}
