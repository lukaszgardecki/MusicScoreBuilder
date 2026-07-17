package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.*;

public class SegmentView extends ComponentView {
    private final BarlineView barlineView = new BarlineView();
    private final ClefView clefView = new ClefView();
    private final KeySigView keySigView = new KeySigView();
    private final TimeSigView timeSigView = new TimeSigView();
    private final VoiceView voiceView = new VoiceView();

    public void draw(GraphicsContext gc, SegmentLayout segment, double measureX, double measureY, double sp) {
        double segmentX = segment.getX() * sp + measureX;
        double segmentY = segment.getY() * sp + measureY;
        double widthPx = segment.getWidth() * sp;
        double heightPx = segment.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(0.3f), segmentX, segmentY, widthPx, heightPx);

        for (ElementLayout element : segment.getElements()) {
            gc.save();
            selectElement(gc, element);
            drawElement(gc, element, segmentX, segmentY, sp);
            gc.restore();
        }
    }

    private void selectElement(GraphicsContext gc, ElementLayout element) {
        if (element.isSelected()) {
            String color = element.getScoreStyle().getElementSelectedColor();
            gc.setFill(Color.web(color));
            gc.setStroke(Color.web(color));

//             Opcjonalnie: możesz narysować delikatne tło wokół elementu, żeby lepiej go widzieć
//                 gc.setGlobalAlpha(0.2);
//                 gc.fillRect(segmentX, segmentY, widthPx, heightPx);
//                 gc.setGlobalAlpha(1.0);
        } else {
            gc.setFill(Color.BLACK);
            gc.setStroke(Color.BLACK);
        }
    }

    private void drawElement(GraphicsContext gc, ElementLayout element, double segmentX, double segmentY, double sp) {
        switch(element) {
            case BarlineLayout barline -> barlineView.draw(gc, barline, segmentX, segmentY, sp);
            case ClefLayout clef -> clefView.draw(gc, clef, segmentX, segmentY, sp);
            case KeySigLayout keySig -> keySigView.draw(gc, keySig, segmentX, segmentY, sp);
            case TimeSigLayout timeSig -> timeSigView.draw(gc, timeSig, segmentX, segmentY, sp);
            case VoiceLayout voice -> voiceView.draw(gc, voice, segmentX, segmentY, sp);
            default -> {}
        }
    }
}
