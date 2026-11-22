package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.SyllableType;

import java.util.ArrayList;
import java.util.List;

public final class LyricHyphenCalculator {

    private LyricHyphenCalculator() {}

    public static List<LyricLayout.HyphenLayout> calculateHyphens(LyricLayout lyricLayout, ScoreLayout scoreLayout) {
        List<LyricLayout.HyphenLayout> hyphens = new ArrayList<>(2);
        if (lyricLayout == null || lyricLayout.getLyric() == null) return hyphens;

        Lyric lyric = lyricLayout.getLyric();
        NoteLayout noteLayout = lyricLayout.getNoteLayout();
        if (noteLayout == null || noteLayout.getNote() == null) return hyphens;

        int verse = lyricLayout.getVerse();
        SyllableType type = lyric.getType();
        if (type == null || type == SyllableType.SINGLE) return hyphens;

        double fontSizeSp = lyricLayout.getFontSize();
        if (fontSizeSp <= 0) fontSizeSp = 12.0;

        double noteCenterX = lyricLayout.getNoteCenterX();
        double textWidth = lyricLayout.getTotalWidth();

        double startLeftX = (textWidth > 0) ? (noteCenterX - (textWidth / 2.0)) : noteCenterX;
        double startRightX = (textWidth > 0) ? (noteCenterX + (textWidth / 2.0)) : noteCenterX;

        // Odstęp od słowa dla myślnika na brzegach systemu
        double edgeOffset = Math.max(0.6, fontSizeSp * 0.5);

        // 1. POCZĄTEK NOWEGO SYSTEMU (Myślnik z lewej strony przed sylabą MIDDLE / END)
        if (type == SyllableType.MIDDLE || type == SyllableType.END) {
            NoteLayout prevLyricNote = findPreviousNoteWithLyric(noteLayout, verse);
            if (prevLyricNote != null) {
                Lyric prevLyric = prevLyricNote.getNote().getLyric(verse);
                if (prevLyric != null && (prevLyric.getType() == SyllableType.BEGIN || prevLyric.getType() == SyllableType.MIDDLE)) {
                    if (!isSameSystem(prevLyricNote, noteLayout)) {
                        double hyphenX = startLeftX - edgeOffset;
                        hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getY(), 1.0, fontSizeSp));
                    }
                }
            }
        }

        // 2. ŁĄCZENIE DO PRZODU (Myślniki za sylabą BEGIN / MIDDLE)
        if (type == SyllableType.BEGIN || type == SyllableType.MIDDLE) {
            NoteLayout nextLyricNote = findNextNoteWithLyric(noteLayout, verse);
            if (nextLyricNote != null) {
                Lyric nextLyric = nextLyricNote.getNote().getLyric(verse);
                if (nextLyric != null && (nextLyric.getType() == SyllableType.MIDDLE || nextLyric.getType() == SyllableType.END)) {

                    if (!isSameSystem(noteLayout, nextLyricNote)) {
                        double hyphenX = startRightX + edgeOffset;
                        hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getY(), 1.0, fontSizeSp));
                    } else {
                        // TA SAMA LINIA ($O(1)$ wyliczenie pozycji bez przeszukiwania drzewa)
                        double n1SystemX = getSystemRelativeX(noteLayout);
                        double n2SystemX = getSystemRelativeX(nextLyricNote);
                        double diffModelX = n2SystemX - n1SystemX;

                        LyricLayout targetLyricLayout = findLyricLayout(nextLyricNote, verse);
                        double targetTextWidth = 0.0;
                        double targetLocalNoteCenterX = nextLyricNote.getX();

                        if (targetLyricLayout != null) {
                            targetTextWidth = targetLyricLayout.getTotalWidth();
                            targetLocalNoteCenterX = targetLyricLayout.getNoteCenterX();
                        }

                        double targetNoteCenterX = diffModelX + targetLocalNoteCenterX;
                        double endLeftX = (targetTextWidth > 0) ? (targetNoteCenterX - (targetTextWidth / 2.0)) : targetNoteCenterX;

                        double gap = endLeftX - startRightX;

                        if (gap >= 2.0) {
                            double targetSpacing = Math.max(2.5, fontSizeSp * 1.5);
                            int numHyphens = (int) Math.floor(gap / targetSpacing);
                            numHyphens = Math.max(1, Math.min(15, numHyphens));

                            if (numHyphens == 1) {
                                double hyphenX = (startRightX + endLeftX) / 2.0;
                                hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getY(), 1.0, fontSizeSp));
                            } else {
                                double step = gap / (numHyphens + 1);
                                for (int i = 1; i <= numHyphens; i++) {
                                    double hyphenX = startRightX + (i * step);
                                    hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getY(), 1.0, fontSizeSp));
                                }
                            }
                        } else if (gap > 0.3) {
                            double hyphenX = (startRightX + endLeftX) / 2.0;
                            hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getY(), 1.0, fontSizeSp));
                        }
                    }
                }
            }
        }

        return hyphens;
    }

    // Bezpośrednie przechodzenie $O(1)$ do następnej nuty z tekstem w danym głosie
    private static NoteLayout findNextNoteWithLyric(NoteLayout start, int verse) {
        if (start == null) return null;
        NoteLayout curr = start.getNextNoteInVoice();
        while (curr != null) {
            if (curr.getNote() != null) {
                Lyric lyr = curr.getNote().getLyric(verse);
                if (lyr != null && !isNullOrBlank(lyr.getText())) {
                    return curr;
                }
            }
            curr = curr.getNextNoteInVoice();
        }
        return null;
    }

    // Bezpośrednie przechodzenie $O(1)$ do poprzedniej nuty z tekstem w danym głosie
    private static NoteLayout findPreviousNoteWithLyric(NoteLayout start, int verse) {
        if (start == null) return null;
        NoteLayout curr = start.getPrevNoteInVoice();
        while (curr != null) {
            if (curr.getNote() != null) {
                Lyric lyr = curr.getNote().getLyric(verse);
                if (lyr != null && !isNullOrBlank(lyr.getText())) {
                    return curr;
                }
            }
            curr = curr.getPrevNoteInVoice();
        }
        return null;
    }

    private static boolean isSameSystem(NoteLayout n1, NoteLayout n2) {
        if (n1 == null || n2 == null) return true;
        SystemLayout sys1 = getSystemLayout(n1);
        SystemLayout sys2 = getSystemLayout(n2);
        return sys1 != null && sys1 == sys2;
    }

    private static SystemLayout getSystemLayout(NoteLayout note) {
        if (note == null || note.getSegment() == null) return null;
        MeasureLayout measure = note.getSegment().getParent();
        if (measure == null) return null;
        return measure.getParent();
    }

    private static double getSystemRelativeX(NoteLayout note) {
        if (note == null || note.getSegment() == null) return 0.0;
        MeasureLayout measure = note.getSegment().getParent();
        if (measure == null) return 0.0;
        return measure.getX() + note.getSegment().getX();
    }

    private static LyricLayout findLyricLayout(NoteLayout noteLayout, int verse) {
        if (noteLayout == null) return null;
        List<LyricLayout> lyrics = noteLayout.getLyrics();
        if (lyrics == null) return null;
        int size = lyrics.size();
        for (int i = 0; i < size; i++) {
            LyricLayout l = lyrics.get(i);
            if (l.getVerse() == verse) {
                return l;
            }
        }
        return null;
    }

    private static boolean isNullOrBlank(String s) {
        if (s == null) return true;
        int len = s.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}