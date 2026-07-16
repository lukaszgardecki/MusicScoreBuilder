package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.ClefLayout;

public class ClefView extends ComponentView {

    public void draw(GraphicsContext gc, ClefLayout clef, double segmentX, double segmentY, double sp) {
        double clefX = segmentX + clef.getX() * sp;
        double clefY = segmentY + clef.getY() * sp;
        double widthPx = clef.getWidth() * sp;
        double heightPx = clef.getHeight() * sp;
        double fontSize = clef.getFontSize() * sp;
        double boxY = segmentY + clef.getBoxY() * sp;
//        fillBackground(gc, Color.RED, segmentX, boxY, widthPx, heightPx);

        gc.setFont(FontManager.getLelandFont(fontSize));
        gc.fillText(clef.getCode(), clefX, clefY);
    }
}