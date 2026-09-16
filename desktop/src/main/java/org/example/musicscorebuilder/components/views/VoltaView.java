package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.example.musicscorebuilder.components.layout.VoltaSliceLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class VoltaView extends ComponentView {

    public void draw(GraphicsContext gc, VoltaSliceLayout voltaSlice, double measureX, double measureY, double sp) {
        if (voltaSlice == null) return;

        double startX = measureX + (voltaSlice.getStartXOffset() * sp);
        double endX = measureX + (voltaSlice.getEndXOffset() * sp);
        double voltaY = measureY - (voltaSlice.getYOffset() * sp);

        gc.save();

        setupGraphicsContext(gc, voltaSlice, sp);
        drawBracket(gc, voltaSlice, startX, endX, voltaY, sp);
        drawText(gc, voltaSlice, startX, voltaY, sp);

        gc.restore();
    }

    private void setupGraphicsContext(GraphicsContext gc, VoltaSliceLayout slice, double sp) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);
        gc.setLineWidth(slice.getLineWidth() * sp);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.setLineJoin(StrokeLineJoin.MITER);
    }

    private void drawBracket(GraphicsContext gc, VoltaSliceLayout slice, double startX, double endX, double voltaY, double sp) {
        double hookHeight = slice.getHookHeight() * sp;

        gc.beginPath();

        // Lewy narożnik i haczyk
        if (slice.isDrawLeftHook()) {
            gc.moveTo(startX, voltaY + hookHeight);
            gc.lineTo(startX, voltaY);
        } else {
            gc.moveTo(startX, voltaY);
        }

        // Poziomy dach klamry
        gc.lineTo(endX, voltaY);

        // Prawy narożnik i haczyk
        if (slice.isDrawRightHook()) {
            gc.lineTo(endX, voltaY + hookHeight);
        }

        gc.stroke();
    }

    private void drawText(GraphicsContext gc, VoltaSliceLayout slice, double startX, double voltaY, double sp) {
        String text = slice.getText();
        if (text == null || text.isEmpty()) return;

        double fontSize = slice.getFontSize() * sp;
        double hookHeight = slice.getHookHeight() * sp;
        double textX = startX + (0.35 * sp);
        double textBaselineY = voltaY + hookHeight;

        gc.setFont(FontManager.getFreeSerifFont(fontSize));
        gc.setTextBaseline(VPos.BASELINE);
        gc.fillText(text, textX, textBaselineY);
    }
}