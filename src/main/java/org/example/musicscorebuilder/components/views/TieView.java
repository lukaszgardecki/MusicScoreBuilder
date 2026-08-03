package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineJoin;
import org.example.musicscorebuilder.components.layout.BowCurveGeometry;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.TieLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class TieView extends ComponentView {

    public void draw(GraphicsContext gc, TieLayout tie, double systemX, double systemY, double sp) {
        if (tie.getStartNote() == null && tie.getEndNote() == null) return;

        BowCurveGeometry geom = tie.getCurveGeometry();
        if (geom.getDx() <= 0) return;

        NoteLayout refNote = tie.getStartNote() != null ? tie.getStartNote() : tie.getEndNote();
        String colorStr = refNote.getScoreStyle().getSelectColor(tie);
        Color color = Color.web(colorStr);

        double startX = systemX + geom.getStartX() * sp;
        double startY = systemY + geom.getStartY() * sp;
        double endX   = systemX + geom.getEndX() * sp;
        double endY   = systemY + geom.getEndY() * sp;

        double cp1x = systemX + geom.getCp1x() * sp;
        double cp2x = systemX + geom.getCp2x() * sp;

        double cp1yOuter = systemY + geom.getCp1yOuter() * sp;
        double cp2yOuter = systemY + geom.getCp2yOuter() * sp;

        double cp1yInner = systemY + geom.getCp1yInner() * sp;
        double cp2yInner = systemY + geom.getCp2yInner() * sp;

        gc.beginPath();
        gc.moveTo(startX, startY);
        gc.bezierCurveTo(cp1x, cp1yOuter, cp2x, cp2yOuter, endX, endY);
        gc.bezierCurveTo(cp2x, cp2yInner, cp1x, cp1yInner, startX, startY);
        gc.closePath();

        gc.setFill(color);
        gc.fill();

        ScoreStyle style = tie.getScoreStyle();
        double minTipRoundingPx = 1.2;
        double roundWidth = Math.max(minTipRoundingPx, style.getBowTipRoundingFactor() * sp);

        gc.setStroke(color);
        gc.setLineWidth(roundWidth);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.stroke();
    }
}