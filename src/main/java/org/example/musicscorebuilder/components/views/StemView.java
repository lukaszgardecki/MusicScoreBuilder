package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.example.musicscorebuilder.components.layout.StemLayout;

public class StemView {

    public void draw(GraphicsContext gc, StemLayout stem, double segmentX, double segmentY, double sp) {
        if (stem == null) return;

        double startY = segmentY + stem.getStartY() * sp;
        double endY   = segmentY + stem.getEndY() * sp;
        double baseX = segmentX + stem.getX() * sp;
        double correctedX = baseX + (stem.getWidth() * sp / 2.0);

        if (stem.isSelected()) {
            gc.setStroke(Color.web(stem.getScoreStyle().getElementSelectedColor()));
        } else {
            gc.setStroke(Color.BLACK);
        }

        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setLineWidth(stem.getWidth() * sp);
        gc.strokeLine(correctedX, startY, correctedX, endY);
    }
}