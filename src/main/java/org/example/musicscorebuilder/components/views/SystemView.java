package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

import java.util.List;

public class SystemView extends VBox {

    public SystemView(SystemLayout systemLayout) {
        setSpacing(systemLayout.getPartSpacing());
        this.setMaxWidth(Region.USE_PREF_SIZE);
        setFillWidth(false);

        systemLayout.getParts().stream()
                .map(PartView::new)
                .forEach(this.getChildren()::add);
    }

    public void update(SystemLayout systemLayout) {
        List<PartLayout> systems = systemLayout.getParts();
        ObservableList<Node> children = this.getChildren();

        while (children.size() > systems.size()) {
            children.removeLast();
        }

        for (int i = 0; i < systems.size(); i++) {
            if (i < children.size()) {
                ((PartView) children.get(i)).update(systems.get(i));
            } else {
                children.add(new PartView(systems.get(i)));
            }
        }
    }
}
