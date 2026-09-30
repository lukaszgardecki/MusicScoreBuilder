package org.example.musicscorebuilder.properties;

import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.managers.ScoreStateManager;

public class GeneralSectionHandler implements PropertySection {
    private final TitledPane pane;
    private final Spinner<Double> scaleSpinner;
    private final Spinner<Double> systemSpacingSpinner;
    private boolean isUpdating = false;

    public GeneralSectionHandler(TitledPane pane, GridPane grid) {
        this.pane = pane;
        scaleSpinner = createScaleSpinner(grid);
        systemSpacingSpinner = createSystemSpacingSpinner(grid);
        refresh();
    }

    @Override
    public void refresh() {
        ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
        boolean hasStyle = mode != null && mode.getStyle() != null;

        isUpdating = true;
        if (hasStyle) {
            scaleSpinner.setDisable(false);
            systemSpacingSpinner.setDisable(false);

            scaleSpinner.getValueFactory().setValue(mode.getStyle().getStaffSpacingScale());
            systemSpacingSpinner.getValueFactory().setValue(mode.getStyle().getSystemSpacing());
        } else {
            scaleSpinner.setDisable(true);
            systemSpacingSpinner.setDisable(true);

            scaleSpinner.getValueFactory().setValue(null);
            systemSpacingSpinner.getValueFactory().setValue(null);

            scaleSpinner.getEditor().clear();
            systemSpacingSpinner.getEditor().clear();
        }
        isUpdating = false;
    }

    private Spinner<Double> createScaleSpinner(GridPane grid) {
        Label scaleLabel = new Label("Skala:");
        scaleLabel.getStyleClass().add("properties-label");
        Spinner<Double> scaleSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.10, 2.00, 1.00, 0.01));
        scaleSpinner.setEditable(true);
        scaleSpinner.getStyleClass().add("properties-spinner");

        grid.add(scaleLabel, 0, 0);
        grid.add(scaleSpinner, 1, 0);

        scaleSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onScaleChanged(newV);
        });
        return scaleSpinner;
    }

    private Spinner<Double> createSystemSpacingSpinner(GridPane grid) {
        Label systemSpacingLabel = new Label("Odstęp między systemami:");
        systemSpacingLabel.getStyleClass().add("properties-label");
        systemSpacingLabel.setWrapText(true);
        Spinner<Double> systemSpacingSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 20.0, 10.0, 0.01));
        systemSpacingSpinner.setEditable(true);
        systemSpacingSpinner.getStyleClass().add("properties-spinner");

        grid.add(systemSpacingLabel, 0, 1);
        grid.add(systemSpacingSpinner, 1, 1);

        systemSpacingSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onSystemSpacingChanged(newV);
        });

        return systemSpacingSpinner;
    }

    private void onScaleChanged(double newScale) {
        ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
        if (mode != null) {
            mode.getStyle().setStaffSpacingScale(newScale);
            mode.getMeasures().forEach(m -> m.setDirty(true));
            ScoreStateManager.getInstance().notifyScoreChanged();
        }
    }

    private void onSystemSpacingChanged(double newSpacing) {
        ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
        if (mode != null) {
            mode.getStyle().setSystemSpacing(newSpacing);
            mode.getMeasures().forEach(m -> m.setDirty(true));
            ScoreStateManager.getInstance().notifyScoreChanged();
        }
    }
}