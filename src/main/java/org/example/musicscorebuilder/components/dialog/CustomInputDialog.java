package org.example.musicscorebuilder.components.dialog;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomInputDialog {
    private final Dialog<String> dialog;
    private final Label headerLabel;
    private final Label contentLabel;
    private final TextField inputTextField;
    private final SVGPath iconPath;

    private ButtonType confirmBtnType;
    private ButtonType cancelBtnType;

    private Consumer<String> onConfirmAction;
    private Runnable onCancelAction;

    public CustomInputDialog() {
        this.dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();

        this.iconPath = createSVGIcon();

        StackPane iconContainer = new StackPane(iconPath);
        iconContainer.setPadding(new Insets(5, 10, 0, 0));

        this.headerLabel = createHeader();
        this.contentLabel = createContent();
        this.inputTextField = createTextField();

        VBox textContainer = new VBox(8, headerLabel, contentLabel, inputTextField);
        HBox customContent = new HBox(18, iconContainer, textContainer);

        try {
            String dialogCss = Objects.requireNonNull(getClass().getResource("/styles/dialog.css")).toExternalForm();
            dialogPane.getStylesheets().add(dialogCss);
        } catch (Exception e) {
            System.err.println("Nie znaleziono pliku /styles/dialog.css: " + e.getMessage());
        }

        dialogPane.getStyleClass().add("custom-alert");
        customContent.setPadding(new Insets(5, 0, 5, 0));
        dialogPane.setPrefWidth(520);
        dialogPane.setContent(customContent);

        setConfirmButton("Zapisz", null);
        setCancelButton("Anuluj", null);

        String editSvgPath = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
        setIconSvg(editSvgPath, "#3B82F6");
    }

    public CustomInputDialog setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public CustomInputDialog setHeader(String headerText) {
        headerLabel.setText(headerText);
        return this;
    }

    public CustomInputDialog setContent(String contentText) {
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

    public CustomInputDialog setDefaultValue(String defaultValue) {
        inputTextField.setText(defaultValue != null ? defaultValue : "");
        return this;
    }

    public CustomInputDialog setPromptText(String promptText) {
        inputTextField.setPromptText(promptText);
        return this;
    }

    public CustomInputDialog setIconSvg(String svgContent, String fillColor) {
        iconPath.setContent(svgContent);
        iconPath.setStyle("-fx-fill: " + fillColor + ";");
        return this;
    }

    public CustomInputDialog setStylesheets(Collection<String> stylesheets) {
        if (stylesheets != null && !stylesheets.isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(stylesheets);
        }
        return this;
    }

    public CustomInputDialog setConfirmButton(String label) {
        return setConfirmButton(label, null);
    }

    public CustomInputDialog setConfirmButton(String label, Consumer<String> action) {
        this.confirmBtnType = new ButtonType(label, ButtonBar.ButtonData.OK_DONE);
        this.onConfirmAction = action;
        return this;
    }

    public CustomInputDialog setCancelButton(String label) {
        return setCancelButton(label, null);
    }

    public CustomInputDialog setCancelButton(String label, Runnable action) {
        this.cancelBtnType = new ButtonType(label, ButtonBar.ButtonData.CANCEL_CLOSE);
        this.onCancelAction = action;
        return this;
    }

    public Optional<String> showAndWait() {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().clear();

        if (confirmBtnType != null) dialogPane.getButtonTypes().add(confirmBtnType);
        if (cancelBtnType != null) dialogPane.getButtonTypes().add(cancelBtnType);

        if (confirmBtnType != null) {
            Button confirmBtnNode = (Button) dialogPane.lookupButton(confirmBtnType);
            if (confirmBtnNode != null) {
                confirmBtnNode.getStyleClass().add("primary-button");
            }
        }

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmBtnType) {
                return inputTextField.getText();
            }
            return null;
        });

        Platform.runLater(() -> {
            inputTextField.requestFocus();
            inputTextField.selectAll();
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresentOrElse(
                text -> {
                    if (onConfirmAction != null) {
                        onConfirmAction.accept(text);
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

    private TextField createTextField() {
        TextField textField = new TextField();
        textField.setPrefWidth(420);
        textField.setStyle("-fx-font-size: 13px; -fx-padding: 8px;");
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