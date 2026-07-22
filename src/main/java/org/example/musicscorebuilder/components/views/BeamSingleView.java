package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.BeamSingleLayout;

public class BeamSingleView extends ComponentView {

    public void draw(GraphicsContext gc, BeamSingleLayout beam, double segmentX, double segmentY, double sp) {
        if (beam == null) return;

        double beamX = segmentX + beam.getX() * sp;
        double beamY = segmentY + beam.getY() * sp;
        double beamBoxY = segmentY + beam.getBoxY() * sp;
        double widthPx = beam.getFontWidth() * sp;
        double heightPx = beam.getHeight() * sp;
        double fontSize = beam.getFontSize() * sp;

//        fillBackground(gc, Util.generateRandomColor(), beamX, beamBoxY, widthPx, heightPx);

        gc.setFont(FontManager.getLelandFont(fontSize));

        gc.setFill(Color.web(beam.getScoreStyle().getSelectColor(beam)));
        gc.fillText(beam.getCode(), beamX, beamY);
    }
}