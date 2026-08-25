package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.RestLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class RestView extends ComponentView {

    public void draw(GraphicsContext gc, RestLayout rest, double segmentX, double segmentY, double sp) {
        double noteX = segmentX + rest.getX() * sp;
        double noteY = segmentY + rest.getY() * sp;
        double boxX = segmentX + rest.getBoxX() * sp;
        double boxY = segmentY + rest.getBoxY() * sp;
        double widthPx = rest.getBoxWidth() * sp;
        double heightPx = rest.getHeight() * sp;
        double fontSize = rest.getFontSize() * sp;

        gc.setFont(FontManager.getLelandFont(fontSize));
        gc.fillText(rest.getCode(), noteX, noteY);
    }
}