package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public abstract class AbstractPaletteSectionController<T> {
    protected final ScoreService scoreService = ScoreService.getInstance();
    protected final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    protected final GridPane gridPane;
    private Button selectedButton = null;

    public AbstractPaletteSectionController(GridPane gridPane) {
        this.gridPane = gridPane;
    }

    public void build() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();

        int columns = getColumnsCount();
        for (int i = 0; i < columns; i++) {
            javafx.scene.layout.ColumnConstraints colConstraints = new javafx.scene.layout.ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / columns);
            colConstraints.setHgrow(javafx.scene.layout.Priority.ALWAYS);
            gridPane.getColumnConstraints().add(colConstraints);
        }

        List<T> items = getItems();
        int index = 0;
        for (T item : items) {
            Button btn = createPaletteButton(item);

            int col = index % columns;
            int row = index / columns;
            gridPane.add(btn, col, row);
            index++;
        }
    }

    private Button createPaletteButton(T item) {
        Button btn = new Button();
        btn.getStyleClass().add("palette-btn");

        Node graphic = createButtonGraphic(item);
        btn.setGraphic(graphic);
        btn.setOnAction(event -> handleItemClick(item, btn));
        return btn;
    }

    private void handleItemClick(T item, Button btn) {
        boolean appliedToSelected = applyToSelectedElement(item);

        if (appliedToSelected) {
            stateManager.clearSelection();
            stateManager.notifyScoreChanged();
            selectButton(null);
        } else {
            selectButton(btn);
        }
    }

    protected void selectButton(Button btn) {
        for (Node node : gridPane.getChildren()) {
            if (node instanceof Button b) {
                b.getStyleClass().remove("selected");
            }
        }
        selectedButton = btn;
        if (selectedButton != null) {
            selectedButton.getStyleClass().add("selected");
        }
    }

    protected abstract int getColumnsCount();
    protected abstract List<T> getItems();
    protected abstract Node createButtonGraphic(T item);
    protected abstract boolean applyToSelectedElement(T item);
}