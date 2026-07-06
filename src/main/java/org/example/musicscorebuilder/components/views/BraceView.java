package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.BraceLayout;

public class BraceView extends Pane {
    private final BraceIcon braceIcon;

    public BraceView(BraceLayout braceLayout) {
        this.braceIcon = new BraceIcon(braceLayout.getCode());
        this.setSnapToPixel(false);
        this.getChildren().add(braceIcon);
        update(braceLayout);
    }

    public void update(BraceLayout braceLayout) {
        double targetWidth = braceLayout.getWidth();
        double targetHeight = braceLayout.getHeight();
        this.setPrefWidth(targetWidth);
        this.setPrefHeight(targetHeight);

        braceIcon.setFont(FontManager.getBravura(targetHeight));

        var bounds = braceIcon.getBoundsInLocal();
        braceIcon.setLayoutX(-bounds.getMinX());
        braceIcon.setLayoutY(-bounds.getMinY());
    }

    private static class BraceIcon extends Text {
        public BraceIcon(String code) {
            this.setText(code);
            this.setFill(Color.BLACK);
            this.setBoundsType(javafx.scene.text.TextBoundsType.VISUAL);
        }
    }
}