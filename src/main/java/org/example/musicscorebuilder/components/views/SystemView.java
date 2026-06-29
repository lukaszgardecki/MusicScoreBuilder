package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.SystemLayout;

public class SystemView extends VBox {

    public SystemView(SystemLayout systemLayout) {
        setSpacing(systemLayout.getPartSpacing());

        systemLayout.getParts().stream()
                .map(PartView::new)
                .forEach(this.getChildren()::add);
    }
}
