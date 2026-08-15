package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.managers.LyricEditorManager;

import java.util.List;

public class LyricView extends ComponentView {

    public void draw(GraphicsContext gc, NoteLayout noteLayout, double segmentX, double segmentY, double sp) {
        if (noteLayout == null) return;

        List<LyricLayout> lyrics = noteLayout.getLyrics();
        if (lyrics.isEmpty()) return;

        LyricEditorManager editorManager = LyricEditorManager.getInstance();

        ScoreLayout scoreLayout = null;
        Parent p = gc.getCanvas().getParent();
        while (p != null) {
            if (p instanceof BackgroundView bg && bg.getScoreView() != null) {
                scoreLayout = bg.getScoreView().getScoreLayout();
                break;
            }
            p = p.getParent();
        }

        gc.save();

        try {
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP);

            for (LyricLayout lyricLayout : lyrics) {
                if (editorManager != null && editorManager.isEditingNote(noteLayout, lyricLayout.getVerse())) {
                    continue;
                }

                lyricLayout.checkAndRefreshIfStale();
                if (lyricLayout.getFragmentLayouts().isEmpty()) continue;

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

                for (LyricLayout.HyphenLayout hyphen : lyricLayout.computeHyphenLayouts(scoreLayout)) {
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