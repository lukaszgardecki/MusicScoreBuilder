package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.MeasureStaffSelection;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.layout.StaffLayout;

public class MeasureStaffSelectionView extends ComponentView {

    public void draw(GraphicsContext gc, MeasureStaffSelection selection, double systemX, double systemY, double sp) {
        if (selection == null) return;

        MeasureLayout measure = selection.getMeasure();
        StaffLayout staff = selection.getStaff();
        ScoreStyle scoreStyle = measure.getScoreStyle();
        double extraHeight = scoreStyle.getSelectionFrameExtraHeight();

        double measureX = measure.getX() * sp + systemX;
        double measureY = (measure.getY() - 0.5*extraHeight) * sp + systemY;
        double rectX = measureX + selection.getElementsX() * sp;
        double rectY = measureY + staff.getY() * sp;

        double rectWidth = selection.getElementsWidth() * sp;

        // Zabezpieczenie: Jeśli brak segmentów CHORDREST, daj szerokość pozostałego obszaru taktu
        if (rectWidth <= 0) {
            rectWidth = (measure.getWidth() - selection.getElementsX()) * sp;
        }


        double rectHeight = (staff.getHeight() + extraHeight) * sp;
        double arcRadius = scoreStyle.getSelectionFrameRadius() * sp;

        gc.setStroke(Color.web(scoreStyle.getSelectColor(selection)));
        gc.setLineWidth(scoreStyle.getSelectionFrameWidth() * sp);
        gc.strokeRoundRect(rectX, rectY, rectWidth, rectHeight, arcRadius, arcRadius);
    }
}