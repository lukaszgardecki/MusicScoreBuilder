package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.PartLayout;

public class PartView extends StackPane {

    public PartView(PartLayout part) {
        VBox stavesContainer = createStavesContainer(part);
        Pane barlineLayer = createBarlineLayer(part);

        this.getChildren().addAll(stavesContainer, barlineLayer);
    }

    private VBox createStavesContainer(PartLayout part) {
        VBox container = new VBox(part.getStaffSpacing());
        part.getStaffLayouts().stream()
                .map(StaffView::new)
                .forEach(container.getChildren()::add);
        return container;
    }

    private Pane createBarlineLayer(PartLayout part) {
        Pane layer = new Pane();
        layer.setMouseTransparent(true);

        layer.prefWidthProperty().bind(this.widthProperty());
        layer.prefHeightProperty().bind(this.heightProperty());

        addBarlinesToLayer(layer, part);
        return layer;
    }

    private void addBarlinesToLayer(Pane layer, PartLayout part) {
        double currentX = 0;
        for (MeasureLayout ml : part.getStaffLayouts().getFirst().getMeasures()) {
            currentX += ml.getWidth();
            layer.getChildren().add(createBarline(currentX, layer));
        }
    }

    private Line createBarline(double x, Pane layer) {
        Line barLine = new Line(x, 0, x, 0);
        barLine.setStroke(Color.BLACK);
        barLine.endYProperty().bind(layer.heightProperty());
        return barLine;
    }
}