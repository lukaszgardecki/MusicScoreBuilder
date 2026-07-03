package org.example.musicscorebuilder.components.views;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.example.musicscorebuilder.components.layout.BarlineLayout;

public class BarlineView extends Line {

    public BarlineView(BarlineLayout barlineLayout) {
        setStartY(0);
        setManaged(false);
        setStroke(Color.BLACK);
        update(barlineLayout);
    }

    public void update(BarlineLayout barlineLayout) {
        setStartX(barlineLayout.getX());
        setEndX(barlineLayout.getX());
    }
}