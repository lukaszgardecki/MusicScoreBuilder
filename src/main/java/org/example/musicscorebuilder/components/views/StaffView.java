package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.example.musicscorebuilder.components.layout.StaffLayout;

public class StaffView extends Pane {

    public StaffView(StaffLayout sl) {
        this.setPrefHeight(sl.getHeight());

        for (int i = 0; i < sl.getStaff().getLinesNumber(); i++) {
            double lineY = i * sl.getStaff().getLineSpacing();
            Line line = new Line(0, 0, sl.getWidth(), 0);
            line.setStrokeWidth(sl.getStaff().getLineWidth());
            line.setLayoutY(lineY);
            this.getChildren().add(line);
        }
    }
}