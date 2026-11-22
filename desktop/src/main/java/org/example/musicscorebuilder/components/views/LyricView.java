package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.views.util.LyricFontUtils;
import org.example.musicscorebuilder.managers.LyricEditorManager;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public class LyricView extends ComponentView {

    public void draw(GraphicsContext gc, NoteLayout noteLayout, double segmentX, double segmentY, double sp) {
        if (noteLayout == null) return;

        List<LyricLayout> lyrics = noteLayout.getLyrics();
        if (lyrics == null || lyrics.isEmpty()) return;

        LyricEditorManager editorManager = LyricEditorManager.getInstance();
        Integer selectedVerse = ScoreStateManager.getInstance().getSelectedVerseNumber();

        boolean isEditing = editorManager != null && editorManager.isEditing();
        int activeVerse = isEditing ? editorManager.getCurrentVerse() : (selectedVerse != null ? selectedVerse : -1);

        gc.save();

        try {
            for (LyricLayout lyricLayout : lyrics) {
                if (lyricLayout == null) continue;

                if (isEditing) {
                    if (lyricLayout.getVerse() != activeVerse) continue;
                } else if (selectedVerse != null && lyricLayout.getVerse() != selectedVerse) {
                    continue;
                }

                if (isEditing && editorManager.isEditingNote(noteLayout, lyricLayout.getVerse())) {
                    continue;
                }

                List<LyricLayout.FragmentLayout> fragments = lyricLayout.getFragmentLayouts();
                if (fragments == null || fragments.isEmpty()) continue;

                double lyricScreenY = segmentY + lyricLayout.getY() * sp;
                double startScreenX = segmentX + lyricLayout.getStartX() * sp;

                // 1. Rysowanie tekstów sylaby
                gc.setTextAlign(TextAlignment.LEFT);
                gc.setTextBaseline(VPos.TOP);

                for (LyricLayout.FragmentLayout frag : fragments) {
                    double fragX = startScreenX + frag.relativeX() * sp;

                    var font = LyricFontUtils.getFont(frag, sp);
                    gc.setFont(font);
                    gc.setFill(Color.BLACK);
                    gc.fillText(frag.fragment().getText(), fragX, lyricScreenY);

                    if (frag.fragment().isUnderline()) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(Math.max(1.0, sp * 0.1));
                        double underlineY = lyricScreenY + frag.underlineRelativeY() * sp;
                        gc.strokeLine(fragX, underlineY, fragX + frag.width() * sp, underlineY);
                    }
                }

                // 2. Rysowanie łączników / myślników (bez konieczności podawania ScoreLayout)
                List<LyricLayout.HyphenLayout> hyphens = lyricLayout.computeHyphenLayouts();
                if (hyphens != null && !hyphens.isEmpty()) {
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.setTextBaseline(VPos.CENTER);

                    double fontSize = lyricLayout.getFontSize();
                    double hyphenCenterY = lyricScreenY + (fontSize * sp * 0.4);

                    for (LyricLayout.HyphenLayout hyphen : hyphens) {
                        gc.save();
                        gc.translate(segmentX + hyphen.modelX() * sp, hyphenCenterY);
                        gc.scale(hyphen.scaleX(), 1.0);
                        gc.setFont(LyricFontUtils.getFont(hyphen, sp));
                        gc.setFill(Color.BLACK);
                        gc.fillText("–", 0, 0);
                        gc.restore();
                    }
                }
            }
        } finally {
            gc.restore();
        }
    }
}