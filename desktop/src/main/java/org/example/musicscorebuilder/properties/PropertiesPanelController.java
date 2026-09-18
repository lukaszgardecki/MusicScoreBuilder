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

    @FXML private TitledPane voltaPane;
    @FXML private GridPane voltaProperties;

    @FXML private TitledPane tempoPane;
    @FXML private GridPane tempoProperties;

    private final List<PropertySection> sections = new ArrayList<>();

    @FXML
    public void initialize() {
        sections.add(new GeneralSectionHandler(generalPane, generalProperties));
        sections.add(new FrameSectionHandler(framePane, frameProperties));
        sections.add(new VoltaSectionHandler(voltaPane, voltaProperties));
        sections.add(new TempoSectionHandler(tempoPane, tempoProperties));

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