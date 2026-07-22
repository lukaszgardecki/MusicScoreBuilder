package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreStyle;
import org.example.musicscorebuilder.components.layout.StemDirection;

public class BeamGroupView extends ComponentView {

    public void draw(GraphicsContext gc, BeamGroupLayout beamGroup, double measureX, double measureY, double sp) {
        if (beamGroup.isEmpty()) return;

        NoteLayout first = beamGroup.getFirstNote();
        NoteLayout last = beamGroup.getLastNote();
        if (first == null || last == null) return;

        ScoreStyle style = first.getScoreStyle();
        var stemWidth = style.getNoteStemWidth() * sp;
        var halfBeamThickness = 0.5 * style.getNoteBeamThickness() * sp;

        double firstStemLocalX = (first.getStem().getDirection() == StemDirection.UP) ? first.getBoxWidth() - first.getStem().getWidth() : 0;
        double startX = measureX + first.getParent().getX() * sp + first.getX() * sp + firstStemLocalX * sp;

        double lastStemLocalX = (last.getStem().getDirection() == StemDirection.UP) ? last.getBoxWidth() - last.getStem().getWidth() : 0;
        double endX = measureX + last.getParent().getX() * sp + last.getX() * sp + lastStemLocalX * sp + stemWidth;

        double startY = measureY + first.getParent().getY() * sp + first.getStem().getEndY() * sp;
        double endY = measureY + last.getParent().getY() * sp + last.getStem().getEndY() * sp;

        double[] xPoints = { startX, endX, endX, startX };
        double[] yPoints = {
                startY - halfBeamThickness, // Up Left
                endY - halfBeamThickness,   // Up Right
                endY + halfBeamThickness,   // Down Right
                startY + halfBeamThickness  // Down Left
        };

        if (beamGroup.isSelected()) gc.setFill(Color.web(style.getElementSelectedColor()));
        else gc.setFill(Color.BLACK);
        gc.fillPolygon(xPoints, yPoints, 4);
    }
}