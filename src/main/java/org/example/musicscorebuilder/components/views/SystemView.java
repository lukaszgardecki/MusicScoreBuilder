package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

import java.util.List;

public class SystemView extends Pane {
    private final BarlineView startBarline;
    private final SystemContainer systemContainer;

    public SystemView(SystemLayout systemLayout) {
        this.startBarline = new BarlineView(systemLayout.getStartBarline());
        this.systemContainer = new SystemContainer(systemLayout);
        this.setSnapToPixel(false);
        this.getChildren().addAll(startBarline, systemContainer);
        update(systemLayout);
    }

    public void update(SystemLayout systemLayout) {
        systemContainer.update(systemLayout.getParts());
        startBarline.update(systemLayout.getStartBarline());
        startBarline.setEndY(systemLayout.getHeight());
        startBarline.setViewOrder(-1);
        startBarline.setLayoutX(systemLayout.getBraceWidth());

        List<PartLayout> parts = systemLayout.getParts();
        ObservableList<Node> children = systemContainer.getChildren();

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



class SystemContainer extends VBox {

    public SystemContainer(SystemLayout systemLayout) {
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
