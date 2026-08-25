package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.AccidentalLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class AccidentalView extends ComponentView {

    public void draw(GraphicsContext gc, AccidentalLayout acc, double noteX, double noteY, double sp) {
        if (acc == null || !acc.isVisible()) return;
        double accX = noteX + acc.getX() * sp;
        double accY = noteY + acc.getY() * sp;
        double fontSize = acc.getFontSize() * sp;

        gc.setFont(FontManager.getLelandFont(fontSize));
        gc.setFill(Color.web(acc.getScoreStyle().getSelectColor(acc)));
        gc.fillText(acc.getCode(), accX, accY);
    }
}