package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.util.Util;

public class StaffView extends ComponentView {

    public void draw(GraphicsContext gc, StaffLayout staff, double partX, double partY, double sp) {
        double staffX = partX;
        double staffY = partY;
        double widthPx = staff.getWidth() * sp;
        double heightPx = staff.getHeight() * sp;
        double lineSpacingPx = staff.getLineSpacing() * sp;

//        fillBackground(gc, Util.generateRandomColor(), staffX, staffY, widthPx, heightPx);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(staff.getLineWidth() * sp);
        gc.setLineCap(StrokeLineCap.BUTT);

        for (int i = 0; i < staff.getLinesNumber(); i++) {
            double currentLineY = staffY + (i * lineSpacingPx);
            gc.strokeLine(staffX, currentLineY, staffX + widthPx, currentLineY);
        }
    }
}
