package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.util.Util;

public class NoteView extends ComponentView {

    public void draw(GraphicsContext gc, NoteLayout note, double chordX, double chordY, double sp) {
        double noteX = chordX + note.getX() * sp;
        double noteY = chordY + note.getY() * sp;
        double boxY = chordY + note.getBoxY() * sp;
        double widthPx = note.getWidth() * sp;
        double heightPx = note.getHeight() * sp;
        double fontSize = note.getFontSize() * sp;

//        fillBackground(gc, Util.generateRandomColor(), noteX, boxY, widthPx, heightPx);

        gc.setFont(FontManager.getLelandFont(fontSize));
        gc.setFill(Color.BLACK);
        gc.fillText(note.getCode(), noteX, noteY);
    }
}
