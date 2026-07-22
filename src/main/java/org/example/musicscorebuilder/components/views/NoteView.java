package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.FontManager;
import org.example.musicscorebuilder.components.layout.BeamSingleLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;

public class NoteView extends ComponentView {
    private final LedgerLineView ledgerLineView = new LedgerLineView();
    private final StemView stemView = new StemView();
    private final BeamSingleView beamSingleView = new BeamSingleView();

    public void draw(GraphicsContext gc, NoteLayout note, double segmentX, double segmentY, double sp) {
        double noteX = segmentX + note.getX() * sp;
        double noteY = segmentY + note.getY() * sp;
        double boxX = segmentX + note.getBoxX() * sp;
        double boxY = segmentY + note.getBoxY() * sp;
        double widthPx = note.getBoxWidth() * sp;
        double heightPx = note.getHeight() * sp;
        double fontSize = note.getFontSize() * sp;

//        fillBackground(gc, Util.generateRandomColor(), boxX, boxY, widthPx, heightPx);

        for (NoteLayout.LedgerLine ledgerLine : note.getLedgerLines()) {
            ledgerLineView.draw(gc, ledgerLine, segmentX, segmentY, sp);
        }

        stemView.draw(gc, note.getStem(), segmentX, segmentY, sp);

        if (note.getBeamSingle() instanceof BeamSingleLayout single) beamSingleView.draw(gc, single, segmentX, segmentY, sp);

        gc.setFont(FontManager.getLelandFont(fontSize));

        if (note.isSelected()) gc.setFill(Color.web(note.getScoreStyle().getElementSelectedColor()));
        else gc.setFill(Color.BLACK);

        gc.fillText(note.getCode(), noteX, noteY);
    }
}
