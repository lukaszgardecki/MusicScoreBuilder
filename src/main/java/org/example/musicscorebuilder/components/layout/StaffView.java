package org.example.musicscorebuilder.components.layout;

import javafx.scene.Group;
import javafx.scene.shape.Line;

public class StaffView extends Group {

    public StaffView(StaffLayout sl) {
        this.setLayoutX(sl.getX());
        this.setLayoutY(sl.getY());

        for (int i = 0; i < sl.getStaff().getLinesNumber(); i++) {
            double lineY = i * sl.getStaff().getLineSpacing();
            Line line = new Line(0, lineY, sl.getWidth(), lineY);
            line.setStrokeWidth(sl.getStaff().getLineWidth());
            this.getChildren().add(line);
        }
    }
}