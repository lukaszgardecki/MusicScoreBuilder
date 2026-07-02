package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.musicscorebuilder.components.layout.MeasureLayout;

public class MeasureView extends Pane {
    private Rectangle background;

    public MeasureView(MeasureLayout measureLayout) {
        double width = measureLayout.getWidth();
        this.setMinWidth(width);
        this.setMaxWidth(width);
        this.setPrefWidth(width);

        this.background = new Rectangle();
        this.background.widthProperty().bind(this.widthProperty());
        this.background.heightProperty().bind(this.heightProperty());
        this.background.setFill(Color.TRANSPARENT);

        this.getChildren().add(background);
    }

    public void update(MeasureLayout measureLayout) {

    }
}