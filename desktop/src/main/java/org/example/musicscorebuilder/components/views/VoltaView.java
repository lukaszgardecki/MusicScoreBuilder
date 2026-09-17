package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import org.example.musicscorebuilder.components.layout.VoltaSliceLayout;
import org.example.musicscorebuilder.components.music.Volta;
import org.example.musicscorebuilder.managers.FontManager;

public class VoltaView extends ComponentView {

    public void draw(GraphicsContext gc, VoltaSliceLayout voltaSlice, double measureX, double measureY, double sp) {
        if (voltaSlice == null) return;

        double startX = measureX + (voltaSlice.getStartXOffset() * sp);
        double endX = measureX + (voltaSlice.getEndXOffset() * sp);
        double voltaY = measureY - (voltaSlice.getYOffset() * sp);

        gc.save();

        setupGraphicsContext(gc, voltaSlice, sp);
        if (voltaSlice.isSelected()) {
            selectElement(gc, voltaSlice);
        }

        drawBracket(gc, voltaSlice, startX, endX, voltaY, sp);
        drawText(gc, voltaSlice, startX, voltaY, sp);

        gc.restore();
    }

    private void setupGraphicsContext(GraphicsContext gc, VoltaSliceLayout slice, double sp) {
        gc.setStroke(Color.BLACK);
        gc.setFill(Color.BLACK);

        double lineWidth = slice.getLineWidth() * sp;
        gc.setLineWidth(lineWidth);
        gc.setLineJoin(StrokeLineJoin.MITER);

        Volta.LineStyle style = slice.getLineStyle();
        if (style == Volta.LineStyle.DASHED) {
            gc.setLineCap(StrokeLineCap.BUTT);
            double dashLen = slice.getDashLength() * sp;
            double dashGap = slice.getDashGap() * sp;
            gc.setLineDashes(dashLen, dashGap);
        } else if (style == Volta.LineStyle.DOTTED) {
            gc.setLineCap(StrokeLineCap.BUTT);
            gc.setLineDashes(lineWidth, lineWidth);
        } else {
            gc.setLineCap(StrokeLineCap.BUTT);
            gc.setLineDashes((double[]) null);
        }
    }

    private void drawBracket(GraphicsContext gc, VoltaSliceLayout slice, double startX, double endX, double voltaY, double sp) {
        double startHookHeight = slice.getStartHookHeight() * sp;
        double endHookHeight = slice.getEndHookHeight() * sp;

        gc.beginPath();

        if (slice.isDrawLeftHook()) {
            gc.moveTo(startX, voltaY + startHookHeight);
            gc.lineTo(startX, voltaY);
        } else {
            gc.moveTo(startX, voltaY);
        }

        gc.lineTo(endX, voltaY);

        if (slice.isDrawRightHook()) {
            gc.lineTo(endX, voltaY + endHookHeight);
        }

        gc.stroke();
    }

    private void drawText(GraphicsContext gc, VoltaSliceLayout slice, double startX, double voltaY, double sp) {
        String text = slice.getText();
        if (text == null || text.isEmpty()) return;

        double fontSize = slice.getFontSize() * sp;
        double startHookHeight = slice.getStartHookHeight() * sp;
        double textX = startX + (slice.getTextXOffset() * sp);
        double textBaselineY = voltaY + startHookHeight;

        gc.setFont(FontManager.getFreeSerifFont(fontSize));
        gc.setTextBaseline(VPos.BASELINE);
        gc.fillText(text, textX, textBaselineY);
    }

    private void selectElement(GraphicsContext gc, VoltaSliceLayout slice) {
        Color selectColor = Color.web(slice.getStyle().getSelectColor(slice));
        gc.setFill(selectColor);
        gc.setStroke(selectColor);
    }
}