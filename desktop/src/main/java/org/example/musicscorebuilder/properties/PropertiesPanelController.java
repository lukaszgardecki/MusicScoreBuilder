package org.example.musicscorebuilder.properties;

import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.ArrayList;
import java.util.List;

public class PropertiesPanelController {
    @FXML private TitledPane generalPane;
    @FXML private GridPane generalProperties;

    @FXML private TitledPane framePane;
    @FXML private GridPane frameProperties;

    private final List<PropertySection> sections = new ArrayList<>();

    @FXML
    public void initialize() {
        sections.add(new GeneralSectionHandler(generalPane, generalProperties));
        sections.add(new FrameSectionHandler(framePane, frameProperties));

        ScoreStateManager stateManager = ScoreStateManager.getInstance();
        stateManager.addScoreChangeListener(this::refreshAll);
        stateManager.addSelectionChangeListener(selected -> refreshAll());

        refreshAll();
    }

    private void refreshAll() {
        for (PropertySection section : sections) {
            section.refresh();
        }
    }
}