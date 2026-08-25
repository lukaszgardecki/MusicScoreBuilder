package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.FooterLayout;

public class FooterView extends ComponentView {

    public void draw(GraphicsContext gc, FooterLayout footer, double pageX, double pageY, double sp) {
        if (!footer.isVisible()) return;
        double footerX = pageX + footer.getX() * sp;
        double footerY = pageY + footer.getY() * sp;
        double footerWidth = footer.getWidth() * sp;
        double footerHeight = footer.getHeight() * sp;
        double centerX = footerX + (footerWidth / 2.0);
        double centerY = footerY + (footerHeight / 2.0);

//        gc.setFill(Color.RED);
//        gc.fillRect(footerX, footerY, footerWidth, footerHeight);

        gc.save();
        drawPageNumber(gc, footer, centerX, centerY, sp);
        gc.restore();
    }

    private void drawPageNumber(GraphicsContext gc, FooterLayout footer, double x, double y, double sp) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        double fontSize = footer.getPageNumFontSize() * sp;
        gc.setFont(Font.font("Times New Roman", fontSize));
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(footer.getPageNum()), x, y);
    }
}