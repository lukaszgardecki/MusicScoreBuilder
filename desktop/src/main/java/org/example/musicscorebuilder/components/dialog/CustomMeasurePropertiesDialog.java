package org.example.musicscorebuilder.components.dialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomMeasurePropertiesDialog {

    public record MeasureLengthResult(int actualNumerator, int actualDenominator) {}

    private final Dialog<MeasureLengthResult> dialog;

    private final Label nominalNumLabel;
    private final Label nominalDenLabel;
    private final Spinner<Integer> actualNumSpinner;
    private final ComboBox<Integer> actualDenCombo;
    private ButtonType prevBtnType;
    private ButtonType nextBtnType;
    private ButtonType confirmBtnType;
    private ButtonType cancelBtnType;
    private ButtonType applyBtnType;

    private Consumer<MeasureLengthResult> onConfirmAction;
    private Consumer<MeasureLengthResult> onApplyAction;
    private Runnable onCancelAction;
    private Runnable onPreviousAction;
    private Runnable onNextAction;

    public CustomMeasurePropertiesDialog() {
        this.dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();

        dialog.setTitle("Właściwości taktu");
        this.nominalNumLabel = createValueLabel("4");
        this.nominalDenLabel = createValueLabel("4");
        this.actualNumSpinner = new Spinner<>(1, 64, 4);
        this.actualNumSpinner.setEditable(true);
        this.actualNumSpinner.setPrefWidth(70);
        this.actualNumSpinner.getStyleClass().add("custom-input-field");

        this.actualDenCombo = new ComboBox<>(FXCollections.observableArrayList(1, 2, 4, 8, 16, 32, 64));
        this.actualDenCombo.setValue(4);
        this.actualDenCombo.setPrefWidth(70);
        this.actualDenCombo.setStyle("-fx-font-size: 12px;");

        Label sectionLabel = new Label("Długość taktu");
        sectionLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        sectionLabel.setStyle("-fx-text-fill: #374151;");

        VBox measureLengthPanel = createMeasureLengthPanel();

        VBox mainContainer = new VBox(6, sectionLabel, measureLengthPanel);
        mainContainer.setPadding(new Insets(0));
        mainContainer.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(mainContainer, Priority.ALWAYS);

        try {
            String dialogCss = Objects.requireNonNull(getClass().getResource("/styles/dialog.css")).toExternalForm();
            dialogPane.getStylesheets().add(dialogCss);
        } catch (Exception e) {
            System.err.println("Nie znaleziono pliku /styles/dialog.css: " + e.getMessage());
        }

        dialogPane.getStyleClass().add("custom-alert");
        dialogPane.setStyle("-fx-padding: 16px;");
        dialogPane.setMinWidth(600);
        dialogPane.setPrefWidth(600);
        dialogPane.setContent(mainContainer);

        this.prevBtnType = new ButtonType("←", ButtonBar.ButtonData.LEFT);
        this.nextBtnType = new ButtonType("→", ButtonBar.ButtonData.LEFT);
        this.confirmBtnType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        this.cancelBtnType = new ButtonType("Anuluj", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.applyBtnType = new ButtonType("Zastosuj", ButtonBar.ButtonData.APPLY);
    }

    public CustomMeasurePropertiesDialog setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public CustomMeasurePropertiesDialog setMeasureNumber(int number) {
        dialog.setTitle("Właściwości taktu dla taktu " + number);
        return this;
    }

    public CustomMeasurePropertiesDialog setNominalLength(int numerator, int denominator) {
        this.nominalNumLabel.setText(String.valueOf(numerator));
        this.nominalDenLabel.setText(String.valueOf(denominator));
        return this;
    }

    public CustomMeasurePropertiesDialog setActualLength(int numerator, int denominator) {
        this.actualNumSpinner.getValueFactory().setValue(numerator);
        this.actualDenCombo.setValue(denominator);
        return this;
    }

    public CustomMeasurePropertiesDialog setNavigationState(boolean hasPrevious, boolean hasNext) {
        DialogPane dialogPane = dialog.getDialogPane();
        Node prevNode = dialogPane.lookupButton(prevBtnType);
        Node nextNode = dialogPane.lookupButton(nextBtnType);

        if (prevNode != null) prevNode.setDisable(!hasPrevious);
        if (nextNode != null) nextNode.setDisable(!hasNext);

        return this;
    }

    public CustomMeasurePropertiesDialog setOnPrevious(Runnable action) {
        this.onPreviousAction = action;
        return this;
    }

    public CustomMeasurePropertiesDialog setOnNext(Runnable action) {
        this.onNextAction = action;
        return this;
    }

    public CustomMeasurePropertiesDialog setOnApply(Consumer<MeasureLengthResult> action) {
        this.onApplyAction = action;
        return this;
    }

    public CustomMeasurePropertiesDialog setConfirmButton(String label, Consumer<MeasureLengthResult> action) {
        this.confirmBtnType = new ButtonType(label, ButtonBar.ButtonData.OK_DONE);
        this.onConfirmAction = action;
        return this;
    }

    public CustomMeasurePropertiesDialog setCancelButton(String label, Runnable action) {
        this.cancelBtnType = new ButtonType(label, ButtonBar.ButtonData.CANCEL_CLOSE);
        this.onCancelAction = action;
        return this;
    }

    public CustomMeasurePropertiesDialog setStylesheets(Collection<String> stylesheets) {
        if (stylesheets != null && !stylesheets.isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(stylesheets);
        }
        return this;
    }

    public Optional<MeasureLengthResult> showAndWait() {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().clear();
        dialogPane.getButtonTypes().addAll(prevBtnType, nextBtnType, confirmBtnType, cancelBtnType, applyBtnType);
        setupButtonNodes(dialogPane);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmBtnType) {
                return getCurrentResult();
            }
            return null;
        });

        Platform.runLater(actualNumSpinner::requestFocus);

        Optional<MeasureLengthResult> result = dialog.showAndWait();

        result.ifPresentOrElse(
                data -> {
                    if (onConfirmAction != null) {
                        onConfirmAction.accept(data);
                    }
                },
                () -> {
                    if (onCancelAction != null) {
                        onCancelAction.run();
                    }
                }
        );

        return result;
    }

    private void setupButtonNodes(DialogPane dialogPane) {
        // Strzałka w lewo
        Button prevBtnNode = (Button) dialogPane.lookupButton(prevBtnType);
        if (prevBtnNode != null) {
            configureSquareButton(prevBtnNode);
            prevBtnNode.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume();
                if (onPreviousAction != null) {
                    onPreviousAction.run();
                }
            });
        }

        // Strzałka w prawo
        Button nextBtnNode = (Button) dialogPane.lookupButton(nextBtnType);
        if (nextBtnNode != null) {
            configureSquareButton(nextBtnNode);
            nextBtnNode.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume();
                if (onNextAction != null) {
                    onNextAction.run();
                }
            });
        }

        // OK
        Button confirmBtnNode = (Button) dialogPane.lookupButton(confirmBtnType);
        if (confirmBtnNode != null) {
            confirmBtnNode.getStyleClass().add("primary-button");
            configureTextButton(confirmBtnNode);
        }

        // Anuluj
        Button cancelBtnNode = (Button) dialogPane.lookupButton(cancelBtnType);
        if (cancelBtnNode != null) {
            configureTextButton(cancelBtnNode);
        }

        // Zastosuj
        Button applyBtnNode = (Button) dialogPane.lookupButton(applyBtnType);
        if (applyBtnNode != null) {
            configureTextButton(applyBtnNode);
            applyBtnNode.addEventFilter(ActionEvent.ACTION, event -> {
                event.consume();
                if (onApplyAction != null) {
                    onApplyAction.accept(getCurrentResult());
                }
            });
        }
    }

    private void configureSquareButton(Button button) {
        ButtonBar.setButtonUniformSize(button, false);

        button.setMinHeight(32);
        button.setPrefHeight(32);
        button.setMaxHeight(32);

        button.setMinWidth(32);
        button.setPrefWidth(32);
        button.setMaxWidth(32);

        button.setStyle(
                "-fx-padding: 0; " +
                "-fx-alignment: center; " +
                "-fx-font-size: 22px; " +
                "-fx-font-weight: bold;"
        );
    }

    private void configureTextButton(Button button) {
        ButtonBar.setButtonUniformSize(button, false);

        button.setMinHeight(32);
        button.setPrefHeight(32);
        button.setMaxHeight(32);

        button.setMinWidth(Region.USE_PREF_SIZE);
        button.setStyle("-fx-padding: 0 16px; -fx-font-size: 12px; -fx-alignment: center;");
    }

    private MeasureLengthResult getCurrentResult() {
        int num = actualNumSpinner.getValue();
        int den = actualDenCombo.getValue() != null ? actualDenCombo.getValue() : 4;
        return new MeasureLengthResult(num, den);
    }

    private VBox createMeasureLengthPanel() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label nominalTitle = createFieldLabel("Nominalna:");
        Label slash1 = createFieldLabel("/");
        HBox nominalBox = new HBox(12, nominalNumLabel, slash1, nominalDenLabel);
        nominalBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(nominalTitle, 0, 0);
        grid.add(nominalBox, 1, 0);

        Label actualTitle = createFieldLabel("Rzeczywista:");
        Label slash2 = createFieldLabel("/");
        HBox actualBox = new HBox(10, actualNumSpinner, slash2, actualDenCombo);
        actualBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(actualTitle, 0, 1);
        grid.add(actualBox, 1, 1);

        VBox panel = new VBox(grid);
        panel.setPadding(new Insets(14, 18, 14, 18));
        panel.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(panel, Priority.ALWAYS);
        panel.setStyle("-fx-border-color: #E5E7EB; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-background-color: #FAFAFA;");

        return panel;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        label.getStyleClass().add("alert-content-text");
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.getStyleClass().add("alert-content-text");
        return label;
    }
}