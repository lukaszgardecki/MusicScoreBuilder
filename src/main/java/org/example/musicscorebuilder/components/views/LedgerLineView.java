package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.NoteLayout;

public class LedgerLineView {

    public void draw(GraphicsContext gc, NoteLayout.LedgerLine ledgerLine, double chordX, double chordY, double sp) {
        double lineStartX = chordX + ledgerLine.startX() * sp;
        double lineEndX = chordX + ledgerLine.endX() * sp;
        double lineY = chordY + ledgerLine.y() * sp;
        gc.setLineWidth(ledgerLine.thickness() * sp);
        gc.strokeLine(lineStartX, lineY, lineEndX, lineY);
    }
}