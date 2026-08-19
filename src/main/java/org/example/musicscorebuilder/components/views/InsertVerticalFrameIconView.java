package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class InsertVerticalFrameIconView {

    public void draw(GraphicsContext gc, double rectX, double rectY, double boxSize) {
        gc.save();

        Color iconColor = Color.BLACK;
        gc.setStroke(iconColor);
        gc.setLineWidth(1.3);
        gc.setLineDashes(0.3, 6.0);
        gc.strokeRect(rectX, rectY, boxSize, boxSize);
        gc.setLineDashes(null);

        double centerX = rectX + boxSize / 2.0;
        double centerY = rectY + boxSize / 2.0;

        double arrowHeight = boxSize * 0.55;
        double headWidth = boxSize * 0.32;
        double headHeight = arrowHeight * 0.3;

        double topY = centerY - arrowHeight / 2.0;
        double bottomY = centerY + arrowHeight / 2.0;

        // Pionowa linia (trzon strzałki)
        gc.setLineWidth(2.0);
        gc.strokeLine(centerX, topY + headHeight * 0.5, centerX, bottomY - headHeight * 0.5);

        // Grot górny
        gc.setFill(iconColor);
        gc.fillPolygon(
                new double[]{centerX, centerX - headWidth / 2.0, centerX + headWidth / 2.0},
                new double[]{topY, topY + headHeight, topY + headHeight},
                3
        );

        // Grot dolny
        gc.fillPolygon(
                new double[]{centerX, centerX - headWidth / 2.0, centerX + headWidth / 2.0},
                new double[]{bottomY, bottomY - headHeight, bottomY - headHeight},
                3
        );

        gc.restore();
    }
}