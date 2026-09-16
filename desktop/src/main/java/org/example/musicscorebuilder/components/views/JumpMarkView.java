package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.JumpMarkLayout;
import org.example.musicscorebuilder.components.layout.JumpSignLayout;
import org.example.musicscorebuilder.components.layout.JumpTextLayout;
import org.example.musicscorebuilder.managers.FontManager;

public class JumpMarkView {

    public void draw(GraphicsContext gc, JumpMarkLayout jumpMark, double measureX, double measureY, double sp) {
        if (jumpMark instanceof JumpSignLayout sign) {
            drawSign(gc, sign, measureX, measureY, sp);
        } else if (jumpMark instanceof JumpTextLayout text) {
            drawText(gc, text, measureX, measureY, sp);
        }
    }

    private void drawSign(GraphicsContext gc, JumpSignLayout signLayout, double measureX, double measureY, double sp) {
        double fontSize = signLayout.getFontSize() * sp;
        double signX = measureX + signLayout.getX() * sp;
        double signY = measureY + signLayout.getY() * sp;

        gc.save();
        gc.setFill(Color.BLACK);
        gc.setFont(FontManager.getLelandFont(fontSize));
        switch (signLayout.getPosition()) {
            case START_OF_MEASURE -> gc.setTextAlign(TextAlignment.LEFT);
            case END_OF_MEASURE -> gc.setTextAlign(TextAlignment.RIGHT);
        }
        gc.fillText(signLayout.getCode(), signX, signY);
        gc.restore();
    }

    private void drawText(GraphicsContext gc, JumpTextLayout textLayout, double measureX, double measureY, double sp) {
        double fontSize = textLayout.getFontSize() * sp;
        double textX = measureX + textLayout.getX() * sp;
        double textY = measureY + textLayout.getY() * sp;

        gc.save();
        gc.setFill(Color.BLACK);
        gc.setFont(FontManager.getFreeSerifFont(fontSize));
        switch (textLayout.getPosition()) {
            case START_OF_MEASURE -> gc.setTextAlign(TextAlignment.LEFT);
            case END_OF_MEASURE -> gc.setTextAlign(TextAlignment.RIGHT);
        }
        gc.fillText(textLayout.getText(), textX, textY);
        gc.restore();
    }
}