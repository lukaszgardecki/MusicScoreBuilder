package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.TimeSigLayout;

public class TimeSigView extends ComponentView {

    public void draw(GraphicsContext gc, TimeSigLayout timeSig, double segmentX, double segmentY, double sp) {
        TimeSigLayout.DigitSign[][] groups = timeSig.getDigitSigns();

        double fontSize = timeSig.getFontSize() * sp;
        gc.setFont(FontManager.getLelandFont(fontSize));

        for (TimeSigLayout.DigitSign[] row : groups) {
            for (TimeSigLayout.DigitSign sign : row) {
                double timeSigX = segmentX + (sign.x() * sp);
                double timeSigY = segmentY + (sign.y() * sp);
                double widthPx = sign.getSignWidth() * sp;
                double heightPx = sign.getHeight() * sp;
                double boxY = segmentY + sign.getBoxY() * sp;

//                fillBackground(gc, Color.RED, timeSigX, boxY, widthPx, heightPx);

                gc.fillText(sign.fontData().getCode(), timeSigX, timeSigY);
            }
        }
    }
}
