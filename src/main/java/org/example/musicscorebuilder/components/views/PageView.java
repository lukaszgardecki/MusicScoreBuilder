package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

import java.util.List;

public class PageView {
    private static final Color PAGE_BACKGROUND_COLOR = Color.rgb(249, 249, 249);
    private static final Color PAGE_BORDER_COLOR = Color.rgb(170, 170, 170);
    private final HeaderView headerView = new HeaderView();
    private final SystemView systemView = new SystemView();
    private final FooterView footerView = new FooterView();

    public void draw(GraphicsContext gc, PageLayout page, double offsetX, double offsetY, double sp) {
        double pageX = offsetX + page.getX() * sp;
        double pageY = offsetY + page.getY() * sp;
        double cardWidthPx = page.getWidth() * sp;
        double cardHeightPx = page.getHeight() * sp;
        double cornerRadius = 0.1 * sp;

        double canvasWidth = gc.getCanvas().getWidth();
        double canvasHeight = gc.getCanvas().getHeight();

        gc.setFill(PAGE_BACKGROUND_COLOR);
        gc.fillRoundRect(pageX, pageY, cardWidthPx, cardHeightPx, cornerRadius, cornerRadius);

        gc.setStroke(PAGE_BORDER_COLOR);
        gc.setLineWidth(cornerRadius);
        gc.strokeRoundRect(pageX, pageY, cardWidthPx, cardHeightPx, cornerRadius, cornerRadius);

        if (page.getHeader() != null) {
            double headerHeightPx = page.getHeader().getHeight() * sp;
            if (pageY + headerHeightPx >= 0 && pageY <= canvasHeight) {
                headerView.draw(gc, page.getHeader(), pageX, pageY, sp);
            }
        }

        if (page.getFooter() != null) {
            double footerHeightPx = page.getFooter().getHeight() * sp;
            double footerY = pageY + (page.getHeight() - page.getMarginBottom() - page.getFooter().getHeight()) * sp;
            if (footerY + footerHeightPx >= 0 && footerY <= canvasHeight) {
                footerView.draw(gc, page.getFooter(), pageX, pageY, sp);
            }
        }

        List<SystemLayout> systems = page.getSystems();
        for (int i = 0; i < systems.size(); i++) {
            SystemLayout system = systems.get(i);
            double systemY = pageY + system.getY() * sp;
            double systemHeightPx = system.getHeight() * sp;

            if (systemY + systemHeightPx < 0 || systemY > canvasHeight) {
                continue;
            }
            systemView.draw(gc, system, pageX, pageY, sp);
        }
    }
}