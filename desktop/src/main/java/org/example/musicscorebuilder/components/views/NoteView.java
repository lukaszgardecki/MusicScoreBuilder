package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.BeamSingleLayout;
import org.example.musicscorebuilder.components.layout.DotLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.managers.FontManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteView extends ComponentView {
    private static final Map<String, Color> COLOR_CACHE = new HashMap<>();
    private final LedgerLineView ledgerLineView = new LedgerLineView();
    private final AugmentationDotView dotView = new AugmentationDotView();
    private final StemView stemView = new StemView();
    private final BeamSingleView beamSingleView = new BeamSingleView();
    private final AccidentalView accidentalView = new AccidentalView();
    private final LyricView lyricView = new LyricView();

    public void draw(GraphicsContext gc, NoteLayout note, double segmentX, double segmentY, double sp) {
        double noteX = segmentX + note.getX() * sp;
        double noteY = segmentY + note.getY() * sp;
        double fontSize = note.getFontSize() * sp;

        List<NoteLayout.LedgerLine> ledgerLines = note.getLedgerLines();
        for (int i = 0; i < ledgerLines.size(); i++) {
            ledgerLineView.draw(gc, ledgerLines.get(i), segmentX, segmentY, sp);
        }

        List<DotLayout> dots = note.getDots();
        for (int i = 0; i < dots.size(); i++) {
            dotView.draw(gc, dots.get(i), noteX, noteY, sp);
        }

        stemView.draw(gc, note.getStem(), segmentX, segmentY, sp);
        if (note.getBeamSingle() instanceof BeamSingleLayout single) beamSingleView.draw(gc, single, segmentX, segmentY, sp);
        accidentalView.draw(gc, note.getAccidental(), noteX, noteY, sp);

        String colorStr = note instanceof GhostNoteLayout ghost
                ? ghost.getColor()
                : note.getScoreStyle().getSelectColor(note);
        gc.setFont(FontManager.getLelandFont(fontSize));
        gc.setFill(getCachedColor(colorStr));
        gc.fillText(note.getCode(), noteX, noteY);

        lyricView.draw(gc, note, segmentX, segmentY, sp);
    }

    private static Color getCachedColor(String hex) {
        if (hex == null) return Color.BLACK;
        return COLOR_CACHE.computeIfAbsent(hex, Color::web);
    }
}