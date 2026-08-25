package org.example.musicscorebuilder.components.dialog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.StringConverter;
import org.example.musicscorebuilder.components.music.ModeType;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomSelectModeDialog {
    private final Dialog<ModeType> dialog;
    private final Label headerLabel;
    private final Label contentLabel;
    private final ComboBox<ModeType> modeComboBox;
    private final SVGPath iconPath;

    private ButtonType confirmBtnType;
    private ButtonType cancelBtnType;

    private Consumer<ModeType> onConfirmAction;
    private Runnable onCancelAction;

    public CustomSelectModeDialog() {
        this.dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();

        this.iconPath = createSVGIcon();

        StackPane iconContainer = new StackPane(iconPath);
        iconContainer.setPadding(new Insets(5, 10, 0, 0));

        this.headerLabel = createHeader();
        this.contentLabel = createContent();
        this.modeComboBox = createComboBox();

        VBox textContainer = new VBox(10, headerLabel, contentLabel, modeComboBox);
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

        // Domyślny kolor i SVG: zielony plus
        String plusSvgPath = "M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z";
        setIconSvg(plusSvgPath, "#10B981");

        setConfirmButton("Dodaj", null);
        setCancelButton("Anuluj", null);
    }

    public CustomSelectModeDialog setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public CustomSelectModeDialog setHeader(String headerText) {
        headerLabel.setText(headerText);
        return this;
    }

    public CustomSelectModeDialog setContent(String contentText) {
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

    public CustomSelectModeDialog setItems(List<ModeType> items) {
        if (items != null) {
            modeComboBox.setItems(FXCollections.observableArrayList(items));
            if (!items.isEmpty()) {
                modeComboBox.getSelectionModel().select(0);
            }
        }
        return this;
    }

    public CustomSelectModeDialog setDefaultValue(ModeType defaultValue) {
        if (defaultValue != null) {
            modeComboBox.getSelectionModel().select(defaultValue);
        }
        return this;
    }

    public CustomSelectModeDialog setIconSvg(String svgContent, String fillColor) {
        iconPath.setContent(svgContent);
        iconPath.setStyle("-fx-fill: " + fillColor + ";");
        return this;
    }

    public CustomSelectModeDialog setStylesheets(Collection<String> stylesheets) {
        if (stylesheets != null && !stylesheets.isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(stylesheets);
        }
        return this;
    }

    public CustomSelectModeDialog setConfirmButton(String label) {
        return setConfirmButton(label, null);
    }

    public CustomSelectModeDialog setConfirmButton(String label, Consumer<ModeType> action) {
        this.confirmBtnType = new ButtonType(label, ButtonBar.ButtonData.OK_DONE);
        this.onConfirmAction = action;
        return this;
    }

    public CustomSelectModeDialog setCancelButton(String label) {
        return setCancelButton(label, null);
    }

    public CustomSelectModeDialog setCancelButton(String label, Runnable action) {
        this.cancelBtnType = new ButtonType(label, ButtonBar.ButtonData.CANCEL_CLOSE);
        this.onCancelAction = action;
        return this;
    }

    public Optional<ModeType> showAndWait() {
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().clear();

        if (confirmBtnType != null) dialogPane.getButtonTypes().add(confirmBtnType);
        if (cancelBtnType != null) dialogPane.getButtonTypes().add(cancelBtnType);

        if (confirmBtnType != null) {
            Node confirmBtnNode = dialogPane.lookupButton(confirmBtnType);
            if (confirmBtnNode != null) {
                confirmBtnNode.getStyleClass().add("primary-button");
            }
        }

        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmBtnType) {
                return modeComboBox.getValue();
            }
            return null;
        });

        Platform.runLater(modeComboBox::requestFocus);

        Optional<ModeType> result = dialog.showAndWait();

        result.ifPresentOrElse(
                selectedType -> {
                    if (onConfirmAction != null) {
                        onConfirmAction.accept(selectedType);
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

    private ComboBox<ModeType> createComboBox() {
        ComboBox<ModeType> comboBox = new ComboBox<>(FXCollections.observableArrayList(Arrays.asList(ModeType.values())));
        comboBox.setPrefWidth(420);
        comboBox.setStyle("-fx-font-size: 13px;");
        comboBox.getStyleClass().add("custom-input-field");

        StringConverter<ModeType> converter = new StringConverter<>() {
            @Override
            public String toString(ModeType modeType) {
                return modeType != null ? modeType.getName() : "";
            }

            @Override
            public ModeType fromString(String string) {
                return null;
            }
        };

        comboBox.setConverter(converter);
        if (!comboBox.getItems().isEmpty()) {
            comboBox.getSelectionModel().select(0);
        }

        return comboBox;
    }

    private SVGPath createSVGIcon() {
        SVGPath icon = new SVGPath();
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);
        return icon;
    }
}