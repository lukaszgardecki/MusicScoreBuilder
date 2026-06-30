package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.example.musicscorebuilder.components.layout.MeasureLayout;

import java.util.Random;

public class MeasureView extends Pane {


    public MeasureView(MeasureLayout measureLayout) {
        double width = measureLayout.getWidth();
        this.setMinWidth(width);
        this.setPrefWidth(width);

        Rectangle bg = generateRectangle(width);
        Line barLine = generateBarline(width);

        this.getChildren().add(bg);
        this.getChildren().add(barLine);
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

    private Line generateBarline(double xPos) {
        Line barLine = new Line();
        barLine.setStartX(xPos);
        barLine.setEndX(xPos);
        barLine.setStartY(0);
        barLine.endYProperty().bind(this.heightProperty());
        barLine.setStroke(Color.BLACK);
        return barLine;
    }
}