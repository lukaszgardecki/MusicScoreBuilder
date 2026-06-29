package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PartLayout;

public class PartView extends VBox {

    public PartView(PartLayout part) {
        this.setSpacing(part.getStaffSpacing());
        this.setPrefHeight(part.getHeight());
        this.setMinHeight(part.getHeight());

        part.getStaffLayouts().stream()
                .map(StaffView::new)
                .forEach(this.getChildren()::add);
    }
}