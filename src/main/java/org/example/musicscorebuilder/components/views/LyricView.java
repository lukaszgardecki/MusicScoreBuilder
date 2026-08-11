package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.SyllableType;
import org.example.musicscorebuilder.managers.FontManager;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.LyricEditorManager;

public class LyricView extends ComponentView {

    public void draw(GraphicsContext gc, NoteLayout noteLayout, double segmentX, double segmentY, double sp) {
        if (noteLayout == null || noteLayout.getNote() == null) return;

        var lyrics = noteLayout.getNote().getLyrics();
        if (lyrics == null || lyrics.isEmpty()) return;

        StaffLayout staff = noteLayout.getStaff();
        if (staff == null) return;

        double staffBottomY = (staff.getY() + staff.getHeight()) * sp;
        double noteX = segmentX + noteLayout.getX() * sp;
        double noteCenterX = noteX + (noteLayout.getFontWidth() * sp) / 2.0;

        LyricEditorManager editorManager = LyricEditorManager.getInstance();
        BackgroundView bgView = null;

        if (gc.getCanvas().getParent() instanceof BackgroundView bg) {
            bgView = bg;
        }

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.TOP);
        gc.setFill(Color.BLACK);

        for (Lyric lyric : lyrics) {
            if (lyric == null || lyric.getText() == null || lyric.getText().trim().isEmpty()) continue;

            int verse = lyric.getVerse();

            if (editorManager != null && editorManager.isEditingNote(noteLayout, verse)) {
                continue;
            }

            double lyricModelY = staffBottomY + (sp * 2.5) + ((verse - 1) * sp * 1.5);
            double lyricScreenY = segmentY + lyricModelY;
            double fontSizeSp = lyric.getFontSize();
            double fontSize = fontSizeSp * sp;
            gc.setFont(FontManager.getFreeSerifFont(fontSize));

            String text = lyric.getText();
            gc.fillText(text, noteCenterX, lyricScreenY);

            if (lyric.getType() == SyllableType.BEGIN || lyric.getType() == SyllableType.MIDDLE) {
                drawDynamicHyphen(gc, bgView, noteLayout, verse, text, fontSizeSp, noteCenterX, lyricScreenY, sp);
            }
        }
    }

    private void drawDynamicHyphen(GraphicsContext gc, BackgroundView bgView, NoteLayout currentNote,
                                   int verse, String currentText, double fontSizeSp,
                                   double noteCenterX, double lyricScreenY, double sp) {

        NoteLayout nextNote = findNextNoteLayout(bgView, currentNote);
        double nextNoteCenterX;
        String nextLyricText = "";

        if (nextNote != null && bgView.getScoreView() != null) {
            ScoreLayout scoreLayout = bgView.getScoreView().getScoreLayout();
            LayoutHitTester.Point currentAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, currentNote, verse);
            LayoutHitTester.Point nextAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, nextNote, verse);

            double diffModelX = nextAbs.x() - currentAbs.x();
            if (diffModelX <= 0) return; // Brak miejsca lub następna nuta znajduje się z tyłu

            nextNoteCenterX = noteCenterX + diffModelX * sp;

            if (nextNote.getNote() != null) {
                Lyric nextLyric = nextNote.getNote().getLyric(verse);
                if (nextLyric != null && nextLyric.getText() != null) {
                    nextLyricText = nextLyric.getText().trim();
                }
            }
        } else {
            nextNoteCenterX = noteCenterX + 3.0 * sp;
        }

        double currentTextWidthSp = FontManager.getTextWidth(FontManager.FontType.FREE_SERIF, currentText, fontSizeSp);
        double nextTextWidthSp = nextLyricText.isEmpty() ? 0 : FontManager.getTextWidth(FontManager.FontType.FREE_SERIF, nextLyricText, fontSizeSp);

        double currentRightX = noteCenterX + (currentTextWidthSp * sp) / 2.0;
        double nextLeftX = nextNoteCenterX - (nextTextWidthSp * sp) / 2.0;

        double gap = nextLeftX - currentRightX;
        double margin = 0.15 * sp; // Margines odstępu od tekstu z lewej i prawej strony

        double availableForHyphen = gap - (2 * margin);
        double standardDashWidth = FontManager.getTextWidth(FontManager.FontType.FREE_SERIF, "–", fontSizeSp) * sp;
        double minDashWidth = 0.25 * standardDashWidth; // Progowa szerokość, poniżej której ukrywamy myślnik

        // Rysowanie z wyliczonym skrajnym lub przeskalowanym rozmiarem
        if (availableForHyphen >= minDashWidth && standardDashWidth > 0) {
            double targetWidth = Math.min(standardDashWidth, availableForHyphen);
            double scaleX = targetWidth / standardDashWidth;

            double hyphenX = (currentRightX + nextLeftX) / 2.0;

            gc.save();
            gc.translate(hyphenX, lyricScreenY);
            gc.scale(scaleX, 1.0);
            gc.fillText("–", 0, 0);
            gc.restore();
        }
    }

    private NoteLayout findNextNoteLayout(BackgroundView bgView, NoteLayout currentNote) {
        if (bgView != null && bgView.getScoreView() != null) {
            ScoreLayout scoreLayout = bgView.getScoreView().getScoreLayout();
            if (scoreLayout != null) {
                var allNotes = LayoutHitTester.getAllPositionedNotes(scoreLayout.getPages());
                for (int i = 0; i < allNotes.size(); i++) {
                    if (allNotes.get(i).noteLayout() == currentNote) {
                        if (i + 1 < allNotes.size()) {
                            return allNotes.get(i + 1).noteLayout();
                        }
                        break;
                    }
                }
            }
        }
        return null;
    }
}