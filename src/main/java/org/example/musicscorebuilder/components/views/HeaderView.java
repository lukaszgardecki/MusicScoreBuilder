package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.*;
import org.example.musicscorebuilder.components.layout.FrameLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.HashMap;
import java.util.Map;

public class HeaderView extends ComponentView {

    private static final Map<String, Color> COLOR_CACHE = new HashMap<>();
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    private static final Color START_COLOR  = Color.web("#dceafc");
    private static final Color END_COLOR    = Color.web("#a4c9fc");
    private static final Color BORDER_COLOR = Color.web("#76a3e3");

    private static final LinearGradient BOX_GRADIENT = new LinearGradient(
            0, 0, 1, 1, true,
            CycleMethod.NO_CYCLE,
            new Stop(0.0, START_COLOR),
            new Stop(1.0, END_COLOR)
    );

    private static Color getCachedColor(String hex) {
        if (hex == null) return Color.BLACK;
        return COLOR_CACHE.computeIfAbsent(hex, Color::web);
    }

    private static Font getFont(String family, FontWeight weight, FontPosture posture, double size) {
        double roundedSize = Math.round(size * 10.0) / 10.0;
        String key = family + "_" + weight + "_" + posture + "_" + roundedSize;
        return FONT_CACHE.computeIfAbsent(key, k -> Font.font(family, weight, posture, roundedSize));
    }

    public void draw(GraphicsContext gc, FrameLayout header, double pageX, double pageY, double sp) {
        ScoreStyle style = header.getScoreStyle();
        double headerX = pageX + header.getX() * sp;
        double contentY = pageY + header.getContentY() * sp;
        double headerWidth = header.getWidth() * sp;
        double contentHeight = header.getContentHeight() * sp;
        gc.save();
        gc.setStroke(getCachedColor(header.isSelected() ? style.getSelectColor(header) : style.getFrameStrokeColor()));
        gc.setLineWidth(style.getFrameStrokeThickness() * sp);
        gc.setLineDashes(style.getFrameStrokeDashLength() * sp, style.getFrameStrokeSpaceLength() * sp);
        gc.strokeRect(headerX, contentY, headerWidth, contentHeight);
        gc.restore();

        gc.save();
        double centerX = headerX + (headerWidth / 2.0);
        double rightX = headerX + headerWidth;

        drawNumber(gc, header, headerX, contentY, sp);
        drawTitle(gc, header, centerX, contentY, sp);
        drawSubtitle(gc, header, centerX, contentY, sp);
        drawComposer(gc, header, rightX, contentY, sp);
        gc.restore();
    }

    private void drawNumber(GraphicsContext gc, FrameLayout header, double x, double y, double sp) {
        Text topNode = createNewNumberTextNode(header, sp);
        Text bottomNode = createOldNumberTextNode(header, sp);
        if (topNode.getText().isBlank() && bottomNode.getText().isBlank()) return;

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
            drawNumberNode(gc, topNode, centerX, line1CenterY);
        }

        if (hasBottom) {
            double line2CenterY = startY + (hasTop ? topHeight + spacing : 0) + bottomHeight / 2.0;
            drawNumberNode(gc, bottomNode, centerX, line2CenterY);
        }
    }

    private void drawNumberBox(GraphicsContext gc, FrameLayout header, double x, double y, double width, double height, double sp) {
        double boxRadius = header.getNumBoxRadius() * sp;
        double strokeWidth = header.getNumBoxStrokeWidth() * sp;

        gc.setFill(BOX_GRADIENT);
        gc.fillRoundRect(x, y, width, height, boxRadius, boxRadius);

        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(strokeWidth);
        gc.strokeRoundRect(x, y, width, height, boxRadius, boxRadius);
    }

    private void drawNumberNode(GraphicsContext gc, Text node, double centerX, double centerY) {
        if (node.getText().isEmpty()) return;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(node.getFont());
        gc.setFill(Color.BLACK);
        gc.fillText(node.getText(), centerX, centerY);
    }

    private Text createNewNumberTextNode(FrameLayout header, double sp) {
        String newNum = header.getNumberNew() != null ? String.valueOf(header.getNumberNew()) : "";
        Font font = getFont("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, header.getNumberNewFontSize() * sp);
        Text textNode = new Text(newNum);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private Text createOldNumberTextNode(FrameLayout header, double sp) {
        String oldNum = header.getNumberOld() != null ? String.valueOf(header.getNumberOld()) : "";
        String formatted = oldNum.isEmpty() ? "" : "[" + oldNum + "]";
        Font font = getFont("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, header.getNumberOldFontSize() * sp);
        Text textNode = new Text(formatted);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private void drawTitle(GraphicsContext gc, FrameLayout header, double x, double y, double sp) {
        String title = header.getTitle() != null ? header.getTitle() : "";
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, header.getTitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(title, x, y);
    }

    private void drawSubtitle(GraphicsContext gc, FrameLayout header, double x, double y, double sp) {
        String subtitle = header.getSubtitle() != null ? header.getSubtitle() : "";
        double subtitleY = y + (header.getTitleFontSize() + 2.5) * sp;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.NORMAL, FontPosture.ITALIC, header.getSubtitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(subtitle, x, subtitleY);
    }

    private void drawComposer(GraphicsContext gc, FrameLayout header, double x, double y, double sp) {
        String composer = header.getComposer() != null ? header.getComposer() : "";
        double composerY = y + (header.getContentHeight() - header.getComposerFontSize()) * sp;
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, header.getComposerFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(composer, x, composerY);
    }
}