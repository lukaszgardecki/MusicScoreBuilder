package org.example.musicscorebuilder.properties;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.text.DecimalFormat;

public class GeneralPropertiesController {
    @FXML private GridPane generalProperties;

    private Spinner<Double> scaleSpinner;
    private boolean isUpdating = false;

    @FXML
    public void initialize() {
        Label scaleLabel = new Label("Skala:");
        scaleLabel.getStyleClass().add("properties-label");

        SpinnerValueFactory.DoubleSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.DoubleSpinnerValueFactory(0.10, 2.00, 1.00, 0.01);

        valueFactory.setConverter(new StringConverter<>() {
            private final DecimalFormat df = new DecimalFormat("0.00");

            @Override
            public String toString(Double value) {
                if (value == null) return "";
                return df.format(value);
            }

            @Override
            public Double fromString(String string) {
                try {
                    if (string == null || string.isBlank()) {
                        return valueFactory.getValue();
                    }
                    String normalized = string.replace(',', '.').trim();
                    return Double.parseDouble(normalized);
                } catch (NumberFormatException e) {
                    return valueFactory.getValue();
                }
            }
        });

        scaleSpinner = new Spinner<>(valueFactory);
        scaleSpinner.setEditable(true);
        scaleSpinner.getStyleClass().add("properties-spinner");

        scaleSpinner.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                scaleSpinner.increment(0);
            }
        });

        scaleSpinner.getEditor().setOnAction(event -> scaleSpinner.increment(0));

        generalProperties.add(scaleLabel, 0, 0);
        generalProperties.add(scaleSpinner, 1, 0);
        scaleSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !isUpdating) {
                onScaleChanged(newValue);
            }
        });

        ScoreStateManager.getInstance().addScoreChangeListener(this::updateFromCurrentMode);
        updateFromCurrentMode();
    }

    public void updateFromCurrentMode() {
        ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
        if (mode != null && mode.getStyle() != null) {
            isUpdating = true;
            scaleSpinner.getValueFactory().setValue(mode.getStyle().getStaffSpacingScale());
            isUpdating = false;
        }
    }

    private void onScaleChanged(double newScale) {
        ScoreStateManager stateManager = ScoreStateManager.getInstance();
        ScoreMode mode = stateManager.getCurrentMode();
        if (mode != null) {
            mode.getStyle().setStaffSpacingScale(newScale);
            mode.getMeasures().forEach(m -> m.setDirty(true));
            stateManager.notifyScoreChanged();
        }
    }
}