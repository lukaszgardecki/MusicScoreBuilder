package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.example.musicscorebuilder.components.layout.StaffLayout;

public class StaffView extends Pane {
    private HBox measuresContainer;

    public StaffView(StaffLayout sl) {
        this.setPrefHeight(sl.getHeight());
        drawStaffLines(sl);

        measuresContainer = new HBox();
        sl.getMeasures().stream()
                .map(MeasureView::new)
                .forEach(this::addMeasure);
        measuresContainer.setFillHeight(true);
        measuresContainer.setPrefHeight(sl.getHeight());
        this.getChildren().add(measuresContainer);
    }

    private void drawStaffLines(StaffLayout sl) {
        for (int i = 0; i < sl.getStaff().getLinesNumber(); i++) {
            double lineY = i * sl.getStaff().getLineSpacing();
            Line line = new Line();
            line.setStartY(0);
            line.setEndY(0);
            line.setLayoutY(lineY);
            line.setStrokeWidth(sl.getStaff().getLineWidth());
            line.endXProperty().bind(this.widthProperty());
            this.getChildren().add(line);
        }
    }

    public void addMeasure(MeasureView measureView) {
        measuresContainer.getChildren().add(measureView);
    }
}