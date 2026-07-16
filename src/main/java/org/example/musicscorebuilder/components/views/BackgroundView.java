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
    private boolean wasDragged = false;

    public BackgroundView(){
        enableDrag();
        enableZoom();
        centerFirstPage();
    }

    public void updateContent(ScoreLayout newLayout) {
        if (scoreView == null) {
            scoreView = new ScoreView(newLayout);
            getChildren().add(scoreView);

            scoreView.widthProperty().bind(this.widthProperty());
            scoreView.heightProperty().bind(this.heightProperty());
            scoreView.widthProperty().addListener((obs, oldVal, newVal) -> centerFirstPage());
            scoreView.heightProperty().addListener((obs, oldVal, newVal) -> centerFirstPage());

            scoreView.setViewportTransform(offsetX, offsetY, zoom);
        } else {
            scoreView.update(newLayout);
        }
    }

    private void centerFirstPage() {
        if (scoreView == null || scoreView.getScoreLayout() == null) return;
        if (scoreView.getScoreLayout().getPages().isEmpty()) return;

        var page = scoreView.getScoreLayout().getPages().getFirst();

        double sp = zoom * scoreView.getBaseSpatiumPx();
        double pageWidthPx = page.getWidth() * sp;
        double pageHeightPx = page.getHeight() * sp;

        double canvasWidth = scoreView.getWidth();
        double canvasHeight = scoreView.getHeight();

        offsetX = (canvasWidth - pageWidthPx) / 2.0;
        offsetY = (canvasHeight - pageHeightPx) / 2.0;

        scoreView.setViewportTransform(offsetX, offsetY, zoom);
    }

    private void enableDrag() {
        setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
            wasDragged = false;
        });

        setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            if (Math.abs(dx) > 3 || Math.abs(dy) > 3) wasDragged = true;

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

    public boolean wasLastMousePressJustClick() { return !wasDragged; }
    public double toModelX(double screenX) { return (screenX - offsetX) / getActualSp(); }
    public double toModelY(double screenY) { return (screenY - offsetY) / getActualSp(); }

    private double getActualSp() {
        if (scoreView == null) return 10.0;
        return zoom * scoreView.getBaseSpatiumPx();
    }
}
