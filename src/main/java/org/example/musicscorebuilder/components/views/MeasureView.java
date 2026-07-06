package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.util.Util;

import java.util.stream.IntStream;

public class MeasureView extends HBox {

    public MeasureView(MeasureLayout ml) {
        this.setSnapToPixel(false);
        this.setBackground(Background.fill(Util.generateRandomColor(0.2f)));
        IntStream.range(0, ml.getSegments()).forEach(i -> {
            Segment segment = new Segment();
            HBox.setHgrow(segment, Priority.ALWAYS);
            this.getChildren().add(segment);
        });
        update(ml);
    }

    public void update(MeasureLayout ml) {
        double w = ml.getWidth();
        this.setMinWidth(w);
        this.setPrefWidth(w);
        this.setMaxWidth(w);
        this.layout();
    }
}

class Segment extends Pane {

    Segment() {
//        this.setFill(Color.TRANSPARENT);
        this.setSnapToPixel(false);
        this.setBackground(Background.fill(Util.generateRandomColor(0.3f)));
    }
}