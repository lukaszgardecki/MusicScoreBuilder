package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.DotLayout;
import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class AugmentationDotView extends ComponentView {

    public void draw(GraphicsContext gc, DotLayout dot, double noteX, double noteY, double sp) {
        double dotX = noteX + dot.getX() * sp;
        double dotY = noteY + dot.getY() * sp;
        double fontSize = dot.getFontSize() * sp;
        var color = dot.getParent() instanceof GhostNoteLayout ghost
                ? ghost.getColor()
                : dot.getScoreStyle().getSelectColor(dot);

        gc.setFont(FontManager.getLelandFont(fontSize));
        gc.setFill(Color.web(color));
        gc.fillText(dot.getCode(), dotX, dotY);
    }
}