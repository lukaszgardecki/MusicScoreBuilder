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

        gc.setFont(FontManager.getLelandFont(fontSize));

        for (KeySigLayout.KeySign sign : keySig.getKeySigns()) {
            double keySigX = segmentX + sign.x() * sp;
            double keySigY = segmentY + sign.y() * sp;
            double boxY = segmentY + sign.boxY() * sp;

//            fillBackground(gc, Color.RED, keySigX, boxY, widthPx, heightPx);

            gc.fillText(keySig.getCode(), keySigX, keySigY);
        }
    }
}