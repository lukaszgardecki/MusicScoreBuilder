package org.example.musicscorebuilder.components.views;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.SyllableType;
import org.example.musicscorebuilder.managers.FontManager;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.LyricEditorManager;

import java.util.List;

public class LyricView extends ComponentView {

    public void draw(GraphicsContext gc, NoteLayout noteLayout, double segmentX, double segmentY, double sp) {
        if (noteLayout == null || noteLayout.getNote() == null) return;

        var lyrics = noteLayout.getNote().getLyrics();
        if (lyrics == null || lyrics.isEmpty()) return;

        StaffLayout staff = noteLayout.getStaff();
        if (staff == null) return;

        gc.save();

        try {
            double staffBottomY = (staff.getY() + staff.getHeight()) * sp;
            double noteX = segmentX + noteLayout.getX() * sp;
            double noteCenterX = noteX + (noteLayout.getFontWidth() * sp) / 2.0;

            LyricEditorManager editorManager = LyricEditorManager.getInstance();
            BackgroundView bgView = null;

            if (gc.getCanvas().getParent() instanceof BackgroundView bg) {
                bgView = bg;
            }

            gc.setTextAlign(TextAlignment.LEFT);
            gc.setTextBaseline(VPos.TOP);

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

                Font baseFont = FontManager.getFreeSerifFont(fontSize);
                String fontFamily = baseFont.getFamily();

                List<LyricFragment> fragments = lyric.getFragments();
                if (fragments == null || fragments.isEmpty()) {
                    fragments = List.of(new LyricFragment(lyric.getText(), false, false, false));
                }

                double totalWidth = calculateLyricWidth(fragments, fontSize, fontFamily);
                double currentX = noteCenterX - (totalWidth / 2.0);

                for (LyricFragment frag : fragments) {
                    Font font = getFragmentFont(fontFamily, fontSize, frag);
                    gc.setFont(font);
                    gc.setFill(Color.BLACK);

                    gc.fillText(frag.getText(), currentX, lyricScreenY);

                    Text measureText = new Text(frag.getText());
                    measureText.setFont(font);
                    double fragWidth = measureText.getLayoutBounds().getWidth();

                    if (frag.isUnderline()) {
                        gc.setStroke(Color.BLACK);
                        gc.setLineWidth(Math.max(1.0, sp * 0.1));
                        double underlineY = lyricScreenY + measureText.getLayoutBounds().getHeight() * 0.8;
                        gc.strokeLine(currentX, underlineY, currentX + fragWidth, underlineY);
                    }

                    currentX += fragWidth;
                }

                if (lyric.getType() == SyllableType.BEGIN || lyric.getType() == SyllableType.MIDDLE) {
                    double currentTextWidthSp = totalWidth / sp;
                    drawDynamicHyphen(gc, bgView, noteLayout, verse, currentTextWidthSp, fontSizeSp, noteCenterX, lyricScreenY, sp);
                }
            }
        } finally {
            gc.restore();
        }
    }

    private void drawDynamicHyphen(
            GraphicsContext gc, BackgroundView bgView, NoteLayout currentNote,
            int verse, double currentTextWidthSp, double fontSizeSp,
            double noteCenterX, double lyricScreenY, double sp
    ) {
        NoteLayout nextNote = findNextNoteLayout(bgView, currentNote);
        double nextNoteCenterX;
        double nextTextWidthSp = 0;

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
                    double fontSize = fontSizeSp * sp;
                    Font baseFont = FontManager.getFreeSerifFont(fontSize);
                    List<LyricFragment> nextFrags = nextLyric.getFragments() != null ? nextLyric.getFragments() :
                            List.of(new LyricFragment(nextLyric.getText(), false, false, false));

                    nextTextWidthSp = calculateLyricWidth(nextFrags, fontSize, baseFont.getFamily()) / sp;
                }
            }
        } else {
            nextNoteCenterX = noteCenterX + 3.0 * sp;
        }

        double currentRightX = noteCenterX + (currentTextWidthSp * sp) / 2.0;
        double nextLeftX = nextNoteCenterX - (nextTextWidthSp * sp) / 2.0;

        double gap = nextLeftX - currentRightX;
        double margin = 0.15 * sp; // Margines odstępu od tekstu z lewej i prawej strony

        double availableForHyphen = gap - (2 * margin);

        Font baseFont = FontManager.getFreeSerifFont(fontSizeSp * sp);
        Text hyphenText = new Text("–");
        hyphenText.setFont(baseFont);
        double standardDashWidth = hyphenText.getLayoutBounds().getWidth();
        double minDashWidth = 0.25 * standardDashWidth;

        if (availableForHyphen >= minDashWidth && standardDashWidth > 0) {
            double targetWidth = Math.min(standardDashWidth, availableForHyphen);
            double scaleX = targetWidth / standardDashWidth;

            double hyphenX = (currentRightX + nextLeftX) / 2.0;

            gc.save();
            gc.translate(hyphenX, lyricScreenY);
            gc.scale(scaleX, 1.0);
            gc.setFont(baseFont);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("–", 0, 0);
            gc.restore();
        }
    }

    private double calculateLyricWidth(List<LyricFragment> fragments, double fontSize, String family) {
        double totalWidth = 0;
        for (LyricFragment frag : fragments) {
            Text t = new Text(frag.getText());
            t.setFont(getFragmentFont(family, fontSize, frag));
            totalWidth += t.getLayoutBounds().getWidth();
        }
        return totalWidth;
    }

    private Font getFragmentFont(String family, double size, LyricFragment fragment) {
        FontWeight weight = fragment.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
        FontPosture posture = fragment.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;
        return Font.font(family, weight, posture, size);
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