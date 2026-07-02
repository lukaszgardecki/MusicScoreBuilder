package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

import java.util.List;

public class SystemView extends VBox {

    public SystemView(SystemLayout systemLayout) {
        setSpacing(systemLayout.getPartSpacing());

        systemLayout.getParts().stream()
                .map(PartView::new)
                .forEach(this.getChildren()::add);
    }

    public void update(SystemLayout systemLayout) {
        List<PartLayout> systems = systemLayout.getParts();

        for (int i = this.getChildren().size(); i < systems.size(); i++) {
            this.getChildren().add(new PartView(systems.get(i)));
        }

        while (this.getChildren().size() > systems.size()) {
            this.getChildren().removeLast();
        }

        for (int i = 0; i < this.getChildren().size(); i++) {
            ((PartView) this.getChildren().get(i)).update(systems.get(i));
        }
    }
}
