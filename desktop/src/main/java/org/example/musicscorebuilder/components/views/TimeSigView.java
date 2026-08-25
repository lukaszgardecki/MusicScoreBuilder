package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.TimeSigLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class TimeSigView extends ComponentView {

    public void draw(GraphicsContext gc, TimeSigLayout timeSig, double segmentX, double segmentY, double sp) {
        TimeSigLayout.DigitSign[][] groups = timeSig.getDigitSigns();
        if (groups == null) return;

        double fontSize = timeSig.getFontSize() * sp;
        gc.setFont(FontManager.getLelandFont(fontSize));

        for (int r = 0; r < groups.length; r++) {
            TimeSigLayout.DigitSign[] row = groups[r];
            if (row == null) continue;

            for (int c = 0; c < row.length; c++) {
                TimeSigLayout.DigitSign sign = row[c];
                if (sign == null) continue;

                double timeSigX = segmentX + (sign.x() * sp);
                double timeSigY = segmentY + (sign.y() * sp);

                gc.fillText(sign.fontData().getCode(), timeSigX, timeSigY);
            }
        }
    }
}