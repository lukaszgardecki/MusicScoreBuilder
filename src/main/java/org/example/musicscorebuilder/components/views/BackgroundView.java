package org.example.musicscorebuilder.components.views;

import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

public class BackgroundView extends Pane {
    private ScoreView scoreView;
    private double lastX;
    private double lastY;
    private double zoom = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;

    public BackgroundView(){
        enableDrag();
        enableZoom();
    }

    public void updateContent(ScoreLayout newLayout) {
        if (scoreView == null) {
            scoreView = new ScoreView(newLayout);
            getChildren().add(scoreView);

            scoreView.widthProperty().bind(this.widthProperty());
            scoreView.heightProperty().bind(this.heightProperty());

            scoreView.setViewportTransform(offsetX, offsetY, zoom);
        } else {
            scoreView.update(newLayout);
        }
    }

    private void enableDrag() {
        setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            offsetX += dx;
            offsetY += dy;

            lastX = e.getSceneX();
            lastY = e.getSceneY();

            if (scoreView != null) {
                scoreView.setViewportTransform(offsetX, offsetY, zoom);
            }
        });
    }

    private void enableZoom() {
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!e.isControlDown()) return;
            e.consume();

            if (scoreView == null) return;

            double maxZoom = 15.0;
            double minZoom = 0.1;
            double delta = 1.1;
            double zoomFactor = (e.getDeltaY() > 0) ? delta : 1 / delta;

            double oldZoom = this.zoom;
            this.zoom = Math.max(minZoom, Math.min(this.zoom * zoomFactor, maxZoom));

            double actualFactor = this.zoom / oldZoom;

            double mouseX = e.getX();
            double mouseY = e.getY();

            offsetX = mouseX - (mouseX - offsetX) * actualFactor;
            offsetY = mouseY - (mouseY - offsetY) * actualFactor;

            scoreView.setViewportTransform(offsetX, offsetY, zoom);
        });
    }
}
