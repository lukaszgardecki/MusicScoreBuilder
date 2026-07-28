package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.layout.ElementLayout;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Clef;
import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.Staff;
import org.example.musicscorebuilder.components.views.StaffView;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public abstract class AbstractPaletteSectionController<T> {
    protected final double defaultCanvasWidth = 45;
    protected final double defaultCanvasHeight = 35;
    protected final ScoreService scoreService = ScoreService.getInstance();
    protected final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    protected final GridPane gridPane;
    private Button selectedButton = null;

    private final StaffView staffView = new StaffView();

    ScoreStyle mockStyle = new ScoreStyle() {
        @Override public double getStaffLineSpacing() { return 6.0; }
        @Override public double getStaffLineWidth() { return 1.0; }
        @Override public double getStaffSpacing() { return 0.0; }
        @Override public double getBarlineLightWidth() { return 2.0; }
        @Override public double getBarlineHeavyWidth() { return 4.0; }
        @Override public double getBarlineGap() { return 3.0; }
        @Override public double getBarlineDotSpace() { return 3.0; }
        @Override public double getBarlineDotRadius() { return 1.0; }
    };
    Staff mockStaff = new Staff(0, new Clef(ClefType.G));
    Measure mockMeasure = new Measure(List.of(mockStaff));
    MeasureLayout mockMeasureLayout = new MeasureLayout(mockMeasure, 0, mockStyle) {
        @Override public double getWidth() { return 46.0; }
    };
    StaffLayout staffLayout = new StaffLayout(mockStaff, mockMeasureLayout, mockStyle);

    protected abstract int getColumnsCount();
    protected abstract List<T> getItems();
    protected abstract Node createButtonGraphic(T item);
    protected abstract boolean applyToSelectedElement(T item);

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

    protected Canvas createBaseCanvas(boolean extraHeight) {
        double canvasHeight = extraHeight ? defaultCanvasHeight + 10 : defaultCanvasHeight;
        Canvas canvas = new Canvas(defaultCanvasWidth, canvasHeight);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double sMeasureY = Math.round((canvasHeight / 2.0) - (staffLayout.getHeight() / 2.0));
        double sp = 1.0;

        drawStaff(gc, staffLayout, 0, sMeasureY, sp);
        return canvas;
    }

    protected double calculateMeasureX(ElementLayout mockLayout) {
        return Math.round((defaultCanvasWidth / 2.0) - (mockLayout.getWidth() / 2.0));
    }

    protected double calculateMeasureY(boolean extraHeight) {
        double canvasHeight = extraHeight ? defaultCanvasHeight + 10 : defaultCanvasHeight;
        return Math.round((canvasHeight / 2.0) - (staffLayout.getHeight() / 2.0));
    }

    protected void drawStaff(GraphicsContext gc, StaffLayout staff, double x, double y, double sp) {
        gc.setGlobalAlpha(0.2);
        staffView.drawForPalette(gc, staff, x, y, sp);
    }
}