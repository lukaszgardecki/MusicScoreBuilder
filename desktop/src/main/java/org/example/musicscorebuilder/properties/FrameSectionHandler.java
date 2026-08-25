package org.example.musicscorebuilder.properties;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.example.musicscorebuilder.components.layout.FrameLayout;
import org.example.musicscorebuilder.components.music.Frame;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.data.StorageService;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class FrameSectionHandler implements PropertySection {
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final StorageService storageService = StorageService.getInstance();

    private final TitledPane pane;
    private final GridPane grid;
    private Spinner<Double> widthSpinner;
    private Spinner<Double> heightSpinner;
    private Spinner<Double> marginTopSpinner;
    private Spinner<Double> marginBottomSpinner;

    private final List<FrameMetaRow> metaRows = new ArrayList<>();
    private boolean isUpdating = false;
    int startRow = 0;

    public FrameSectionHandler(TitledPane pane, GridPane grid) {
        this.pane = pane;
        this.grid = grid;
        setupFrameDimensionControls();
        addSpacer();
        setupFrameDataControls();
    }

    private void setupFrameDataControls() {
        metaRows.add(new FrameMetaRow("Tytuł", Frame::getTitle, Frame::setTitle, Score::getTitle, grid, startRow++));
        metaRows.add(new FrameMetaRow("Podtytuł", Frame::getSubtitle, Frame::setSubtitle, Score::getSubtitle, grid, startRow++));
        metaRows.add(new FrameMetaRow("Nowy numer", Frame::getNumberNew, Frame::setNumberNew, Score::getNumberNew, grid, startRow++));
        metaRows.add(new FrameMetaRow("Stary numer", Frame::getNumberOld, Frame::setNumberOld, Score::getNumberOld, grid, startRow++));
        metaRows.add(new FrameMetaRow("Kompozytor", Frame::getComposer, Frame::setComposer, Score::getComposer, grid, startRow++));
    }

    private void setupFrameDimensionControls() {
        // --- Szerokość ---
        widthSpinner = createSpinner(1.0, 2000.0, getDefaultWidth(), 0.1);
        Button widthResetBtn = createResetButton("Przywróć domyślną szerokość strony");
        widthResetBtn.setOnAction(e -> resetDimension(this::getDefaultWidth, widthSpinner, this::onWidthChanged));
        widthSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onWidthChanged(newV);
        });
        HBox widthBox = createLabeledRow("Szerokość:", widthSpinner, widthResetBtn);
        grid.add(widthBox, 0, startRow++, 2, 1);

        // --- Wysokość ---
        heightSpinner = createSpinner(1.0, 2000.0, getDefaultHeight(), 0.1);
        Button heightResetBtn = createResetButton("Przywróć domyślną wysokość ze stylu");
        heightResetBtn.setOnAction(e -> resetDimension(this::getDefaultHeight, heightSpinner, this::onHeightChanged));
        heightSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onHeightChanged(newV);
        });
        HBox heightBox = createLabeledRow("Wysokość:", heightSpinner, heightResetBtn);
        grid.add(heightBox, 0, startRow++, 2, 1);

        // --- Margines Górny ---
        marginTopSpinner = createSpinner(0.0, 1000.0, getDefaultMarginTop(), 0.1);
        Button marginTopResetBtn = createResetButton("Przywróć domyślny margines górny ze stylu");
        marginTopResetBtn.setOnAction(e -> resetDimension(this::getDefaultMarginTop, marginTopSpinner, this::onMarginTopChanged));
        marginTopSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onMarginTopChanged(newV);
        });
        HBox marginTopBox = createLabeledRow("Margines góra:", marginTopSpinner, marginTopResetBtn);
        grid.add(marginTopBox, 0, startRow++, 2, 1);

        // --- Margines Dolny ---
        marginBottomSpinner = createSpinner(0.0, 1000.0, getDefaultMarginBottom(), 0.1);
        Button marginBottomResetBtn = createResetButton("Przywróć domyślny margines dolny ze stylu");
        marginBottomResetBtn.setOnAction(e -> resetDimension(this::getDefaultMarginBottom, marginBottomSpinner, this::onMarginBottomChanged));
        marginBottomSpinner.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && !isUpdating) onMarginBottomChanged(newV);
        });
        HBox marginBottomBox = createLabeledRow("Margines dół:", marginBottomSpinner, marginBottomResetBtn);
        grid.add(marginBottomBox, 0, startRow++, 2, 1);
    }

    private void addSpacer() {
        Region spacer = new Region();
        spacer.setMinHeight(12.0);
        grid.add(spacer, 0, startRow++, 2, 1);
    }

    @Override
    public void refresh() {
        FrameLayout frameLayout = getSelectedFrame();

        boolean visible = (frameLayout != null);
        pane.setVisible(visible);
        pane.setManaged(visible);

        if (visible) {
            isUpdating = true;
            widthSpinner.getValueFactory().setValue(frameLayout.getWidth());
            heightSpinner.getValueFactory().setValue(frameLayout.getContentHeight());
            marginTopSpinner.getValueFactory().setValue(frameLayout.getMarginTop());
            marginBottomSpinner.getValueFactory().setValue(frameLayout.getMarginBottom());

            Frame frameData = frameLayout.getFrameData();
            Score score = storageService.getScore();

            for (FrameMetaRow row : metaRows) {
                row.refresh(frameData, score);
            }
            isUpdating = false;
        }
    }

    private void resetDimension(Supplier<Double> defaultValueSupplier, Spinner<Double> spinner, Consumer<Double> onChange) {
        FrameLayout frame = getSelectedFrame();
        if (frame != null) {
            double defaultVal = defaultValueSupplier.get();
            spinner.getValueFactory().setValue(defaultVal);
            onChange.accept(defaultVal);
        }
    }

    private void onWidthChanged(double newWidth) {
        FrameLayout frame = getSelectedFrame();
        if (frame != null) {
            frame.setWidth(newWidth);
            stateManager.notifyScoreChanged();
        }
    }

    private void onHeightChanged(double newHeight) {
        FrameLayout frame = getSelectedFrame();
        if (frame != null) {
            frame.setContentHeight(newHeight);
            stateManager.notifyScoreChanged();
        }
    }

    private void onMarginTopChanged(double newMargin) {
        FrameLayout frame = getSelectedFrame();
        if (frame != null) {
            frame.setMarginTop(newMargin);
            stateManager.notifyScoreChanged();
        }
    }

    private void onMarginBottomChanged(double newMargin) {
        FrameLayout frame = getSelectedFrame();
        if (frame != null) {
            frame.setMarginBottom(newMargin);
            stateManager.notifyScoreChanged();
        }
    }

    private FrameLayout getSelectedFrame() {
        if (stateManager.getSelectedItem() instanceof FrameLayout fl && fl.isSelected()) {
            return fl;
        }
        return null;
    }

    private double getDefaultWidth() {
        var score = storageService.getScore();
        if (score == null) return 0;
        double widthMm = score.getPage().getEffectiveWidthMm();
        return stateManager.getCurrentMode().getStyle().toSp(widthMm);
    }

    private double getDefaultHeight() {
        var mode = stateManager.getCurrentMode();
        if (mode == null) return 0;
        return mode.getStyle().getFrameDefHeight();
    }

    private double getDefaultMarginTop() {
        var mode = stateManager.getCurrentMode();
        if (mode == null) return 0;
        return stateManager.getCurrentMode().getStyle().getFrameDefMarginTop();
    }

    private double getDefaultMarginBottom() {
        var mode = stateManager.getCurrentMode();
        if (mode == null) return 0;
        return mode.getStyle().getFrameDefMarginBottom();
    }


    private HBox createLabeledRow(String labelText, Control control, Button button) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        label.setMinWidth(75);

        control.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(control, Priority.ALWAYS);

        HBox box = new HBox(6, label, control, button);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Spinner<Double> createSpinner(double min, double max, double initial, double step) {
        Spinner<Double> spinner = new Spinner<>(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, step));
        spinner.setEditable(true);
        spinner.getStyleClass().add("properties-spinner");
        return spinner;
    }

    private Button createResetButton(String tooltipText) {
        Button btn = new Button();
        btn.getStyleClass().add("frame-reset-btn");
        btn.setTooltip(new Tooltip(tooltipText));

        Region icon = new Region();
        icon.getStyleClass().add("frame-reset-icon");

        btn.setGraphic(icon);
        btn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        return btn;
    }


    private class FrameMetaRow {
        private final CheckBox checkBox;
        private final Label label;
        private final TextField textField;
        private final Button resetButton;

        private final Function<Frame, String> frameGetter;
        private final BiConsumer<Frame, String> frameSetter;
        private final Function<Score, String> scoreGetter;

        public FrameMetaRow(String labelText,
                            Function<Frame, String> frameGetter,
                            BiConsumer<Frame, String> frameSetter,
                            Function<Score, String> scoreGetter,
                            GridPane grid,
                            int rowIndex) {
            this.frameGetter = frameGetter;
            this.frameSetter = frameSetter;
            this.scoreGetter = scoreGetter;

            checkBox = new CheckBox();

            label = new Label(labelText);
            label.getStyleClass().add("field-label");
            label.setMinWidth(75);

            textField = new TextField();
            textField.getStyleClass().add("metadata-input");
            textField.setMinWidth(40);
            HBox.setHgrow(textField, Priority.ALWAYS);

            resetButton = createResetButton("Przywróć wartość ze śpiewnika");

            HBox rowBox = new HBox(6, checkBox, label, textField, resetButton);
            rowBox.setAlignment(Pos.CENTER_LEFT);

            grid.add(rowBox, 0, rowIndex, 2, 1);

            registerListeners();
        }

        private void registerListeners() {
            checkBox.selectedProperty().addListener((obs, oldV, newV) -> {
                if (isUpdating) return;
                FrameLayout frameLayout = getSelectedFrame();
                if (frameLayout == null) return;

                boolean active = Boolean.TRUE.equals(newV);
                textField.setDisable(!active);
                resetButton.setDisable(!active);

                if (active) {
                    Score score = storageService.getScore();
                    String defaultVal = (score != null) ? scoreGetter.apply(score) : "";
                    if (textField.getText() == null || textField.getText().isEmpty()) {
                        textField.setText(defaultVal != null ? defaultVal : "");
                    }
                    frameSetter.accept(frameLayout.getFrameData(), textField.getText());
                } else {
                    textField.setText("");
                    frameSetter.accept(frameLayout.getFrameData(), null);
                }
                stateManager.notifyScoreChanged();
            });

            textField.textProperty().addListener((obs, oldV, newV) -> {
                if (isUpdating || !checkBox.isSelected()) return;
                FrameLayout frameLayout = getSelectedFrame();
                if (frameLayout != null) {
                    frameSetter.accept(frameLayout.getFrameData(), newV);
                    stateManager.notifyScoreChanged();
                }
            });

            resetButton.setOnAction(e -> {
                FrameLayout frameLayout = getSelectedFrame();
                if (frameLayout == null) return;

                Score score = storageService.getScore();
                String defaultVal = (score != null) ? scoreGetter.apply(score) : "";
                String valToSet = (defaultVal != null) ? defaultVal : "";

                textField.setText(valToSet);
                frameSetter.accept(frameLayout.getFrameData(), valToSet);
                stateManager.notifyScoreChanged();
            });
        }

        public void refresh(Frame frameData, Score score) {
            String value = frameGetter.apply(frameData);
            boolean hasValue = (value != null);

            checkBox.setSelected(hasValue);
            textField.setDisable(!hasValue);
            resetButton.setDisable(!hasValue);

            if (hasValue) {
                textField.setText(value);
            } else {
                textField.setText("");
                String scoreVal = (score != null) ? scoreGetter.apply(score) : null;
                textField.setPromptText(scoreVal != null ? scoreVal : "");
            }
        }
    }
}