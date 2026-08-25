package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.managers.FontManager;
import org.example.musicscorebuilder.components.layout.BraceLayout;

public class BraceView extends ComponentView {

    public void draw(GraphicsContext gc, BraceLayout braceLayout, double partX, double partY, double sp) {
        double braceX = braceLayout.getX() * sp + partX;
        double braceY = braceLayout.getY() * sp + partY;
        double widthPx = braceLayout.getWidth() * sp;
        double heightPx = braceLayout.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), braceX, braceY, widthPx, heightPx);

        Font oldFont = gc.getFont();
        VPos oldBaseline = gc.getTextBaseline();
        TextAlignment oldAlign = gc.getTextAlign();

        gc.setFont(FontManager.getLelandFont(heightPx));
        gc.setFill(Color.BLACK);

        gc.setTextBaseline(VPos.BASELINE);
        gc.setTextAlign(TextAlignment.LEFT);

        double correctedY = braceY + heightPx;
        gc.fillText(braceLayout.getCode(), braceX, correctedY);

        gc.setFont(oldFont);
        gc.setTextBaseline(oldBaseline);
        gc.setTextAlign(oldAlign);
    }
}
