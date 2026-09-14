package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.MeasureStaffSelection;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.List;

public class MeasureStaffSelectionView extends ComponentView {

    public void draw(GraphicsContext gc, MeasureStaffSelection selection, double systemX, double systemY, double sp) {
        if (selection == null || selection.getMeasure() == null) return;
        draw(gc, selection, selection.getMeasure().getParent(), systemX, systemY, sp);
    }

    public void draw(GraphicsContext gc, MeasureStaffSelection selection, SystemLayout currentSystem, double systemX, double systemY, double sp) {
        if (selection == null || currentSystem == null) return;

        List<MeasureLayout> measuresToDraw = selection.getMeasuresBySystem().get(currentSystem);
        if (measuresToDraw == null || measuresToDraw.isEmpty()) return;

        MeasureLayout firstMeasure = measuresToDraw.get(0);
        MeasureLayout lastMeasure = measuresToDraw.get(measuresToDraw.size() - 1);
        StaffLayout staff = selection.getStaff();
        if (staff == null) return;

        ScoreStyle scoreStyle = firstMeasure.getScoreStyle();
        double extraHeight = scoreStyle.getSelectionFrameExtraHeight();

        double measureY = (firstMeasure.getY() - 0.5 * extraHeight) * sp + systemY;
        double rectY = measureY + staff.getY() * sp;
        double rectHeight = (staff.getHeight() + extraHeight) * sp;

        double startElemX = selection.getElementsX(firstMeasure);
        double measureX = firstMeasure.getX() * sp + systemX;
        double rectX = measureX + startElemX * sp;

        double rectWidth;
        if (firstMeasure == lastMeasure) {
            double elemWidth = selection.getElementsWidth(firstMeasure);
            if (elemWidth <= 0) {
                elemWidth = firstMeasure.getWidth() - startElemX;
            }
            rectWidth = elemWidth * sp;
        } else {
            double lastElemX = selection.getElementsX(lastMeasure);
            double lastElemWidth = selection.getElementsWidth(lastMeasure);
            if (lastElemWidth <= 0) {
                lastElemWidth = lastMeasure.getWidth() - lastElemX;
            }
            double lastMeasureX = lastMeasure.getX() * sp + systemX;
            double rectEndX = lastMeasureX + (lastElemX + lastElemWidth) * sp;
            rectWidth = rectEndX - rectX;
        }

        double arcRadius = scoreStyle.getSelectionFrameRadius() * sp;

        gc.setStroke(Color.web(scoreStyle.getSelectColor(selection)));
        gc.setLineWidth(scoreStyle.getSelectionFrameWidth() * sp);
        gc.strokeRoundRect(rectX, rectY, rectWidth, rectHeight, arcRadius, arcRadius);
    }
}