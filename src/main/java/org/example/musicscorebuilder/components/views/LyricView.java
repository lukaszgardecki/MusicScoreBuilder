package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.managers.LyricEditorManager;

public class LyricView extends ComponentView {

    public void draw(GraphicsContext gc, NoteLayout noteLayout, double segmentX, double segmentY, double sp) {
        if (noteLayout == null || noteLayout.getLyrics().isEmpty()) return;

        LyricEditorManager editorManager = LyricEditorManager.getInstance();
        BackgroundView bgView = (gc.getCanvas().getParent() instanceof BackgroundView bg) ? bg : null;
        ScoreLayout scoreLayout = (bgView != null && bgView.getScoreView() != null)
                ? bgView.getScoreView().getScoreLayout() : null;

        gc.save();

        try {
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP);

            for (LyricLayout lyricLayout : noteLayout.getLyrics()) {
                if (editorManager != null && editorManager.isEditingNote(noteLayout, lyricLayout.getVerse())) {
                    continue;
                }

                double lyricScreenY = segmentY + lyricLayout.getModelY() * sp;
                double startScreenX = segmentX + lyricLayout.getStartX() * sp;

                for (LyricLayout.FragmentLayout frag : lyricLayout.getFragmentLayouts()) {
                    double fragX = startScreenX + frag.relativeX() * sp;

                    gc.setFont(frag.getFont(sp));
                    gc.setFill(Color.BLACK);
                    gc.fillText(frag.fragment().getText(), fragX, lyricScreenY);

                    if (frag.fragment().isUnderline()) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(Math.max(1.0, sp * 0.1));
                        double underlineY = lyricScreenY + frag.underlineRelativeY() * sp;
                        gc.strokeLine(fragX, underlineY, fragX + frag.width() * sp, underlineY);
                    }
                }

                LyricLayout.HyphenLayout hyphen = lyricLayout.computeHyphenLayout(scoreLayout);
                if (hyphen != null) {
                    gc.save();
                    gc.translate(segmentX + hyphen.modelX() * sp, segmentY + hyphen.modelY() * sp);
                    gc.scale(hyphen.scaleX(), 1.0);
                    gc.setFont(hyphen.getFont(sp));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("–", 0, 0);
                    gc.restore();
                }
            }
        } finally {
            gc.restore();
        }
    }
}