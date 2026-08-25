package org.example.musicscorebuilder.components.dialog;

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

public class CustomConfirmationDialog {
    private final Alert alert;
    private final Label headerLabel;
    private final Label contentLabel;
    private final SVGPath iconPath;

    private ButtonType confirmBtnType;
    private ButtonType denyBtnType;
    private ButtonType cancelBtnType;

    private Runnable onConfirmAction;
    private Runnable onDenyAction;
    private Runnable onCancelAction;

    public CustomConfirmationDialog() {
        this.alert = new Alert(Alert.AlertType.NONE);
        DialogPane dialogPane = alert.getDialogPane();

        this.iconPath = createSVGIcon();

        StackPane iconContainer = new StackPane(iconPath);
        iconContainer.setPadding(new Insets(5, 10, 0, 0));

        this.headerLabel = createHeader();
        this.contentLabel = createContent();

        VBox textContainer = new VBox(8, headerLabel, contentLabel);
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
    }



    public CustomConfirmationDialog setTitle(String title) {
        alert.setTitle(title);
        return this;
    }

    public CustomConfirmationDialog setHeader(String headerText) {
        headerLabel.setText(headerText);
        return this;
    }

    public CustomConfirmationDialog setContent(String contentText) {
        contentLabel.setText(contentText);
        return this;
    }

    public CustomConfirmationDialog setIconSvg(String svgContent, String fillColor) {
        iconPath.setContent(svgContent);
        iconPath.setStyle("-fx-fill: " + fillColor + ";");
        return this;
    }

    public CustomConfirmationDialog setStylesheets(Collection<String> stylesheets) {
        if (stylesheets != null && !stylesheets.isEmpty()) {
            alert.getDialogPane().getStylesheets().addAll(stylesheets);
        }
        return this;
    }




    public CustomConfirmationDialog setConfirmButton(String label, Runnable action) {
        this.confirmBtnType = new ButtonType(label, ButtonBar.ButtonData.OK_DONE);
        this.onConfirmAction = action;
        return this;
    }

    public CustomConfirmationDialog setDenyButton(String label, Runnable action) {
        this.denyBtnType = new ButtonType(label, ButtonBar.ButtonData.NO);
        this.onDenyAction = action;
        return this;
    }

    public CustomConfirmationDialog setCancelButton(String label, Runnable action) {
        this.cancelBtnType = new ButtonType(label, ButtonBar.ButtonData.CANCEL_CLOSE);
        this.onCancelAction = action;
        return this;
    }




    public void showAndWait() {
        alert.getButtonTypes().clear();

        if (confirmBtnType != null) alert.getButtonTypes().add(confirmBtnType);
        if (denyBtnType != null) alert.getButtonTypes().add(denyBtnType);
        if (cancelBtnType != null) alert.getButtonTypes().add(cancelBtnType);

        DialogPane dialogPane = alert.getDialogPane();

        if (confirmBtnType != null) {
            Button confirmBtnNode = (Button) dialogPane.lookupButton(confirmBtnType);
            if (confirmBtnNode != null) {
                confirmBtnNode.getStyleClass().add("primary-button");
            }
        }

        alert.showAndWait().ifPresent(response -> {
            if (response == confirmBtnType && onConfirmAction != null) {
                onConfirmAction.run();
            } else if (response == denyBtnType && onDenyAction != null) {
                onDenyAction.run();
            } else if (response == cancelBtnType && onCancelAction != null) {
                onCancelAction.run();
            }
        });
    }

    private Label createHeader() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(440);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        label.getStyleClass().add("alert-header-text");
        return label;
    }

    private Label createContent() {
        Label label = new Label();
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        label.getStyleClass().add("alert-content-text");
        return label;
    }

    private SVGPath createSVGIcon() {
        SVGPath icon = new SVGPath();
        icon.setContent("M12 2C11.3 2 10.6 2.4 10.2 3.1L1.4 18.3C0.6 19.7 1.6 21.5 3.2 21.5H20.8C22.4 21.5 23.4 19.7 22.6 18.3L13.8 3.1C13.4 2.4 12.7 2 12 2ZM12 4.5L20.8 19.5H3.2L12 4.5ZM11 9V14H13V9H11ZM11 16V18H13V16H11Z");
        icon.setStyle("-fx-fill: #1e293b;");
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);
        return icon;
    }
}
