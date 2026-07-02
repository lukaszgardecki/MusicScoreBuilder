package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PartView extends StackPane {
    private final List<Color> measureColors = new ArrayList<>();
    private final VBox stavesContainer;
    private final Pane barlineLayer;

    public PartView(PartLayout part) {
        this.stavesContainer = createStavesContainer(part);
        this.barlineLayer = createBarlineLayer(part);

        this.getChildren().addAll(stavesContainer, barlineLayer);
    }

    private VBox createStavesContainer(PartLayout part) {
        VBox container = new VBox(part.getStaffSpacing());
        part.getStaffLayouts().stream()
                .map(sl -> new StaffView(sl, this::getColorForMeasure))
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

    public void update(PartLayout partLayout) {
        List<StaffLayout> staffLayouts = partLayout.getStaffLayouts();
        ObservableList<Node> staffNodes = stavesContainer.getChildren();

        for (int i = 0; i < staffLayouts.size(); i++) {
            if (i < staffNodes.size()) {
                ((StaffView) staffNodes.get(i)).update(staffLayouts.get(i), this::getColorForMeasure);
            } else {
                stavesContainer.getChildren().add(new StaffView(staffLayouts.get(i), this::getColorForMeasure));
            }
        }
        while (staffNodes.size() > staffLayouts.size()) {
            staffNodes.removeLast();
        }

        syncSystemBarlines(partLayout);
    }

    private void syncSystemBarlines(PartLayout part) {
        barlineLayer.getChildren().clear();
        double currentX = 0;

        List<MeasureLayout> measures = part.getStaffLayouts().get(0).getMeasures();

        for (MeasureLayout ml : measures) {
            currentX += ml.getWidth();

            Line barLine = new Line();
            barLine.setStartX(currentX);
            barLine.setEndX(currentX);
            barLine.setStartY(0);

            barLine.endYProperty().bind(stavesContainer.heightProperty());

            barLine.setStroke(Color.BLACK);
            barlineLayer.getChildren().add(barLine);
        }
    }

    private Color getColorForMeasure(int index) {
        while (measureColors.size() <= index) {
            measureColors.add(generateRandomColor());
        }
        return measureColors.get(index);
    }

    private Color generateRandomColor() {
        Random rand = new Random();
        return Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 0.2);
    }
}