package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.musicscorebuilder.components.layout.MeasureLayout;

public class MeasureView extends Pane {
    private Rectangle background;

    public MeasureView(MeasureLayout ml) {
        this.background = new Rectangle();
        this.background.widthProperty().bind(this.widthProperty());
        this.background.heightProperty().bind(this.heightProperty());
        this.background.setFill(Color.TRANSPARENT);

        this.getChildren().add(background);
        update(ml);
    }

    public void update(MeasureLayout ml) {
        double w = ml.getFinalWidth();
        this.setMinWidth(w);
        this.setPrefWidth(w);
        this.setMaxWidth(w);

        this.layout();
    }
}