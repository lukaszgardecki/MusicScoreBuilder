package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.example.musicscorebuilder.components.layout.MeasureLayout;

import java.util.Random;

public class MeasureView extends Pane {


    public MeasureView(MeasureLayout measureLayout) {
        double width = measureLayout.getWidth();
        this.setMinWidth(width);
        this.setPrefWidth(width);

        Rectangle bg = generateRectangle(width);
        this.getChildren().add(bg);
    }

    private String getRandomHexColor() {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        return String.format("rgba(%d, %d, %d, 0.2)", r, g, b);
    }

    private Rectangle generateRectangle(double width) {
        Rectangle bg = new Rectangle();
        bg.setWidth(width);
        bg.heightProperty().bind(this.heightProperty());
        bg.setFill(Color.valueOf(getRandomHexColor()));
        return bg;
    }
}