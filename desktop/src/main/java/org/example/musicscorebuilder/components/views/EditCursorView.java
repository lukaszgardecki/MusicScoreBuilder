package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;

public class EditCursorView extends ComponentView {

    public void draw(GraphicsContext gc, CursorLayout cursor, double measureX, double measureY, double sp) {
        double cursorX = cursor.getX() * sp + measureX;
        double cursorY = cursor.getY() * sp + measureY;
        var cursorWidth = cursor.getWidth() * sp;
        var cursorHeight = cursor.getHeight() * sp;
        String colorHex = cursor.getColor();
        gc.save();

        Color bgColor = Color.web(colorHex, 0.3);
        fillBackground(gc, bgColor, cursorX, cursorY, cursorWidth, cursorHeight);

        double cursorThickness = cursor.getThickness() * sp;
        var cursorEndY = cursorY + cursorHeight;
        var cursorColor = Color.web(colorHex);
        gc.setStroke(cursorColor);
        gc.setLineWidth(cursorThickness);
        gc.strokeLine(cursorX, cursorY, cursorX, cursorEndY);

        gc.restore();
    }
}