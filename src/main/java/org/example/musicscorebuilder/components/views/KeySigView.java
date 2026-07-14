package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.KeySigLayout;

public class KeySigView extends ComponentView {

    public void draw(GraphicsContext gc, KeySigLayout keySig, double segmentX, double segmentY, double sp) {
        double widthPx = keySig.getSignWidth() * sp;
        double heightPx = keySig.getHeight() * sp;
        double fontSize = keySig.getFontSize() * sp;

        for (int i = 0; i < keySig.count(); i++) {
            double keySigX = segmentX + keySig.getX(i) * sp;
            double keySigY = segmentY + keySig.getY(i) * sp;
            double boxY = segmentY + keySig.getBoxY(i) * sp;

//            fillBackground(gc, Color.RED, keySigX, boxY, widthPx, heightPx);

            gc.setFont(FontManager.getLelandFont(fontSize));
            gc.setFill(Color.BLACK);
            gc.fillText(keySig.getCode(), keySigX, keySigY);
        }
    }
}