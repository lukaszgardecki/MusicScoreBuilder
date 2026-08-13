package org.example.musicscorebuilder.controller.songbookcontroller;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.shape.SVGPath;

public class SongbookListCell extends ListCell<SongbookItem> {

    @Override
    protected void updateItem(SongbookItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            return;
        }

        HBox container = new HBox(8);
        container.setAlignment(Pos.CENTER_LEFT);

        SVGPath icon = new SVGPath();
        Label label = new Label();

        switch (item.type()) {
            case PARENT_DIR -> {
                icon.setContent("M11 19V7.83l-5.58 5.59L4 12l8-8 8 8-1.41 1.41L13 7.83V19h-2z");
                icon.setStyle("-fx-fill: #6B7280;");
                label.setText(".. (Folder wyżej)");
                label.setStyle("-fx-text-fill: #6B7280; -fx-font-style: italic;");
            }
            case DIRECTORY -> {
                icon.setContent("M10 4H2a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-8l-2-2z");
                icon.setStyle("-fx-fill: #F59E0B;");
                label.setText(item.displayName());
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: #1F2937;");
            }
            case FILE -> {
                icon.setContent("M6 2a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13z");
                icon.setStyle("-fx-fill: #3B82F6;");
                label.setText(item.displayName());
                label.setStyle("-fx-text-fill: #374151;");
            }
        }

        container.getChildren().addAll(icon, label);
        setGraphic(container);
        setText(null);
    }
}