package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.BeamLayout;
import org.example.musicscorebuilder.util.Util;

public class BeamView extends ComponentView{

    public void draw(GraphicsContext gc, BeamLayout beam, double segmentX, double segmentY, double sp) {
        if (beam == null) return;

        double beamX = segmentX + beam.getX() * sp;
        double beamY = segmentY + beam.getY() * sp;
        double beamBoxY = segmentY + beam.getBoxY() * sp;
        double widthPx = beam.getFontWidth() * sp;
        double heightPx = beam.getHeight() * sp;
        double fontSize = beam.getFontSize() * sp;

//        fillBackground(gc, Util.generateRandomColor(), beamX, beamBoxY, widthPx, heightPx);

        gc.setFont(FontManager.getLelandFont(fontSize));

        if (beam.isSelected()) gc.setFill(Color.web(beam.getScoreStyle().getElementSelectedColor()));
        else gc.setFill(Color.BLACK);
        gc.fillText(beam.getCode(), beamX, beamY);
    }
}