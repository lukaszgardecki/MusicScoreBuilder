package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

import java.util.List;

public class SystemView extends HBox {
    private final BarlineView startBarline;
    private final PartsContainer partsContainer;

    public SystemView(SystemLayout systemLayout) {
        startBarline = new BarlineView(systemLayout.getStartBarline());
        startBarline.setEndY(systemLayout.getHeight());
        partsContainer = new PartsContainer(systemLayout);
        this.setSnapToPixel(false);
        this.getChildren().addAll(startBarline, partsContainer);
    }

    public void update(SystemLayout systemLayout) {
        startBarline.setEndY(systemLayout.getHeight());
        partsContainer.update(systemLayout.getParts());

        List<PartLayout> parts = systemLayout.getParts();
        ObservableList<Node> children = partsContainer.getChildren();

        while (children.size() > parts.size()) {
            children.removeLast();
        }

        for (int i = 0; i < parts.size(); i++) {
            if (i < children.size()) {
                ((PartView) children.get(i)).update(parts.get(i));
            } else {
                children.add(new PartView(parts.get(i)));
            }
        }
    }
}

class PartsContainer extends VBox {

    public PartsContainer(SystemLayout systemLayout) {
        setSpacing(systemLayout.getPartSpacing());
        setMaxWidth(Region.USE_PREF_SIZE);
        setFillWidth(false);
        setSnapToPixel(false);
        update(systemLayout.getParts());
    }

    public void update(List<PartLayout> parts) {
        ObservableList<Node> children = this.getChildren();

        while (children.size() > parts.size()) {
            children.removeLast();
        }

        for (int i = 0; i < parts.size(); i++) {
            if (i < children.size()) {
                ((PartView) children.get(i)).update(parts.get(i));
            } else {
                children.add(new PartView(parts.get(i)));
            }
        }
    }
}
