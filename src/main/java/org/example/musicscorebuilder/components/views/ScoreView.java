package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

public class ScoreView extends Canvas {
    GraphicsContext gc = getGraphicsContext2D();
    private final PageView pageView = new PageView();
    private ScoreLayout scoreLayout;
    private final double baseSpatiumPx;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double zoom = 1.0;

    public ScoreView(ScoreLayout layout) {
        this.scoreLayout = layout;

        double dpi = Screen.getPrimary().getDpi();
        if (dpi <= 0) dpi = 96.0;

        double pixelsPerMm = dpi / 25.4;
        double spatiumMm = scoreLayout.getStyle().getSpatiumMm();
        this.baseSpatiumPx = spatiumMm * pixelsPerMm;

        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    public void update(ScoreLayout newLayout) {
        this.scoreLayout = newLayout;
        draw();
    }

    public void setViewportTransform(double offsetX, double offsetY, double zoom) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.zoom = zoom;
        draw();
    }

    private void draw() {
        if (scoreLayout == null || scoreLayout.getPages().isEmpty()) return;
        if (getWidth() <= 0 || getHeight() <= 0) return;
        drawBackground();
        drawPages();
    }

    private void drawBackground() {
        gc.clearRect(0, 0, getWidth(), getHeight());
        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawPages() {
        double sp = zoom * baseSpatiumPx;
        double canvasWidth = getWidth();
        double canvasHeight = getHeight();

        for (PageLayout page : scoreLayout.getPages()) {
            double pageX = offsetX + page.getX() * sp;
            double pageY = offsetY;
            double pageWidthPx = page.getWidth() * sp;
            double pageHeightPx = page.getHeight() * sp;

            if (pageY + pageHeightPx < 0 || pageY > canvasHeight) continue;
            if (pageX + pageWidthPx < 0 || pageX > canvasWidth) continue;

            pageView.draw(gc, page, offsetX, offsetY, sp);
        }
    }

    public ScoreLayout getScoreLayout() { return scoreLayout; }
    public double getBaseSpatiumPx() { return baseSpatiumPx; }
}