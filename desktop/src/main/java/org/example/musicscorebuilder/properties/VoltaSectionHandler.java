package org.example.musicscorebuilder.properties;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.VoltaSliceLayout;
import org.example.musicscorebuilder.components.music.Volta;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.function.Consumer;

public class VoltaSectionHandler implements PropertySection {
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();

    private final TitledPane pane;
    private final GridPane mainGrid;

    // Karta: Tekst
    private TextField textField;
    private Spinner<Double> fontSizeSpinner;
    private Spinner<Double> textXOffsetSpinner;

    // Karta: Styl
    private ToggleButton openHookBtn;
    private ToggleButton closedHookBtn;
    private ToggleGroup hookGroup;

    private Spinner<Double> startHookSpinner;
    private Spinner<Double> endHookSpinner;
    private Spinner<Double> yOffsetSpinner;
    private Spinner<Double> thicknessSpinner;

    private ToggleButton solidStyleBtn;
    private ToggleButton dashedStyleBtn;
    private ToggleButton dottedStyleBtn;
    private ToggleGroup styleGroup;

    private GridPane dashedOptionsGrid;
    private Spinner<Double> dashLengthSpinner;
    private Spinner<Double> dashGapSpinner;

    private boolean isUpdating = false;

    public VoltaSectionHandler(TitledPane pane, GridPane grid) {
        this.pane = pane;
        this.mainGrid = grid;
        setupControls();
    }

    private void setupControls() {
        mainGrid.getChildren().clear();
        mainGrid.getStyleClass().add("volta-properties-grid");

        ToggleGroup tabGroup = new ToggleGroup();

        ToggleButton styleTabBtn = new ToggleButton("Styl");
        styleTabBtn.setToggleGroup(tabGroup);
        styleTabBtn.getStyleClass().add("custom-tab-button");
        styleTabBtn.setMaxWidth(Double.MAX_VALUE);

        ToggleButton textTabBtn = new ToggleButton("Tekst");
        textTabBtn.setToggleGroup(tabGroup);
        textTabBtn.getStyleClass().add("custom-tab-button");
        textTabBtn.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(styleTabBtn, Priority.ALWAYS);
        HBox.setHgrow(textTabBtn, Priority.ALWAYS);

        tabGroup.selectedToggleProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) {
                tabGroup.selectToggle(oldV);
            }
        });

        HBox tabHeader = new HBox(styleTabBtn, textTabBtn);
        tabHeader.getStyleClass().add("custom-tab-header");
        tabHeader.setMaxWidth(Double.MAX_VALUE);

        // --- SIATKI ZAWARTOŚCI ---
        GridPane styleGrid = createTabGrid();
        setupStyleTabControls(styleGrid);

        GridPane textGrid = createTabGrid();
        setupTextTabControls(textGrid);

        styleGrid.visibleProperty().bind(styleTabBtn.selectedProperty());
        styleGrid.managedProperty().bind(styleTabBtn.selectedProperty());

        textGrid.visibleProperty().bind(textTabBtn.selectedProperty());
        textGrid.managedProperty().bind(textTabBtn.selectedProperty());

        styleTabBtn.setSelected(true);

        VBox container = new VBox(tabHeader, styleGrid, textGrid);
        container.setMaxWidth(Double.MAX_VALUE);

        mainGrid.add(container, 0, 0, 2, 1);
        GridPane.setHgrow(container, Priority.ALWAYS);
    }

    private GridPane createTabGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setStyle("-fx-padding: 10 0 0 0;");

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);
        return grid;
    }

    // ==========================================
    // SEKCJA: KARTA TEKST
    // ==========================================
    private void setupTextTabControls(GridPane textGrid) {
        int row = 0;

        // 1. TEKST VOLTY
        Label textLabel = createSectionLabel("Tekst");
        textGrid.add(textLabel, 0, row++, 2, 1);

        textField = new TextField();
        textField.getStyleClass().add("metadata-input");
        textField.setMaxWidth(Double.MAX_VALUE);

        textGrid.add(textField, 0, row++, 2, 1);

        textField.textProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setText(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        // 2. ROZMIAR CZCIONKI
        Label fontSizeLabel = createFieldLabel("Rozmiar czcionki");
        textGrid.add(fontSizeLabel, 0, row++, 2, 1);

        fontSizeSpinner = createSpinner(0.5, 20.0, 2.0, 0.01);
        fontSizeSpinner.setMaxWidth(Double.MAX_VALUE);

        HBox fontSizeBox = createControlWithReset(fontSizeSpinner, v -> v.setFontSize(null));
        textGrid.add(fontSizeBox, 0, row++, 2, 1);

        fontSizeSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setFontSize(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        // 3. ODSTĘP POZIOMY (X)
        Label textXOffsetLabel = createFieldLabel("Odstęp poziomy (X)");
        textGrid.add(textXOffsetLabel, 0, row++, 2, 1);

        textXOffsetSpinner = createSpinner(-50.0, 50.0, 0.0, 0.01);
        textXOffsetSpinner.setMaxWidth(Double.MAX_VALUE);

        HBox textXOffsetBox = createControlWithReset(textXOffsetSpinner, v -> v.setTextXOffset(null));
        textGrid.add(textXOffsetBox, 0, row++, 2, 1);

        textXOffsetSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setTextXOffset(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });
    }

    // ==========================================
    // SEKCJA: KARTA STYL
    // ==========================================
    private void setupStyleTabControls(GridPane styleGrid) {
        int row = 0;

        HBox hookHeader = createHeaderRow("Hak końcowy", v -> v.setClosedEnd(null));
        styleGrid.add(hookHeader, 0, row++, 2, 1);

        hookGroup = new ToggleGroup();
        openHookBtn = createIconButton(createHookIcon(false), hookGroup);
        closedHookBtn = createIconButton(createHookIcon(true), hookGroup);

        GridPane hookGrid = createEqualGrid(openHookBtn, closedHookBtn);
        styleGrid.add(hookGrid, 0, row++, 2, 1);

        hookGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                hookGroup.selectToggle(oldT);
                return;
            }
            if (isUpdating) return;
            Volta volta = getSelectedVolta();
            if (volta != null) {
                volta.setClosedEnd(newT == closedHookBtn);
                stateManager.notifyScoreChanged();
            }
        });

        GridPane hookHeightsGrid = createHookHeightsUI();
        styleGrid.add(hookHeightsGrid, 0, row++, 2, 1);

        Label yOffsetLabel = createSectionLabel("Odstęp pionowy (Y)");
        styleGrid.add(yOffsetLabel, 0, row++, 2, 1);

        yOffsetSpinner = createSpinner(-10.0, 20.0, 2.5, 0.01);
        yOffsetSpinner.setMaxWidth(Double.MAX_VALUE);

        HBox yOffsetBox = createControlWithReset(yOffsetSpinner, v -> v.setYOffset(null));
        styleGrid.add(yOffsetBox, 0, row++, 2, 1);

        yOffsetSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setYOffset(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        Label thicknessLabel = createSectionLabel("Grubość");
        styleGrid.add(thicknessLabel, 0, row++, 2, 1);

        thicknessSpinner = createSpinner(0.01, 5.0, 0.11, 0.01);
        thicknessSpinner.setMaxWidth(Double.MAX_VALUE);

        HBox thicknessBox = createControlWithReset(thicknessSpinner, v -> v.setLineWidth(null));
        styleGrid.add(thicknessBox, 0, row++, 2, 1);

        thicknessSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setLineWidth(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        HBox styleHeader = createHeaderRow("Styl", v -> {
            v.setLineStyle(null);
            v.setDashLength(null);
            v.setDashGap(null);
        });
        styleGrid.add(styleHeader, 0, row++, 2, 1);

        styleGroup = new ToggleGroup();
        solidStyleBtn = createIconButton(createLineStyleIcon("solid"), styleGroup);
        dashedStyleBtn = createIconButton(createLineStyleIcon("dashed"), styleGroup);
        dottedStyleBtn = createIconButton(createLineStyleIcon("dotted"), styleGroup);

        GridPane lineStyleGrid = createEqualGrid(solidStyleBtn, dashedStyleBtn, dottedStyleBtn);
        styleGrid.add(lineStyleGrid, 0, row++, 2, 1);

        setupDashedOptionsUI();
        styleGrid.add(dashedOptionsGrid, 0, row++, 2, 1);

        styleGroup.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) {
                styleGroup.selectToggle(oldT);
                return;
            }
            if (!isUpdating) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    if (newT == dashedStyleBtn) {
                        volta.setLineStyle(Volta.LineStyle.DASHED);
                    } else if (newT == dottedStyleBtn) {
                        volta.setLineStyle(Volta.LineStyle.DOTTED);
                    } else {
                        volta.setLineStyle(Volta.LineStyle.SOLID);
                    }
                    updateDashedOptionsVisibility();
                    stateManager.notifyScoreChanged();
                }
            }
        });
    }

    private GridPane createHookHeightsUI() {
        GridPane hookHeightsGrid = new GridPane();
        hookHeightsGrid.setHgap(8);
        hookHeightsGrid.setVgap(4);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        hookHeightsGrid.getColumnConstraints().addAll(col1, col2);

        Label startHookLabel = createCenteredFieldLabel("Wysokość haka\npoczątkowego");
        Label endHookLabel = createCenteredFieldLabel("Wysokość haka\nkońcowego");

        startHookSpinner = createSpinner(0.0, 10.0, 1.9, 0.01);
        startHookSpinner.setMaxWidth(Double.MAX_VALUE);

        endHookSpinner = createSpinner(0.0, 10.0, 1.9, 0.01);
        endHookSpinner.setMaxWidth(Double.MAX_VALUE);

        HBox startHookBox = createControlWithReset(startHookSpinner, v -> v.setStartHookHeight(null));
        HBox endHookBox = createControlWithReset(endHookSpinner, v -> v.setEndHookHeight(null));

        hookHeightsGrid.add(startHookLabel, 0, 0);
        hookHeightsGrid.add(endHookLabel, 1, 0);
        hookHeightsGrid.add(startHookBox, 0, 1);
        hookHeightsGrid.add(endHookBox, 1, 1);

        startHookSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setStartHookHeight(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        endHookSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setEndHookHeight(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        return hookHeightsGrid;
    }

    private void setupDashedOptionsUI() {
        dashedOptionsGrid = new GridPane();
        dashedOptionsGrid.setHgap(8);
        dashedOptionsGrid.setVgap(4);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        dashedOptionsGrid.getColumnConstraints().addAll(col1, col2);

        Label dashLabel = createFieldLabel("Kreska");
        Label gapLabel = createFieldLabel("Przerwa");

        dashLengthSpinner = createSpinner(0.5, 20.0, 5.0, 0.01);
        dashLengthSpinner.setMaxWidth(Double.MAX_VALUE);

        dashGapSpinner = createSpinner(0.5, 20.0, 5.0, 0.01);
        dashGapSpinner.setMaxWidth(Double.MAX_VALUE);

        HBox dashBox = createControlWithReset(dashLengthSpinner, v -> v.setDashLength(null));
        HBox gapBox = createControlWithReset(dashGapSpinner, v -> v.setDashGap(null));

        dashedOptionsGrid.add(dashLabel, 0, 0);
        dashedOptionsGrid.add(gapLabel, 1, 0);
        dashedOptionsGrid.add(dashBox, 0, 1);
        dashedOptionsGrid.add(gapBox, 1, 1);

        dashLengthSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setDashLength(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        dashGapSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Volta volta = getSelectedVolta();
                if (volta != null) {
                    volta.setDashGap(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });
    }

    private void updateDashedOptionsVisibility() {
        boolean isDashed = styleGroup.getSelectedToggle() == dashedStyleBtn;
        dashedOptionsGrid.setVisible(isDashed);
        dashedOptionsGrid.setManaged(isDashed);
    }

    @Override
    public void refresh() {
        VoltaSliceLayout voltaSlice = getSelectedVoltaSlice();
        boolean visible = (voltaSlice != null);

        pane.setVisible(visible);
        pane.setManaged(visible);

        if (visible) {
            Volta volta = voltaSlice.getVolta();
            if (volta == null) return;

            isUpdating = true;

            textField.setText(volta.getText() != null ? volta.getText() : "");
            fontSizeSpinner.getValueFactory().setValue(voltaSlice.getFontSize());

            Double textX = voltaSlice.getTextXOffset();
            textXOffsetSpinner.getValueFactory().setValue(textX != null ? textX : 0.0);

            if (volta.isClosedEnd()) {
                closedHookBtn.setSelected(true);
            } else {
                openHookBtn.setSelected(true);
            }

            startHookSpinner.getValueFactory().setValue(voltaSlice.getStartHookHeight());
            endHookSpinner.getValueFactory().setValue(voltaSlice.getEndHookHeight());
            yOffsetSpinner.getValueFactory().setValue(voltaSlice.getYOffset());
            thicknessSpinner.getValueFactory().setValue(voltaSlice.getLineWidth());

            dashLengthSpinner.getValueFactory().setValue(voltaSlice.getDashLength());
            dashGapSpinner.getValueFactory().setValue(voltaSlice.getDashGap());

            Volta.LineStyle style = voltaSlice.getLineStyle();
            if (style == Volta.LineStyle.DASHED) {
                dashedStyleBtn.setSelected(true);
            } else if (style == Volta.LineStyle.DOTTED) {
                dottedStyleBtn.setSelected(true);
            } else {
                solidStyleBtn.setSelected(true);
            }

            updateDashedOptionsVisibility();

            isUpdating = false;
        }
    }

    private void resetVoltaProperty(Consumer<Volta> resetAction) {
        Volta volta = getSelectedVolta();
        if (volta != null) {
            resetAction.accept(volta);
            stateManager.notifyScoreChanged();
            refresh();
        }
    }

    private HBox createControlWithReset(Control control, Consumer<Volta> resetAction) {
        Button resetBtn = createResetButton(resetAction);
        HBox box = new HBox(4, control, resetBtn);
        HBox.setHgrow(control, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button createResetButton(Consumer<Volta> resetAction) {
        Button resetBtn = new Button();
        resetBtn.getStyleClass().add("property-reset-icon-btn");
        resetBtn.setOnAction(e -> {
            if (!isUpdating) {
                resetVoltaProperty(resetAction);
            }
        });
        return resetBtn;
    }

    private VoltaSliceLayout getSelectedVoltaSlice() {
        if (stateManager.getSelectedItem() instanceof VoltaSliceLayout slice && slice.isSelected()) {
            return slice;
        }
        return null;
    }

    private Volta getSelectedVolta() {
        VoltaSliceLayout slice = getSelectedVoltaSlice();
        return slice != null ? slice.getVolta() : null;
    }

    private GridPane createEqualGrid(ToggleButton... buttons) {
        GridPane container = new GridPane();
        container.setHgap(4);
        double percent = 100.0 / buttons.length;

        for (int i = 0; i < buttons.length; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(percent);
            cc.setHgrow(Priority.ALWAYS);
            container.getColumnConstraints().add(cc);

            ToggleButton btn = buttons[i];
            btn.setMaxWidth(Double.MAX_VALUE);
            container.add(btn, i, 0);
        }
        return container;
    }

    private ToggleButton createIconButton(SVGPath icon, ToggleGroup group) {
        ToggleButton btn = new ToggleButton();
        btn.setGraphic(icon);
        btn.setToggleGroup(group);
        btn.getStyleClass().add("segment-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER);
        return btn;
    }

    private SVGPath createHookIcon(boolean closed) {
        SVGPath path = new SVGPath();
        if (closed) {
            path.setContent("M 6,13 L 6,4 L 30,4 L 30,13");
        } else {
            path.setContent("M 6,13 L 6,4 L 30,4");
        }
        path.setStroke(Color.web("#333333"));
        path.setStrokeWidth(1.8);
        path.setFill(null);
        return path;
    }

    private SVGPath createLineStyleIcon(String style) {
        SVGPath path = new SVGPath();
        path.setContent("M 4,8 L 28,8");
        path.setStroke(Color.web("#333333"));
        path.setStrokeWidth(2.0);
        path.setFill(null);

        if ("dashed".equals(style)) {
            path.getStrokeDashArray().addAll(5.0, 3.0);
        } else if ("dotted".equals(style)) {
            path.getStrokeDashArray().addAll(2.0, 2.0);
            path.setStrokeLineCap(StrokeLineCap.BUTT);
        }
        return path;
    }

    private HBox createHeaderRow(String title, Consumer<Volta> resetAction) {
        Label label = new Label(title);
        label.getStyleClass().add("section-subtitle-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox box = new HBox(label, spacer);
        if (resetAction != null) {
            Button resetBtn = createResetButton(resetAction);
            box.getChildren().add(resetBtn);
        }
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Label createFieldLabel(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("field-label");
        label.setWrapText(true);
        return label;
    }

    private Label createCenteredFieldLabel(String title) {
        Label label = createFieldLabel(title);
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private Label createSectionLabel(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("section-subtitle-label");
        return label;
    }

    private Spinner<Double> createSpinner(double min, double max, double initial, double step) {
        Spinner<Double> spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step));
        spinner.setEditable(true);
        spinner.getStyleClass().add("properties-spinner");
        return spinner;
    }
}