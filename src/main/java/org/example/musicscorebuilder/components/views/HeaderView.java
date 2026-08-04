package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.*;
import org.example.musicscorebuilder.components.layout.HeaderLayout;

public class HeaderView extends ComponentView {

    public void draw(GraphicsContext gc, HeaderLayout header, double pageX, double pageY, double sp) {
        double headerX = pageX + header.getX() * sp;
        double headerY = pageY + header.getY() * sp;
        double headerWidth = header.getWidth() * sp;
        double headerHeight = header.getHeight() * sp;

        gc.save();
//        gc.setFill(Color.GREEN);
//        gc.fillRect(headerX, headerY, headerWidth, headerHeight);

        double centerX = headerX + (headerWidth / 2.0);
        double rightX = headerX + headerWidth;

        drawNumber(gc, header, headerX, headerY, sp);
        drawTitle(gc, header, centerX, headerY, sp);
        drawSubtitle(gc, header, centerX, headerY, sp);
        drawComposer(gc, header, rightX, headerY, sp);
        gc.restore();
    }

    private void drawNumber(GraphicsContext gc, HeaderLayout header, double x, double y, double sp) {
        Text topNode = createNewNumberTextNode(header, sp);
        Text bottomNode = createOldNumberTextNode(header, sp);

        boolean hasTop = !topNode.getText().isEmpty();
        boolean hasBottom = !bottomNode.getText().isEmpty();

        double topWidth = hasTop ? topNode.getLayoutBounds().getWidth() : 0;
        double topHeight = hasTop ? topNode.getLayoutBounds().getHeight() : 0;

        double bottomWidth = hasBottom ? bottomNode.getLayoutBounds().getWidth() : 0;
        double bottomHeight = hasBottom ? bottomNode.getLayoutBounds().getHeight() : 0;

        double spacing = header.getNumBoxSpacing() * sp;
        double paddingX = header.getNumBoxPaddingX() * sp;
        double paddingY = header.getNumBoxPaddingY() * sp;

        double contentWidth = Math.max(topWidth, bottomWidth);
        double contentHeight = 0;
        if (hasTop) contentHeight += topHeight;
        if (hasBottom) {
            if (hasTop) contentHeight += spacing;
            contentHeight += bottomHeight;
        }

        double rectWidth = Math.max(header.getNumBoxMinWidth() * sp, contentWidth + 2 * paddingX);
        double rectHeight = Math.max(header.getNumBoxMinHeight() * sp, contentHeight + 2 * paddingY);

        drawNumberBox(gc, header, x, y, rectWidth, rectHeight, sp);

        double centerX = x + (rectWidth / 2.0);
        double startY = y + (rectHeight - contentHeight) / 2.0;

        if (hasTop) {
            double line1CenterY = startY + topHeight / 2.0;
            drawNumberNew(gc, header, centerX, line1CenterY, sp);
        }

        if (hasBottom) {
            double line2CenterY = startY + (hasTop ? topHeight + spacing : 0) + bottomHeight / 2.0;
            drawNumberOld(gc, header, centerX, line2CenterY, sp);
        }
    }

    private void drawNumberBox(GraphicsContext gc, HeaderLayout header, double x, double y, double width, double height, double sp) {
        double boxRadius = header.getNumBoxRadius() * sp;
        double strokeWidth = header.getNumBoxStrokeWidth() * sp;

        Color startColor  = Color.web("#dceafc");
        Color endColor    = Color.web("#a4c9fc");
        Color borderColor = Color.web("#76a3e3");

        LinearGradient gradient45deg = new LinearGradient(
                0, 0, 1, 1, true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, startColor),
                new Stop(1.0, endColor)
        );

        gc.setFill(gradient45deg);
        gc.fillRoundRect(x, y, width, height, boxRadius, boxRadius);

        gc.setStroke(borderColor);
        gc.setLineWidth(strokeWidth);
        gc.strokeRoundRect(x, y, width, height, boxRadius, boxRadius);
    }

    private void drawNumberNew(GraphicsContext gc, HeaderLayout header, double centerX, double centerY, double sp) {
        Text node = createNewNumberTextNode(header, sp);
        if (node.getText().isEmpty()) return;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(node.getFont());
        gc.setFill(Color.BLACK);
        gc.fillText(node.getText(), centerX, centerY);
    }

    private void drawNumberOld(GraphicsContext gc, HeaderLayout header, double centerX, double centerY, double sp) {
        Text node = createOldNumberTextNode(header, sp);
        if (node.getText().isEmpty()) return;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(node.getFont());
        gc.setFill(Color.BLACK);
        gc.fillText(node.getText(), centerX, centerY);
    }

    private Text createNewNumberTextNode(HeaderLayout header, double sp) {
        String newNum = header.getNumberNew() != null ? String.valueOf(header.getNumberNew()) : "";
        Font font = Font.font("Times New Roman", FontWeight.BOLD, header.getNumberNewFontSize() * sp);
        Text textNode = new Text(newNum);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private Text createOldNumberTextNode(HeaderLayout header, double sp) {
        String oldNum = header.getNumberOld() != null ? String.valueOf(header.getNumberOld()) : "";
        String formatted = oldNum.isEmpty() ? "" : "[" + oldNum + "]";
        Font font = Font.font("Times New Roman", FontWeight.NORMAL, header.getNumberOldFontSize() * sp);
        Text textNode = new Text(formatted);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private void drawTitle(GraphicsContext gc, HeaderLayout header, double x, double y, double sp) {
        String title = header.getTitle() != null ? header.getTitle() : "";
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("Times New Roman", FontWeight.BOLD, header.getTitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(title, x, y);
    }

    private void drawSubtitle(GraphicsContext gc, HeaderLayout header, double x, double y, double sp) {
        String subtitle = header.getSubtitle() != null ? header.getSubtitle() : "";
        double subtitleY = y + (header.getTitleFontSize() + 2.5) * sp;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("Times New Roman", FontWeight.NORMAL, FontPosture.ITALIC, header.getSubtitleFontSize() * sp));
        gc.fillText(subtitle, x, subtitleY);
    }

    private void drawComposer(GraphicsContext gc, HeaderLayout header, double x, double y, double sp) {
        String composer = header.getComposer() != null ? header.getComposer() : "";
        double composerY = y + (header.getHeight() - header.getComposerFontSize() - 1.5) * sp;
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(Font.font("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, header.getComposerFontSize() * sp));
        gc.fillText(composer, x, composerY);
    }
}