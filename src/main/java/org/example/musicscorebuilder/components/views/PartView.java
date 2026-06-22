package org.example.musicscorebuilder.components.views;

import javafx.scene.Group;
import org.example.musicscorebuilder.components.layout.PartLayout;

public class PartView extends Group {

    public PartView(PartLayout part) {
        this.setLayoutX(part.getX());
        this.setLayoutY(part.getY());

        part.getStaffLayouts().stream()
                .map(StaffView::new)
                .forEach(this.getChildren()::add);
    }
}