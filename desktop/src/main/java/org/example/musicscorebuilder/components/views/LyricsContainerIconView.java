package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class LyricsContainerIconView extends ComponentView {

    public void draw(GraphicsContext gc, double measureX, double measureY, double widthPx, ScoreStyle style, double sp) {
        gc.save();

        double boxSize = 2.5 * sp;
        double x = measureX + widthPx - boxSize;
        double y = measureY - boxSize - (1.0 * sp);
        Color color = Color.web(style.getFrameStrokeColor());

        drawDashedFrame(gc, x, y, boxSize, color, style, sp);
        drawVerseBlocksWithNumbers(gc, x, y, boxSize, color);

        gc.restore();
    }

    private void drawDashedFrame(GraphicsContext gc, double x, double y, double boxSize, Color color, ScoreStyle style, double sp) {
        gc.setStroke(color);
        gc.setLineWidth(style.getFrameStrokeThickness() * sp);
        gc.setLineDashes(style.getFrameStrokeDashLength() * sp, style.getFrameStrokeSpaceLength() * sp);
        gc.strokeRect(x, y, boxSize, boxSize);
        gc.setLineDashes(null);
    }

    private void drawVerseBlocksWithNumbers(GraphicsContext gc, double x, double y, double boxSize, Color color) {
        gc.setFill(color);

        double blockWidth = boxSize * 0.48;
        double blockHeight = boxSize * 0.26;
        double gap = boxSize * 0.10;

        double totalHeight = (2 * blockHeight) + gap;
        double startY = y + (boxSize - totalHeight) / 2.0;

        double fontSize = boxSize * 0.38;
        gc.setFont(Font.font("System", FontWeight.BOLD, fontSize));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double numberX = x + boxSize * 0.22;
        double blockX = x + boxSize * 0.42;

        for (int i = 0; i < 2; i++) {
            double currentY = startY + i * (blockHeight + gap);
            double centerY = currentY + (blockHeight / 2.0);

            gc.fillText(String.valueOf(i + 1), numberX, centerY);
            gc.fillRect(blockX, currentY, blockWidth, blockHeight);
        }
    }
}