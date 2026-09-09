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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CustomVerseEditDialog {
    private static final String CIRCLE_INDICATOR = "●";

    private final Dialog<String> dialog;
    private final Label headerLabel;
    private final Label contentLabel;
    private final TextArea verseTextArea;
    private final CheckBox applyToAllCheckBox;
    private final SVGPath iconPath;

    private ButtonType confirmBtnType;
    private ButtonType cancelBtnType;

    private BiConsumer<String, Boolean> onConfirmAction;
    private Runnable onCancelAction;

    private boolean programmaticChange = false;

    public CustomVerseEditDialog() {
        this.dialog = new Dialog<>();
        DialogPane dialogPane = dialog.getDialogPane();

        this.iconPath = createSVGIcon();

        StackPane iconContainer = new StackPane(iconPath);
        iconContainer.setPadding(new Insets(5, 10, 0, 0));

        this.headerLabel = createHeader();
        this.contentLabel = createContent();
        this.verseTextArea = createTextArea();
        this.applyToAllCheckBox = new CheckBox("Zastosuj podział do wszystkich zwrotek");
        this.applyToAllCheckBox.setStyle("-fx-font-size: 13px; -fx-padding: 4px 0 0 0;");

        VBox textContainer = new VBox(8, headerLabel, contentLabel, verseTextArea, applyToAllCheckBox);
        HBox customContent = new HBox(18, iconContainer, textContainer);

        try {
            String dialogCss = Objects.requireNonNull(getClass().getResource("/styles/dialog.css")).toExternalForm();
            dialogPane.getStylesheets().add(dialogCss);
        } catch (Exception e) {
            System.err.println("Nie znaleziono pliku /styles/dialog.css: " + e.getMessage());
        }

        dialogPane.getStyleClass().add("custom-alert");
        customContent.setPadding(new Insets(5, 0, 5, 0));

        dialogPane.setPrefWidth(700);
        dialogPane.setContent(customContent);

        setConfirmButton("Zapisz", (BiConsumer<String, Boolean>) null);
        setCancelButton("Anuluj", null);

        String editSvgPath = "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";
        setIconSvg(editSvgPath, "#3B82F6");
    }

    public CustomVerseEditDialog setTitle(String title) {
        dialog.setTitle(title);
        return this;
    }

    public CustomVerseEditDialog setHeader(String headerText) {
        headerLabel.setText(headerText);
        return this;
    }

    public CustomVerseEditDialog setContent(String contentText) {
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

    public CustomVerseEditDialog setVerseText(String initialText) {
        this.programmaticChange = true;
        try {
            String formattedText = initialText != null ? initialText.replaceAll("\r?\n", CIRCLE_INDICATOR + "\n") : "";
            this.verseTextArea.setText(formattedText);
        } finally {
            this.programmaticChange = false;
        }
        return this;
    }

    public CustomVerseEditDialog setIconSvg(String svgContent, String fillColor) {
        iconPath.setContent(svgContent);
        iconPath.setStyle("-fx-fill: " + fillColor + ";");
        return this;
    }

    public CustomVerseEditDialog setStylesheets(Collection<String> stylesheets) {
        if (stylesheets != null && !stylesheets.isEmpty()) {
            dialog.getDialogPane().getStylesheets().addAll(stylesheets);
        }
        return this;
    }

    public CustomVerseEditDialog setConfirmButton(String label) {
        return setConfirmButton(label, (BiConsumer<String, Boolean>) null);
    }

    public CustomVerseEditDialog setConfirmButton(String label, Consumer<String> action) {
        return setConfirmButton(label, (text, applyToAll) -> {
            if (action != null) {
                action.accept(text);
            }
        });
    }

    public CustomVerseEditDialog setConfirmButton(String label, BiConsumer<String, Boolean> action) {
        this.confirmBtnType = new ButtonType(label, ButtonBar.ButtonData.OK_DONE);
        this.onConfirmAction = action;
        return this;
    }

    public CustomVerseEditDialog setCancelButton(String label) {
        return setCancelButton(label, null);
    }

    public CustomVerseEditDialog setCancelButton(String label, Runnable action) {
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
                return verseTextArea.getText()
                        .replace(CIRCLE_INDICATOR, "")
                        .replaceAll(" +\\n", "\n")
                        .replaceAll(" +$", "");
            }
            return null;
        });

        Platform.runLater(() -> {
            verseTextArea.requestFocus();
            verseTextArea.deselect();
        });

        Optional<String> result = dialog.showAndWait();

        result.ifPresentOrElse(
                editedText -> {
                    if (onConfirmAction != null) {
                        onConfirmAction.accept(editedText, applyToAllCheckBox.isSelected());
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
        label.setMaxWidth(580);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        label.getStyleClass().add("alert-header-text");
        return label;
    }

    private Label createContent() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(580);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        label.getStyleClass().add("alert-content-text");
        return label;
    }

    private TextArea createTextArea() {
        TextArea textArea = new TextArea();
        textArea.setPrefWidth(580);
        textArea.setPrefRowCount(8);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-font-size: 13px; -fx-padding: 6px; -fx-font-family: 'Segoe UI', sans-serif;");
        textArea.getStyleClass().add("custom-input-field");

        disableMouseSelection(textArea);
        textArea.setTextFormatter(createLineBreakTextFormatter());

        return textArea;
    }

    private SVGPath createSVGIcon() {
        SVGPath icon = new SVGPath();
        icon.setScaleX(1.5);
        icon.setScaleY(1.5);
        return icon;
    }

    private TextFormatter<String> createLineBreakTextFormatter() {
        return new TextFormatter<>(change -> {
            if (programmaticChange || !change.isContentChange()) {
                return change;
            }

            if (change.isAdded()) {
                return handleTextAddition(change);
            }

            if (change.isDeleted()) {
                return handleTextDeletion(change);
            }

            return change;
        });
    }

    private TextFormatter.Change handleTextAddition(TextFormatter.Change change) {
        if (change.getText().matches("[\\r\\n]+")) {
            String replacement = CIRCLE_INDICATOR + "\n";
            change.setText(replacement);

            int targetCaretPos = change.getRangeStart() + replacement.length();
            change.setCaretPosition(targetCaretPos);
            change.setAnchor(targetCaretPos);

            return change;
        }
        return null;
    }

    private TextFormatter.Change handleTextDeletion(TextFormatter.Change change) {
        String fullText = change.getControlText();
        int reqStart = change.getRangeStart();
        int reqEnd = change.getRangeEnd();

        List<int[]> markers = findAllLineBreakMarkers(fullText);

        int newStart = reqStart;
        int newEnd = reqEnd;
        int totalMarkerLength = 0;
        boolean overlapsMarker = false;

        for (int[] m : markers) {
            if (Math.max(reqStart, m[0]) < Math.min(reqEnd, m[1])) {
                overlapsMarker = true;
                newStart = Math.min(newStart, m[0]);
                newEnd = Math.max(newEnd, m[1]);
                totalMarkerLength += (m[1] - m[0]);
            }
        }

        if (!overlapsMarker) {
            return null;
        }

        if ((newEnd - newStart) == totalMarkerLength) {
            change.setRange(newStart, newEnd);
            change.setCaretPosition(newStart);
            change.setAnchor(newStart);
            return change;
        }

        return null;
    }

    private List<int[]> findAllLineBreakMarkers(String fullText) {
        List<int[]> markers = new ArrayList<>();
        String marker = CIRCLE_INDICATOR + "\n";

        int idx = 0;
        while ((idx = fullText.indexOf(marker, idx)) != -1) {
            markers.add(new int[]{idx, idx + marker.length()});
            idx += marker.length();
        }

        idx = 0;
        while ((idx = fullText.indexOf("\n", idx)) != -1) {
            final int pos = idx;
            boolean covered = markers.stream().anyMatch(m -> pos >= m[0] && pos < m[1]);
            if (!covered) {
                markers.add(new int[]{pos, pos + 1});
            }
            idx++;
        }

        return markers;
    }

    private void disableMouseSelection(TextArea textArea) {
        textArea.selectionProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getLength() > 0) {
                Platform.runLater(textArea::deselect);
            }
        });
    }
}