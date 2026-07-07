package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.ClefLayout;

public class ClefView extends Pane {
    private final ClefIcon clefIcon;

    public ClefView(ClefLayout clefLayout) {
        this.clefIcon = new ClefIcon(clefLayout.getCode());
        this.setSnapToPixel(false);
        this.getChildren().add(clefIcon);
        update(clefLayout);
    }

    public void update(ClefLayout clefLayout) {
        double targetWidth = clefLayout.getWidth();
        double targetHeight = clefLayout.getHeight();
        this.setPrefWidth(targetWidth);
        this.setPrefHeight(targetHeight);

        clefIcon.setFont(FontManager.getLelandFont(targetHeight));

        var bounds = clefIcon.getBoundsInLocal();
        clefIcon.setLayoutX(-bounds.getMinX());
        clefIcon.setLayoutY(-bounds.getMinY());
    }

    private static class ClefIcon extends Text {
        public ClefIcon(String code) {
            this.setText(code);
            this.setFill(Color.BLACK);
            this.setBoundsType(javafx.scene.text.TextBoundsType.VISUAL);
        }
    }
}