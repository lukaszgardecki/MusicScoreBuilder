package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;


public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane pageContainer;
    @FXML private StackPane page;

    private double lastX;
    private double lastY;
    private double zoom = 1.0;

    @FXML
    public void initialize() {
        scrollPane.setPannable(false);
        enableDrag();
        enableZoom();
    }

    private void enableDrag() {
        pageContainer.setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        pageContainer.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            pageContainer.setTranslateX(pageContainer.getTranslateX() + dx);
            pageContainer.setTranslateY(pageContainer.getTranslateY() + dy);

            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });
    }

    private void enableZoom() {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {

            // Zoom tylko gdy CTRL wciśnięty
//            if (!e.isControlDown()) return;

            double zoomFactor = 1.05;

            if (e.getDeltaY() > 0) {
                zoom *= zoomFactor;
            } else {
                zoom /= zoomFactor;
            }

            // ograniczenia
            zoom = Math.max(0.2, Math.min(zoom, 5.0));

            page.setScaleX(zoom);
            page.setScaleY(zoom);

            e.consume();
        });
    }

}
