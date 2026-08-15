package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.ElementLayout;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.PageLayout;
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
        if (lyricLayout == null || lyricLayout.getLyric() == null || scoreLayout == null) return hyphens;

        Lyric lyric = lyricLayout.getLyric();
        NoteLayout noteLayout = lyricLayout.getNoteLayout();
        if (noteLayout == null || noteLayout.getNote() == null) return hyphens;

        int verse = lyricLayout.getVerse();
        SyllableType type = lyric.getType();
        if (type == null || type == SyllableType.SINGLE) return hyphens;

        double fontSizeSp = lyricLayout.getFontSize();
        double noteCenterX = lyricLayout.getNoteCenterX();
        double textWidth = lyricLayout.getTotalWidth();

        double startLeftX = (textWidth > 0) ? (noteCenterX - (textWidth / 2.0)) : noteCenterX;
        double startRightX = (textWidth > 0) ? (noteCenterX + (textWidth / 2.0)) : noteCenterX;

        // Odstęp od słowa dla myślnika na brzegach systemu (ok. 0.6 spacji)
        double edgeOffset = Math.max(0.6, fontSizeSp * 0.5);

        // KROK KRYTYCZNY: Znajdujemy instancję nuty bezpośrednio w przekazanym drzewie na podstawie niezmiennego modelu!
        // Zapobiega to błędom związanym z odświeżaniem węzłów układu na nowych stronach/systemach.
        NoteLayout currentInTree = findCurrentNoteInTree(scoreLayout, noteLayout);

        // 1. POCZĄTEK NOWEGO SYSTEMU (Myślnik z lewej strony przed sylabą MIDDLE / END)
        if (type == SyllableType.MIDDLE || type == SyllableType.END) {
            NoteLayout prevLyricNote = findPreviousNoteWithLyric(scoreLayout, currentInTree, verse);
            if (prevLyricNote != null) {
                Lyric prevLyric = prevLyricNote.getNote().getLyric(verse);
                if (prevLyric != null && (prevLyric.getType() == SyllableType.BEGIN || prevLyric.getType() == SyllableType.MIDDLE)) {
                    // Jeśli poprzednia sylaba była w INNYM systemie
                    if (!isSameSystem(prevLyricNote, currentInTree)) {
                        double hyphenX = startLeftX - edgeOffset;
                        hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, fontSizeSp));
                    }
                }
            }
        }

        // 2. ŁĄCZENIE DO PRZODU (Myślniki za sylabą BEGIN / MIDDLE)
        if (type == SyllableType.BEGIN || type == SyllableType.MIDDLE) {
            NoteLayout nextLyricNote = findNextNoteWithLyric(scoreLayout, currentInTree, verse);
            if (nextLyricNote != null) {
                Lyric nextLyric = nextLyricNote.getNote().getLyric(verse);
                if (nextLyric != null && (nextLyric.getType() == SyllableType.MIDDLE || nextLyric.getType() == SyllableType.END)) {

                    // Jeśli kolejna sylaba będzie w INNYM systemie
                    if (!isSameSystem(currentInTree, nextLyricNote)) {
                        double hyphenX = startRightX + edgeOffset;
                        hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, fontSizeSp));
                    } else {
                        // TA SAMA LINIA (Wyliczamy pełen dystans, automatycznie przeskakując przez melizmaty)
                        double n1SystemX = getSystemRelativeX(currentInTree);
                        double n2SystemX = getSystemRelativeX(nextLyricNote);
                        double diffModelX = n2SystemX - n1SystemX;

                        LyricLayout targetLyricLayout = findLyricLayout(nextLyricNote, verse);
                        double targetTextWidth = 0.0;
                        double targetLocalNoteCenterX = nextLyricNote.getX();

                        if (targetLyricLayout != null) {
                            targetLyricLayout.checkAndRefreshIfStale();
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
                                hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, fontSizeSp));
                            } else {
                                double step = gap / (numHyphens + 1);
                                for (int i = 1; i <= numHyphens; i++) {
                                    double hyphenX = startRightX + (i * step);
                                    hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, fontSizeSp));
                                }
                            }
                        } else if (gap > 0.3) {
                            double hyphenX = (startRightX + endLeftX) / 2.0;
                            hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, fontSizeSp));
                        }
                    }
                }
            }
        }

        return hyphens;
    }

    // Odnajduje fizyczną instancję w bieżącym drzewie bazując na stałym obiekcie muzycznym
    private static NoteLayout findCurrentNoteInTree(ScoreLayout score, NoteLayout current) {
        if (score == null || current == null || current.getNote() == null) return current;
        int targetVoice = current.getNote().getVoice();
        int targetStaff = current.getStaff() != null ? current.getStaff().getStaffIndex() : 0;

        for (PageLayout page : score.getPages()) {
            if (page.getSystems() == null) continue;
            for (SystemLayout system : page.getSystems()) {
                if (system.getMeasures() == null) continue;
                for (MeasureLayout measure : system.getMeasures()) {
                    if (measure.getSegments() == null) continue;
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout el : segment.getElements()) {
                            if (el instanceof NoteLayout nl && nl.getNote() != null) {
                                int staffIdx = nl.getStaff() != null ? nl.getStaff().getStaffIndex() : 0;
                                if (staffIdx == targetStaff && nl.getNote().getVoice() == targetVoice) {
                                    // Zrównanie z modelem
                                    if (nl.getNote() == current.getNote()) {
                                        return nl;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return current;
    }

    // Bezpośrednie wyszukiwanie *następnej nuty z tekstem*
    private static NoteLayout findNextNoteWithLyric(ScoreLayout score, NoteLayout currentInTree, int verse) {
        if (score == null || currentInTree == null || currentInTree.getNote() == null) return null;
        int targetVoice = currentInTree.getNote().getVoice();
        int targetStaff = currentInTree.getStaff() != null ? currentInTree.getStaff().getStaffIndex() : 0;
        boolean foundCurrent = false;

        for (PageLayout page : score.getPages()) {
            if (page.getSystems() == null) continue;
            for (SystemLayout system : page.getSystems()) {
                if (system.getMeasures() == null) continue;
                for (MeasureLayout measure : system.getMeasures()) {
                    if (measure.getSegments() == null) continue;
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout el : segment.getElements()) {
                            if (el instanceof NoteLayout nl && nl.getNote() != null) {
                                int staffIdx = nl.getStaff() != null ? nl.getStaff().getStaffIndex() : 0;
                                if (staffIdx == targetStaff && nl.getNote().getVoice() == targetVoice) {
                                    if (foundCurrent) {
                                        Lyric lyr = nl.getNote().getLyric(verse);
                                        if (lyr != null && !isNullOrBlank(lyr.getText())) {
                                            return nl;
                                        }
                                    } else if (nl == currentInTree) { // Dzięki findCurrentNoteInTree to porównanie nigdy nie chybia!
                                        foundCurrent = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    // Bezpośrednie wyszukiwanie *poprzedniej nuty z tekstem*
    private static NoteLayout findPreviousNoteWithLyric(ScoreLayout score, NoteLayout currentInTree, int verse) {
        if (score == null || currentInTree == null || currentInTree.getNote() == null) return null;
        int targetVoice = currentInTree.getNote().getVoice();
        int targetStaff = currentInTree.getStaff() != null ? currentInTree.getStaff().getStaffIndex() : 0;
        NoteLayout previousWithLyric = null;

        for (PageLayout page : score.getPages()) {
            if (page.getSystems() == null) continue;
            for (SystemLayout system : page.getSystems()) {
                if (system.getMeasures() == null) continue;
                for (MeasureLayout measure : system.getMeasures()) {
                    if (measure.getSegments() == null) continue;
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout el : segment.getElements()) {
                            if (el instanceof NoteLayout nl && nl.getNote() != null) {
                                int staffIdx = nl.getStaff() != null ? nl.getStaff().getStaffIndex() : 0;
                                if (staffIdx == targetStaff && nl.getNote().getVoice() == targetVoice) {
                                    if (nl == currentInTree) {
                                        return previousWithLyric;
                                    }
                                    Lyric lyr = nl.getNote().getLyric(verse);
                                    if (lyr != null && !isNullOrBlank(lyr.getText())) {
                                        previousWithLyric = nl;
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
        if (note == null || note.getSegment() == null || note.getSegment().getParent() == null) return 0.0;
        MeasureLayout measure = note.getSegment().getParent();
        SegmentLayout segment = note.getSegment();
        return measure.getX() + segment.getX();
    }

    private static LyricLayout findLyricLayout(NoteLayout noteLayout, int verse) {
        if (noteLayout == null) return null;
        List<LyricLayout> lyrics = noteLayout.getLyrics();
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