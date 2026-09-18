package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.TempoLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class TempoView extends ComponentView {

    public void draw(GraphicsContext gc, TempoLayout tempoLayout, double segmentX, double segmentY, double sp) {
        if (tempoLayout == null) return;

        String text = tempoLayout.getText();
        if (text.isEmpty()) return;

        double textX = segmentX + (tempoLayout.getXOffset() * sp);
        double textY = segmentY - (tempoLayout.getYOffset() * sp);

        gc.save();

        setupGraphicsContext(gc);
        if (tempoLayout.isSelected()) {
            selectElement(gc, tempoLayout);
        }

        drawText(gc, tempoLayout, textX, textY, sp);

        gc.restore();
    }

    private void setupGraphicsContext(GraphicsContext gc) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
    }

    private void drawText(GraphicsContext gc, TempoLayout tempoLayout, double textX, double textY, double sp) {
        String text = tempoLayout.getText();
        double fontSize = tempoLayout.getFontSize() * sp;

        gc.setFont(FontManager.getFreeSerifBoldFont(fontSize));
        gc.setTextBaseline(VPos.BASELINE);
        gc.fillText(text, textX, textY);
    }

    private void selectElement(GraphicsContext gc, TempoLayout tempoLayout) {
        Color selectColor = Color.web(tempoLayout.getStyle().getSelectColor(tempoLayout));
        gc.setFill(selectColor);
        gc.setStroke(selectColor);
    }
}