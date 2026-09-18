package org.example.musicscorebuilder.properties;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.example.musicscorebuilder.components.layout.TempoLayout;
import org.example.musicscorebuilder.components.music.Tempo;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.function.Consumer;

public class TempoSectionHandler implements PropertySection {
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();

    private final TitledPane pane;
    private final GridPane mainGrid;

    private TextField textField;
    private Spinner<Double> fontSizeSpinner;

    private ToggleButton boldBtn;
    private ToggleButton italicBtn;
    private Spinner<Double> xOffsetSpinner;
    private Spinner<Double> yOffsetSpinner;

    private boolean isUpdating = false;

    public TempoSectionHandler(TitledPane pane, GridPane grid) {
        this.pane = pane;
        this.mainGrid = grid;

        this.pane.setMinWidth(230);
        this.mainGrid.setMinWidth(210);

        setupControls();
    }

    private void setupControls() {
        mainGrid.getChildren().clear();
        mainGrid.getStyleClass().add("tempo-properties-grid");
        mainGrid.setHgap(8);
        mainGrid.setVgap(8);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);

        mainGrid.getColumnConstraints().setAll(col1, col2);

        int row = 0;

        // 1. TEKST TEMPA
        Label textLabel = createSectionLabel("Tekst");
        mainGrid.add(textLabel, 0, row++, 2, 1);

        textField = new TextField();
        textField.getStyleClass().add("metadata-input");
        textField.setMaxWidth(Double.MAX_VALUE);
        mainGrid.add(textField, 0, row++, 2, 1);

        textField.textProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating) {
                Tempo tempo = getSelectedTempo();
                if (tempo != null) {
                    tempo.setText(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        // 2. CZCIONKA I FORMATOWANIE
        Label fontLabel = createFieldLabel("Czcionka i format");
        mainGrid.add(fontLabel, 0, row++, 2, 1);

        fontSizeSpinner = createSpinner(1.0, 50.0, 12.0, 0.1);
        HBox.setHgrow(fontSizeSpinner, Priority.ALWAYS);

        Button fontResetBtn = createResetButton(t -> {
            t.setFontSize(null);
            t.setBold(null);
            t.setItalic(null);
        });

        boldBtn = createTextFormatButton("B", true);
        italicBtn = createTextFormatButton("I", false);
        boldBtn.setMinWidth(32);
        italicBtn.setMinWidth(32);

        HBox fontRow = new HBox(4, fontSizeSpinner, fontResetBtn, boldBtn, italicBtn);
        fontRow.setAlignment(Pos.CENTER_LEFT);
        fontRow.setMaxWidth(Double.MAX_VALUE);
        mainGrid.add(fontRow, 0, row++, 2, 1);

        fontSizeSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Tempo tempo = getSelectedTempo();
                if (tempo != null) {
                    tempo.setFontSize(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        boldBtn.selectedProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating) {
                Tempo tempo = getSelectedTempo();
                if (tempo != null) {
                    tempo.setBold(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        italicBtn.selectedProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating) {
                Tempo tempo = getSelectedTempo();
                if (tempo != null) {
                    tempo.setItalic(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        // 3. ODSTĘPY X I Y
        Label xLabel = createFieldLabel("Odstęp X");
        Label yLabel = createFieldLabel("Odstęp Y");

        mainGrid.add(xLabel, 0, row);
        mainGrid.add(yLabel, 1, row++);

        xOffsetSpinner = createSpinner(-50.0, 50.0, 0.0, 0.01);
        yOffsetSpinner = createSpinner(-50.0, 50.0, 0.0, 0.01);

        HBox xOffsetBox = createControlWithReset(xOffsetSpinner, t -> t.setXOffset(null));
        HBox yOffsetBox = createControlWithReset(yOffsetSpinner, t -> t.setYOffset(null));

        mainGrid.add(xOffsetBox, 0, row);
        mainGrid.add(yOffsetBox, 1, row++);

        xOffsetSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Tempo tempo = getSelectedTempo();
                if (tempo != null) {
                    tempo.setXOffset(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });

        yOffsetSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (!isUpdating && newV != null) {
                Tempo tempo = getSelectedTempo();
                if (tempo != null) {
                    tempo.setYOffset(newV);
                    stateManager.notifyScoreChanged();
                }
            }
        });
    }

    @Override
    public void refresh() {
        TempoLayout tempoLayout = getSelectedTempoLayout();
        boolean visible = (tempoLayout != null);

        pane.setVisible(visible);
        pane.setManaged(visible);

        if (visible) {
            Tempo tempo = tempoLayout.getTempo();
            if (tempo == null) return;

            isUpdating = true;

            textField.setText(tempoLayout.getText());
            fontSizeSpinner.getValueFactory().setValue(tempoLayout.getFontSize());

            boldBtn.setSelected(tempoLayout.isBold());
            italicBtn.setSelected(tempoLayout.isItalic());

            xOffsetSpinner.getValueFactory().setValue(tempoLayout.getXOffset());
            yOffsetSpinner.getValueFactory().setValue(tempoLayout.getYOffset());

            isUpdating = false;
        }
    }

    private void resetTempoProperty(Consumer<Tempo> resetAction) {
        Tempo tempo = getSelectedTempo();
        if (tempo != null) {
            resetAction.accept(tempo);
            stateManager.notifyScoreChanged();
            refresh();
        }
    }

    private HBox createControlWithReset(Control control, Consumer<Tempo> resetAction) {
        Button resetBtn = createResetButton(resetAction);
        HBox.setHgrow(control, Priority.ALWAYS);

        HBox box = new HBox(4, control, resetBtn);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    private Button createResetButton(Consumer<Tempo> resetAction) {
        Button resetBtn = new Button();
        resetBtn.getStyleClass().add("property-reset-icon-btn");
        resetBtn.setMinWidth(Region.USE_PREF_SIZE);
        resetBtn.setOnAction(e -> {
            if (!isUpdating) {
                resetTempoProperty(resetAction);
            }
        });
        return resetBtn;
    }

    private TempoLayout getSelectedTempoLayout() {
        if (stateManager.getSelectedItem() instanceof TempoLayout layout && layout.isSelected()) {
            return layout;
        }
        return null;
    }

    private Tempo getSelectedTempo() {
        TempoLayout layout = getSelectedTempoLayout();
        return layout != null ? layout.getTempo() : null;
    }

    private ToggleButton createTextFormatButton(String text, boolean bold) {
        ToggleButton btn = new ToggleButton(text);
        btn.getStyleClass().add("segment-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER);

        if (bold) {
            btn.setFont(Font.font("System", FontWeight.BOLD, 13));
        } else {
            btn.setFont(Font.font("System", FontPosture.ITALIC, 13));
        }

        return btn;
    }

    private Label createFieldLabel(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("field-label");
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    private Label createSectionLabel(String title) {
        Label label = new Label(title);
        label.getStyleClass().add("section-subtitle-label");
        label.setMinWidth(Region.USE_PREF_SIZE);
        return label;
    }

    private Spinner<Double> createSpinner(double min, double max, double initial, double step) {
        Spinner<Double> spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step));
        spinner.setEditable(true);
        spinner.getStyleClass().add("properties-spinner");
        spinner.setMaxWidth(Double.MAX_VALUE);
        spinner.setMinWidth(50);
        return spinner;
    }
}