package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.example.musicscorebuilder.components.layout.StemLayout;
import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.util.Util;

public class StemView extends ComponentView {

    public void draw(GraphicsContext gc, StemLayout stem, double segmentX, double segmentY, double sp) {
        if (stem == null) return;

        double startY = segmentY + stem.getStartY() * sp;
        double endY = segmentY + stem.getEndY() * sp;
        double baseX = segmentX + stem.getX() * sp;
        double height = Math.abs(endY - startY);
        double correctedX = baseX + (stem.getWidth() * sp / 2.0);

//        fillBackground(gc, Util.generateRandomColor(0.4f), baseX, Math.min(startY, endY), stem.getWidth()*sp, height);

        var color = stem.getParent() instanceof GhostNoteLayout ghost
                ? ghost.getColor()
                : stem.getScoreStyle().getSelectColor(stem);
        gc.setStroke(Color.web(color));
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setLineWidth(stem.getWidth() * sp);
        gc.strokeLine(correctedX, startY, correctedX, endY);
    }
}