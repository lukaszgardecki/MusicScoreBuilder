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
    private boolean isUpdating = false;

    public GeneralSectionHandler(TitledPane pane, GridPane grid) {
        this.pane = pane;

        Label scaleLabel = new Label("Skala:");
        scaleLabel.getStyleClass().add("properties-label");
        scaleSpinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.10, 2.00, 1.00, 0.01));
        scaleSpinner.setEditable(true);
        scaleSpinner.getStyleClass().add("properties-spinner");

        grid.add(scaleLabel, 0, 0);
        grid.add(scaleSpinner, 1, 0);

        scaleSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onScaleChanged(newV);
        });
    }

    @Override
    public void refresh() {
        ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
        if (mode != null && mode.getStyle() != null) {
            isUpdating = true;
            scaleSpinner.getValueFactory().setValue(mode.getStyle().getStaffSpacingScale());
            isUpdating = false;
        }
    }

    private void onScaleChanged(double newScale) {
        ScoreMode mode = ScoreStateManager.getInstance().getCurrentMode();
        if (mode != null) {
            mode.getStyle().setStaffSpacingScale(newScale);
            mode.getMeasures().forEach(m -> m.setDirty(true));
            ScoreStateManager.getInstance().notifyScoreChanged();
        }
    }
}