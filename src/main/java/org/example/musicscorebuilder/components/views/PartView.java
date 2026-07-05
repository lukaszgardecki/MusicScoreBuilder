package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;

import java.util.List;

public class PartView extends HBox {
    private final BraceView braceView;
    private final PartContainer partContainer;

    public PartView(PartLayout partLayout) {
        this.braceView = new BraceView(partLayout.getBraceLayout());
        this.partContainer = new PartContainer(partLayout);

        this.setSnapToPixel(false);
        this.getChildren().addAll(braceView, partContainer);
        update(partLayout);
    }

    public void update(PartLayout partLayout) {
        partContainer.update(partLayout);
        braceView.update(partLayout.getBraceLayout());
    }
}

class PartContainer extends StackPane {
    private final StavesContainer stavesContainer;
    private final BarlinesContainer barlineLayer;

    public PartContainer(PartLayout partLayout) {
        this.stavesContainer = new StavesContainer(partLayout.getStaffSpacing());
        this.barlineLayer = new BarlinesContainer();

        this.getChildren().addAll(stavesContainer, barlineLayer);
        update(partLayout);
    }

    public void update(PartLayout partLayout) {
        stavesContainer.update(partLayout);
        barlineLayer.update(partLayout);

        double preciseWidth = partLayout.getStaffLayouts().get(0).getMeasures().stream()
                .mapToDouble(MeasureLayout::getFinalWidth)
                .sum();

        this.setMinWidth(preciseWidth);
        this.setPrefWidth(preciseWidth);
        this.setMaxWidth(preciseWidth);

        stavesContainer.setMinWidth(preciseWidth);
        stavesContainer.setPrefWidth(preciseWidth);
        stavesContainer.setMaxWidth(preciseWidth);

        stavesContainer.layout();
        barlineLayer.layout();
    }
}

class StavesContainer extends VBox {
    public StavesContainer(double spacing) {
        super(spacing);
        setSnapToPixel(false);
    }

    public void update(PartLayout partLayout) {
        List<StaffLayout> staffLayouts = partLayout.getStaffLayouts();
        ObservableList<Node> staffNodes = this.getChildren();

        while (staffNodes.size() > staffLayouts.size()) {
            staffNodes.removeLast();
        }

        for (int i = 0; i < staffLayouts.size(); i++) {
            if (i < staffNodes.size()) {
                ((StaffView) staffNodes.get(i)).update(staffLayouts.get(i));
            } else {
                staffNodes.add(new StaffView(staffLayouts.get(i)));
            }
        }
    }
}

class BarlinesContainer extends HBox {

    BarlinesContainer() {
        setSnapToPixel(false);
    }

    public void update(PartLayout part) {
        List<MeasureLayout> measures = part.getStaffLayouts().get(0).getMeasures();
        ObservableList<Node> barlineNodes = this.getChildren();

        while (barlineNodes.size() > measures.size()) {
            barlineNodes.removeLast();
        }

        for (int i = 0; i < measures.size(); i++) {
            MeasureLayout ml = measures.get(i);

            if (i < barlineNodes.size()) {
                Pane measureContainer = (Pane) barlineNodes.get(i);
                measureContainer.setPrefWidth(ml.getFinalWidth());
                BarlineView barlineView = (BarlineView) measureContainer.getChildren().get(0);
                barlineView.update(ml.getRightBarline());
            } else {
                Pane measureContainer = new Pane();
                measureContainer.setPrefWidth(ml.getFinalWidth());
                BarlineView rightBarline = new BarlineView(ml.getRightBarline());
                rightBarline.endYProperty().bind(this.heightProperty());
                measureContainer.getChildren().add(rightBarline);
                this.getChildren().add(measureContainer);
            }
        }
    }
}