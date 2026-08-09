package org.example.musicscorebuilder.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.example.musicscorebuilder.components.layout.MeasureStaffSelection;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import java.io.IOException;
import java.util.Objects;

public class ContextMenuController {

    @FXML private ContextMenu contextMenu;

    @FXML private MenuItem cutItem;
    @FXML private MenuItem copyItem;
    @FXML private MenuItem pasteItem;
    @FXML private SeparatorMenuItem selectionSeparator;

    @FXML private MenuItem clearMeasuresItem;
    @FXML private MenuItem deleteMeasuresItem;
    @FXML private SeparatorMenuItem measureSeparator;
    @FXML private MenuItem staffPropertiesItem;
    @FXML private Menu insertMeasuresMenu;
    @FXML private MenuItem measurePropertiesItem;

    @FXML private MenuItem notePropertiesItem;

    private Selectable clickedElement;

    @FXML
    public void initialize() {
        contextMenu.setOnShowing(event -> {
            String css = Objects.requireNonNull(getClass().getResource("/styles/context-menu.css")).toExternalForm();
            if (!contextMenu.getScene().getStylesheets().contains(css)) {
                contextMenu.getScene().getStylesheets().add(css);
            }
        });
    }

    public static ContextMenuController create() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    ContextMenuController.class.getResource("/org/example/musicscorebuilder/context-menu.fxml")
            );
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            System.err.println("Błąd ładowania FXML menu kontekstowego: " + e.getMessage());
            return null;
        }
    }

    public void setContext(Selectable clickedElement) {
        this.clickedElement = clickedElement;
        updateMenuVisibility();
    }

    private void updateMenuVisibility() {
        boolean isNote = clickedElement instanceof NoteLayout;
        boolean isMeasure = clickedElement instanceof MeasureStaffSelection;

        setVisible(selectionSeparator, isMeasure || isNote);

        setVisible(clearMeasuresItem, isMeasure);
        setVisible(deleteMeasuresItem, isMeasure);
        setVisible(measureSeparator, isMeasure);
        setVisible(insertMeasuresMenu, isMeasure);
        setVisible(measurePropertiesItem, isMeasure);
        setVisible(staffPropertiesItem, isMeasure);

        setVisible(notePropertiesItem, isNote);
    }

    public void show(Node anchor, double screenX, double screenY) {
        if (contextMenu != null) {
            contextMenu.show(anchor, screenX, screenY);
        }
    }

    public void hide() {
        if (contextMenu != null) {
            contextMenu.hide();
        }
    }

    public boolean isShowing() {
        return contextMenu != null && contextMenu.isShowing();
    }

    @FXML
    private void handleCut(ActionEvent event) {
        System.out.println("Wycięto: " + clickedElement);
    }
    @FXML
    private void handleCopy(ActionEvent event) {
        System.out.println("Skopiowano: " + clickedElement);
    }
    @FXML
    private void handlePaste(ActionEvent event) {
        System.out.println("Wklejono");
    }
    @FXML
    private void handleClearMeasures(ActionEvent event) {
        System.out.println("Czyszczenie taktu: " + clickedElement);
    }
    @FXML
    private void handleDeleteMeasures(ActionEvent event) {
        System.out.println("Usuwanie taktu: " + clickedElement);
    }
    @FXML
    private void handleStaffProperties(ActionEvent event) {
        System.out.println("Właściwości pięciolinii");
    }
    @FXML
    private void handleInsertMeasureBefore(ActionEvent event) {
        System.out.println("Wstaw takt przed");
    }
    @FXML
    private void handleInsertMeasureAfter(ActionEvent event) {
        System.out.println("Wstaw takt po");
    }
    @FXML
    private void handleInsertMultipleMeasures(ActionEvent event) {
        System.out.println("Wstaw wiele taktów");
    }
    @FXML
    private void handleMeasureProperties(ActionEvent event) {
        System.out.println("Właściwości taktu: " + clickedElement);
    }
    @FXML
    private void handleNoteProperties(ActionEvent event) {
        System.out.println("Właściwości nuty: " + clickedElement);
    }

    private void setVisible(MenuItem item, boolean visible) {
        if (item != null) {
            item.setVisible(visible);
        }
    }
}