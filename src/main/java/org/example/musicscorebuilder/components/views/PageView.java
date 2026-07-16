package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

public class PageView {
    private static final Color PAGE_BACKGROUND_COLOR = Color.rgb(249,249,249);
    private static final Color PAGE_BORDER_COLOR = Color.rgb(170, 170, 170);
    private final SystemView systemView = new SystemView();

    public void draw(GraphicsContext gc, PageLayout page, double offsetX, double offsetY, double sp) {
        double pageX = offsetX + page.getX() * sp;
        double pageY = offsetY;
        double cardWidthPx = page.getWidth() * sp;
        double cardHeightPx = page.getHeight() * sp;
        double cornerRadius = 0.1 * sp;

        gc.setFill(PAGE_BACKGROUND_COLOR);
        gc.fillRoundRect(pageX, pageY, cardWidthPx, cardHeightPx, cornerRadius, cornerRadius);

        gc.setStroke(PAGE_BORDER_COLOR);
        gc.setLineWidth(0.1 * sp);
        gc.strokeRoundRect(pageX, pageY, cardWidthPx, cardHeightPx, cornerRadius, cornerRadius);

        for (SystemLayout system : page.getSystems()) {
            systemView.draw(gc, system, pageX, pageY, sp);
        }
    }
}