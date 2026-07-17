package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.ChordLayout;
import org.example.musicscorebuilder.components.layout.VoiceLayout;

public class VoiceView extends ComponentView {
    private final ChordView chordView = new ChordView();

    public void draw(GraphicsContext gc, VoiceLayout voice, double segmentX, double segmentY, double sp) {
        double voiceX = segmentX + voice.getX() * sp;
        double voiceY = segmentY + voice.getY() * sp;
        double widthPx = voice.getWidth() * sp;
        double heightPx = voice.getHeight() * sp;

//        fillBackground(gc, Color.RED, voiceX, voiceY, widthPx, heightPx);

        for (ChordLayout chord : voice.getChords()) {
            chordView.draw(gc, chord, voiceX, segmentY, sp);
        }
    }
}