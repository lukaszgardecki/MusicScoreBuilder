package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import org.example.musicscorebuilder.components.layout.BraceLayout;

public class BraceView extends Pane {
    private final BraceIcon braceIcon;

    public BraceView(BraceLayout braceLayout) {
        this.braceIcon = new BraceIcon(braceLayout.getPath());
        this.setSnapToPixel(false);
        this.getChildren().add(braceIcon);
        update(braceLayout);
    }

    public void update(BraceLayout braceLayout) {
        this.setPrefHeight(braceLayout.getHeight());
        this.setMaxHeight(braceLayout.getHeight());
        this.setPrefWidth(braceLayout.getWidth());
        braceIcon.applyScale(braceLayout.getScale());
    }

    private class BraceIcon extends SVGPath {

        public BraceIcon(String path) {
            this.setContent(path);
            this.setFill(Color.BLACK);
        }

        public void applyScale(double scale) {
            this.getTransforms().clear();

            Scale transform = new Scale(scale, scale, 0, 0);
            this.getTransforms().add(transform);
        }
    }
}