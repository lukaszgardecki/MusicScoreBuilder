package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;

import java.util.List;

public class StaffView extends Pane {
    private final HBox measuresContainer;

    public StaffView(StaffLayout sl) {
        this.setPrefHeight(sl.getHeight());
        drawStaffLines(sl);

        measuresContainer = new HBox();
        measuresContainer.setFillHeight(true);
        measuresContainer.setPrefHeight(sl.getHeight());

        sl.getMeasures().stream()
                .map(MeasureView::new)
                .forEach(this::addMeasure);

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

    public void update(StaffLayout staffLayout) {
        List<MeasureLayout> measures = staffLayout.getMeasures();
        ObservableList<Node> measureNodes = measuresContainer.getChildren();

        for (int i = 0; i < measures.size(); i++) {
            if (i < measureNodes.size()) {
                ((MeasureView) measureNodes.get(i)).update(measures.get(i));
            } else {
                measureNodes.add(new MeasureView(measures.get(i)));
            }
        }

        while (measureNodes.size() > measures.size()) {
            measureNodes.removeLast();
        }
    }
}