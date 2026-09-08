package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.*;
import org.example.musicscorebuilder.components.frames.FrameLayout;
import org.example.musicscorebuilder.components.frames.HeaderFrameLayout;
import org.example.musicscorebuilder.components.frames.TextFrameLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;
import org.example.musicscorebuilder.components.music.frames.TextLine;
import org.example.musicscorebuilder.managers.ScoreStateManager;

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

        switch (frame) {
            case HeaderFrameLayout headerFrame -> drawHeaderFrame(gc, headerFrame, frameX, contentY, frameWidth, sp);
            case TextFrameLayout textFrame -> drawTextFrame(gc, textFrame, frameX, contentY, frameWidth, contentHeight, sp);
            default -> throw new IllegalStateException("Unexpected value: " + frame);
        }
    }

    private void drawTextFrame(GraphicsContext gc, TextFrameLayout frame, double frameX, double contentY, double frameWidth, double contentHeight, double sp) {
        if (frame == null || frame.getVerses() == null || frame.getVerses().isEmpty()) return;

        gc.save();
        gc.beginPath();
        gc.rect(frameX, contentY, frameWidth, contentHeight);
        gc.clip();

        gc.setTextBaseline(VPos.TOP);

        double defaultFontSizeSp = frame.getVerseFontSize();
        String fontFamily = "Times New Roman";
        double padding = frame.getPadding() * sp;
        double verseSpacing = frame.getVerseSpacing() * sp;

        double textPaddingX = frame.getVersePaddingX() * sp;
        double textPaddingY = frame.getVersePaddingY() * sp;
        int selectedVerseNum = ScoreStateManager.getInstance().getSelectedVerseNumber();
        double scrollY = 0.0;

        try {
            var method = frame.getClass().getMethod("getScrollY");
            Object val = method.invoke(frame);
            if (val instanceof Number num) {
                scrollY = num.doubleValue() * sp;
            }
        } catch (Exception ignored) {}

        double currentY = contentY + padding - scrollY;
        double totalContentHeight = padding * 2;

        for (TextFrameVerse verse : frame.getVerses()) {
            if (verse.getLines() == null) continue;

            double verseTextHeight = 0;
            for (TextLine line : verse.getLines()) {
                double lineFontSizeSp = line.getFontSize() != null ? line.getFontSize() : defaultFontSizeSp;
                verseTextHeight += (lineFontSizeSp * sp) * 1.3;
            }

            double totalVerseHeight = verseTextHeight + (2 * textPaddingY);


            if (currentY + totalVerseHeight >= contentY && currentY <= contentY + contentHeight) {

                // --- TŁO TYLKO DLA AKTUALNIE ZAZNACZONEJ ZWROTKI ---
                if (verse.getNumber() == selectedVerseNum) {
                    double bgX = frameX + padding;
                    double bgWidth = frameWidth - (padding * 2);
                    double cornerRadius = frame.getVerseCornerRadius() * sp;

                    gc.setFill(Color.web("#c8def8"));
                    gc.fillRoundRect(bgX, currentY, bgWidth, totalVerseHeight, cornerRadius, cornerRadius);
                }
                // ---------------------------------------------------

                // Tekst rysowany z przesunięciem pionowym wewnątrz tła
                double lineY = currentY + textPaddingY;

                for (TextLine line : verse.getLines()) {
                    if (line.getFragments() == null) continue;

                    // Tekst rysowany z przesunięciem poziomym wewnątrz tła
                    double currentX = frameX + padding + textPaddingX;
                    double lineFontSizeSp = line.getFontSize() != null ? line.getFontSize() : defaultFontSizeSp;
                    double baseFontSize = lineFontSizeSp * sp;
                    double lineHeight = baseFontSize * 1.3;

                    for (LyricFragment fragment : line.getFragments()) {
                        String text = fragment.getText();
                        if (text == null || text.isEmpty()) continue;

                        FontWeight weight = fragment.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
                        FontPosture posture = fragment.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;

                        Font font = getFont(fontFamily, weight, posture, baseFontSize);
                        gc.setFont(font);
                        gc.setFill(Color.BLACK);
                        gc.fillText(text, currentX, lineY);

                        double textWidth = computeTextWidth(text, font);

                        if (fragment.isUnderline()) {
                            gc.setStroke(Color.BLACK);
                            gc.setLineWidth(1.0 * sp);
                            double lineStrokeY = lineY + baseFontSize * 0.95;
                            gc.strokeLine(currentX, lineStrokeY, currentX + textWidth, lineStrokeY);
                        }

                        currentX += textWidth;
                    }

                    lineY += lineHeight;
                }
            }

            // Przesunięcie pozycji rysowania o pełną wysokość bloku oraz odstęp między zwrotkami
            currentY += totalVerseHeight + verseSpacing;
            totalContentHeight += totalVerseHeight + verseSpacing;
        }

        // 3. Rysowanie wskaźnika przewijania (Scrollbar Canvas)
        if (totalContentHeight > contentHeight) {
            double scrollbarWidth = 0.3 * sp;
            double scrollbarX = frameX + frameWidth - scrollbarWidth - (0.1 * sp);

            gc.setFill(Color.rgb(210, 210, 210, 0.5));
            gc.fillRoundRect(scrollbarX, contentY, scrollbarWidth, contentHeight, scrollbarWidth, scrollbarWidth);

            double visibleRatio = contentHeight / totalContentHeight;
            double thumbHeight = Math.max(1.2 * sp, contentHeight * visibleRatio);
            double maxScroll = totalContentHeight - contentHeight;
            double thumbY = contentY + (scrollY / maxScroll) * (contentHeight - thumbHeight);

            gc.setFill(Color.rgb(100, 100, 100, 0.8));
            gc.fillRoundRect(scrollbarX, thumbY, scrollbarWidth, thumbHeight, scrollbarWidth, scrollbarWidth);
        }

        gc.restore();
    }

    private double computeTextWidth(String text, Font font) {
        if (text == null || text.isEmpty()) return 0;
        Text textNode = new Text(text);
        textNode.setFont(font);
        return textNode.getLayoutBounds().getWidth();
    }

    private void drawHeaderFrame(GraphicsContext gc, HeaderFrameLayout frame, double frameX, double contentY, double frameWidth, double sp) {
        gc.save();
        double centerX = frameX + (frameWidth / 2.0);
        double rightX = frameX + frameWidth;

        drawNumber(gc, frame, frameX, contentY, sp);
        drawTitle(gc, frame, centerX, contentY, sp);
        drawSubtitle(gc, frame, centerX, contentY, sp);
        drawComposer(gc, frame, rightX, contentY, sp);
        gc.restore();
    }

    private void drawNumber(GraphicsContext gc, HeaderFrameLayout frame, double x, double y, double sp) {
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

    private void drawNumberBox(GraphicsContext gc, HeaderFrameLayout frame, double x, double y, double width, double height, double sp) {
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

    private Text createNewNumberTextNode(HeaderFrameLayout frame, double sp) {
        String newNum = frame.getNumberNew() != null ? String.valueOf(frame.getNumberNew()) : "";
        Font font = getFont("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, frame.getNumberNewFontSize() * sp);
        Text textNode = new Text(newNum);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private Text createOldNumberTextNode(HeaderFrameLayout frame, double sp) {
        String oldNum = frame.getNumberOld() != null ? String.valueOf(frame.getNumberOld()) : "";
        String formatted = oldNum.isEmpty() ? "" : "[" + oldNum + "]";
        Font font = getFont("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, frame.getNumberOldFontSize() * sp);
        Text textNode = new Text(formatted);
        textNode.setFont(font);
        textNode.setBoundsType(TextBoundsType.VISUAL);
        return textNode;
    }

    private void drawTitle(GraphicsContext gc, HeaderFrameLayout frame, double x, double y, double sp) {
        String title = frame.getTitle() != null ? frame.getTitle() : "";
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.BOLD, FontPosture.REGULAR, frame.getTitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(title, x, y);
    }

    private void drawSubtitle(GraphicsContext gc, HeaderFrameLayout frame, double x, double y, double sp) {
        String subtitle = frame.getSubtitle() != null ? frame.getSubtitle() : "";
        double subtitleY = y + (frame.getTitleFontSize() + 2.5) * sp;
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.NORMAL, FontPosture.ITALIC, frame.getSubtitleFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(subtitle, x, subtitleY);
    }

    private void drawComposer(GraphicsContext gc, HeaderFrameLayout frame, double x, double y, double sp) {
        String composer = frame.getComposer() != null ? frame.getComposer() : "";
        double composerY = y + (frame.getContentHeight() - frame.getComposerFontSize()) * sp;
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setTextBaseline(VPos.TOP);
        gc.setFont(getFont("Times New Roman", FontWeight.NORMAL, FontPosture.REGULAR, frame.getComposerFontSize() * sp));
        gc.setFill(Color.BLACK);
        gc.fillText(composer, x, composerY);
    }
}