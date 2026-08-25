package org.example.musicscorebuilder.components.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.musicscorebuilder.controller.songbookcontroller.SongbookScoreMetadata;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomNewFileDialog {
    private final Dialog<SongbookScoreMetadata> dialog;
    private final Label headerLabel;
    private final Label contentLabel;
    private final SVGPath iconPath;

    private final TextField numberField;
    private final TextField oldNumberField;
    private final TextField titleField;
    private final TextField subtitleField;
    private final TextField composerField;

    private ButtonType confirmBtnType;
    private ButtonType cancelBtnType;

    private Consumer<SongbookScoreMetadata> onConfirmAction;
    private Runnable onCancelAction;

    public CustomNewFileDialog() {
        this.dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();

        this.iconPath = createSVGIcon();

        StackPane iconContainer = new StackPane(iconPath);
        iconContainer.setPadding(new Insets(5, 10, 0, 0));

        this.headerLabel = createHeader();
        this.contentLabel = createContent();

        this.numberField = createTextField("np. 123");
        this.oldNumberField = createTextField("np. 45");
        this.titleField = createTextField("Tytuł utworu (wymagane)");
        this.subtitleField = createTextField("Podtytuł");
        this.composerField = createTextField("Kompozytor / Autor");

        GridPane grid = createFormGrid();

        VBox textContainer = new VBox(10, headerLabel, contentLabel, grid);
        HBox customContent = new HBox(18, iconContainer, textContainer);

        try {
            String dialogCss = Objects.requireNonNull(getClass().getResource("/styles/dialog.css")).toExternalForm();
            dialogPane.getStylesheets().add(dialogCss);
        } catch (Exception e) {
            System.err.println("Nie znaleziono pliku /styles/dialog.css: " + e.getMessage());
        }

        dialogPane.getStyleClass().add("custom-alert");
        customContent.setPadding(new Insets(5, 0, 5, 0));
        dialogPane.setPrefWidth(540);
        dialogPane.setContent(customContent);

        setConfirmButton("Zapisz", null);
        setCancelButton("Anuluj", null);
    }

    public CustomNewFileDialog setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public CustomNewFileDialog setHeader(String headerText) {
        headerLabel.setText(headerText);
        return this;
    }

    public CustomNewFileDialog setContent(String contentText) {
        if (contentText == null || contentText.trim().isEmpty()) {
            contentLabel.setVisible(false);
            contentLabel.setManaged(false);
        } else {
            contentLabel.setText(contentText);
            contentLabel.setVisible(true);
            contentLabel.setManaged(true);
        }
        return this;
    }

    public CustomNewFileDialog setIconSvg(String svgContent, String fillColor) {
        iconPath.setContent(svgContent);
        iconPath.setStyle("-fx-fill: " + fillColor + ";");
        return this;
    }

    public CustomNewFileDialog setStylesheets(Collection<String> stylesheets) {
        if (stylesheets != null && !stylesheets.isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(stylesheets);
        }
        return this;
    }

    public CustomNewFileDialog setConfirmButton(String label) {
        return setConfirmButton(label, null);
    }

    public CustomNewFileDialog setConfirmButton(String label, Consumer<SongbookScoreMetadata> action) {
        this.confirmBtnType = new ButtonType(label, ButtonBar.ButtonData.OK_DONE);
        this.onConfirmAction = action;
        return this;
    }

    public CustomNewFileDialog setCancelButton(String label) {
        return setCancelButton(label, null);
    }

    public CustomNewFileDialog setCancelButton(String label, Runnable action) {
        this.cancelBtnType = new ButtonType(label, ButtonBar.ButtonData.CANCEL_CLOSE);
        this.onCancelAction = action;
        return this;
    }

    public Optional<SongbookScoreMetadata> showAndWait() {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().clear();

        if (confirmBtnType != null) dialogPane.getButtonTypes().add(confirmBtnType);
        if (cancelBtnType != null) dialogPane.getButtonTypes().add(cancelBtnType);

        if (confirmBtnType != null) {
            Node confirmBtnNode = dialogPane.lookupButton(confirmBtnType);
            if (confirmBtnNode != null) {
                confirmBtnNode.getStyleClass().add("primary-button");

                confirmBtnNode.setDisable(titleField.getText().trim().isEmpty());
                titleField.textProperty().addListener((obs, oldVal, newVal) -> {
                    confirmBtnNode.setDisable(newVal.trim().isEmpty());
                });
            }
        }

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmBtnType) {
                return new SongbookScoreMetadata(
                        numberField.getText().trim(),
                        oldNumberField.getText().trim(),
                        titleField.getText().trim(),
                        subtitleField.getText().trim(),
                        composerField.getText().trim()
                );
            }
            return null;
        });

        Platform.runLater(() -> {
            titleField.requestFocus();
            titleField.selectAll();
        });

        Optional<SongbookScoreMetadata> result = dialog.showAndWait();

        result.ifPresentOrElse(
                metadata -> {
                    if (onConfirmAction != null) {
                        onConfirmAction.accept(metadata);
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

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(5, 0, 0, 0));

        // Reset prefWidth (310px z createTextField) i konfiguracja elastyczności pól numerycznych
        numberField.setPrefWidth(80);
        oldNumberField.setPrefWidth(80);
        numberField.setMaxWidth(Double.MAX_VALUE);
        oldNumberField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(numberField, Priority.ALWAYS);
        HBox.setHgrow(oldNumberField, Priority.ALWAYS);

        Label oldNumberLabel = createFieldLabel("Stary numer:");

        HBox numbersContainer = new HBox(8, numberField, oldNumberLabel, oldNumberField);
        numbersContainer.setAlignment(Pos.CENTER_LEFT);
        numbersContainer.setPrefWidth(310);
        numbersContainer.setMaxWidth(310);

        grid.add(createFieldLabel("Tytuł:*"), 0, 0);
        grid.add(titleField, 1, 0);

        grid.add(createFieldLabel("Podtytuł:"), 0, 1);
        grid.add(subtitleField, 1, 1);

        grid.add(createFieldLabel("Nowy numer:"), 0, 2);
        grid.add(numbersContainer, 1, 2);

        grid.add(createFieldLabel("Kompozytor:"), 0, 3);
        grid.add(composerField, 1, 3);

        return grid;
    }

    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        label.getStyleClass().add("alert-content-text");
        label.setMinWidth(Region.USE_PREF_SIZE); // Blokuje ściskanie i obcinanie do "..."
        return label;
    }

    private Label createHeader() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(420);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        label.getStyleClass().add("alert-header-text");
        return label;
    }

    private Label createContent() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(420);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        label.getStyleClass().add("alert-content-text");
        return label;
    }

    private TextField createTextField(String promptText) {
        TextField textField = new TextField();
        textField.setPromptText(promptText);
        textField.setPrefWidth(310);
        textField.setStyle("-fx-font-size: 13px; -fx-padding: 6px;");
        textField.getStyleClass().add("custom-input-field");
        return textField;
    }

    private SVGPath createSVGIcon() {
        SVGPath icon = new SVGPath();
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);
        return icon;
    }
}