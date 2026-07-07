package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.util.Util;

public class MeasureView extends HBox {

    public MeasureView(MeasureLayout ml) {
        this.setSnapToPixel(false);
//        this.setBackground(Background.fill(Util.generateRandomColor(0.2f)));

        ml.getSegments().stream()
                .filter(SegmentLayout::isGenerated)
                .forEach(segment -> {
                    SegmentView segmentView = new SegmentView(segment);
                    HBox.setHgrow(segmentView, Priority.ALWAYS);
                    this.getChildren().add(segmentView);
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

class SegmentView extends Pane {

    SegmentView(SegmentLayout segment) {
        this.setSnapToPixel(false);

        Color color = switch (segment.getType()) {
            case CLEF -> Color.RED;
            case KEY_SIG -> Util.generateRandomColor(0.3f);
            case TIME_SIG -> Color.LIME;
            case CHORD_REST -> Util.generateRandomColor(0.3f);
            case BARLINE -> Util.generateRandomColor(0.3f);
        };

        this.setBackground(Background.fill(color));
//        this.setBackground(Background.fill(Color.TRANSPARENT));
    }
}