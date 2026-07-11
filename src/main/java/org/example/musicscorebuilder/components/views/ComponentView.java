package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class ComponentView {

    protected void fillBackground(GraphicsContext gc, Color color, double x, double y, double width, double height) {
        gc.setFill(color);
        gc.fillRect(x, y, width, height);
    }
}
