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

public class FrameView extends ComponentView {

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

    public void draw(GraphicsContext gc, FrameLayout frame, double pageX, double pageY, double sp) {
        ScoreStyle style = frame.getScoreStyle();
        double frameX = pageX + frame.getX() * sp;
        double contentY = pageY + frame.getContentY() * sp;
        double frameWidth = frame.getWidth() * sp;
        double contentHeight = frame.getContentHeight() * sp;
        gc.save();
        gc.setStroke(getCachedColor(frame.isSelected() ? style.getSelectColor(frame) : style.getFrameStrokeColor()));
        gc.setLineWidth(style.getFrameStrokeThickness() * sp);
        gc.setLineDashes(style.getFrameStrokeDashLength() * sp, style.getFrameStrokeSpaceLength() * sp);
        gc.strokeRect(frameX, contentY, frameWidth, contentHeight);
        gc.restore();

        gc.save();
        double centerX = frameX + (frameWidth / 2.0);
        double rightX = frameX + frameWidth;

        drawNumber(gc, frame, frameX, contentY, sp);
        drawTitle(gc, frame, centerX, contentY, sp);
        drawSubtitle(gc, frame, centerX, contentY, sp);
        drawComposer(gc, frame, rightX, contentY, sp);
        gc.restore();
    }

    private void drawNumber(GraphicsContext gc, FrameLayout frame, double x, double y, double sp) {
        Text topNode = createNewNumberTextNode(frame, sp);
        Text bottomNode = createOldNumberTextNode(frame, sp);
        if (topNode.getText().isBlank() && bottomNode.getText().isBlank()) return;

        boolean hasTop = !topNode.getText().isEmpty();
        boolean hasBottom = !bottomNode.getText().isEmpty();

        double topWidth = hasTop ? topNode.getLayoutBounds().getWidth() : 0;
        double topHeight = hasTop ? topNode.getLayoutBounds().getHeight() : 0;

        double bottomWidth = hasBottom ? bottomNode.getLayoutBounds().getWidth() : 0;
        double bottomHeight = hasBottom ? bottomNode.getLayoutBounds().getHeight() : 0;

        double spacing = frame.getNumBoxSpacing() * sp;
        double paddingX = frame.getNumBoxPaddingX() * sp;
        double paddingY = frame.getNumBoxPaddingY() * sp;

        double contentWidth = Math.max(topWidth, bottomWidth);
        double contentHeight = 0;
        if (hasTop) contentHeight += topHeight;
        if (hasBottom) {
            if (hasTop) contentHeight += spacing;
            contentHeight += bottomHeight;
        }

        double rectWidth = Math.max(frame.getNumBoxMinWidth() * sp, contentWidth + 2 * paddingX);
        double rectHeight = Math.max(frame.getNumBoxMinHeight() * sp, contentHeight + 2 * paddingY);

        drawNumberBox(gc, frame, x, y, rectWidth, rectHeight, sp);

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

    private void drawNumberBox(GraphicsContext gc, FrameLayout frame, double x, double y, double width, double height, double sp) {
        double boxRadius = frame.getNumBoxRadius() * sp;
        double strokeWidth = frame.getNumBoxStrokeWidth() * sp;

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

    private Text createNewNumberTextNode(FrameLayout frame, double sp) {
        String newNum = frame.getNumberNew() != null ? String.valueOf(frame.getNumberNew()) : "";
        Font font = getFont("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, frame.getNumberNewFontSize() * sp);
        Text textNode = new Text(newNum);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private Text createOldNumberTextNode(FrameLayout frame, double sp) {
        String oldNum = frame.getNumberOld() != null ? String.valueOf(frame.getNumberOld()) : "";
        String formatted = oldNum.isEmpty() ? "" : "[" + oldNum + "]";
        Font font = getFont("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, frame.getNumberOldFontSize() * sp);
        Text textNode = new Text(formatted);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private void drawTitle(GraphicsContext gc, FrameLayout frame, double x, double y, double sp) {
        String title = frame.getTitle() != null ? frame.getTitle() : "";
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, frame.getTitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(title, x, y);
    }

    private void drawSubtitle(GraphicsContext gc, FrameLayout frame, double x, double y, double sp) {
        String subtitle = frame.getSubtitle() != null ? frame.getSubtitle() : "";
        double subtitleY = y + (frame.getTitleFontSize() + 2.5) * sp;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.NORMAL, FontPosture.ITALIC, frame.getSubtitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(subtitle, x, subtitleY);
    }

    private void drawComposer(GraphicsContext gc, FrameLayout frame, double x, double y, double sp) {
        String composer = frame.getComposer() != null ? frame.getComposer() : "";
        double composerY = y + (frame.getContentHeight() - frame.getComposerFontSize()) * sp;
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, frame.getComposerFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(composer, x, composerY);
    }
}