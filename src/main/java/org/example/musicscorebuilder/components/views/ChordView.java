package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.ChordLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;

public class ChordView extends ComponentView {
    private final NoteView noteView = new NoteView();

    public void draw(GraphicsContext gc, ChordLayout chord, double voiceX, double voiceY, double sp) {
        double chordX = voiceX + chord.getX() * sp;
        double chordY = voiceY + chord.getY() * sp;
        double widthPx = chord.getWidth() * sp;
        double heightPx = chord.getHeight() * sp;

//        fillBackground(gc, Color.LIME, chordX, chordY, widthPx, heightPx);

        for (NoteLayout note : chord.getNotes()) {
            noteView.draw(gc, note, chordX, voiceY, sp);
        }
    }
}