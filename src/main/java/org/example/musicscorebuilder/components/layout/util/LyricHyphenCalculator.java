package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.ElementLayout;
import org.example.musicscorebuilder.components.layout.LyricLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.SyllableType;
import org.example.musicscorebuilder.managers.LayoutHitTester;

import java.util.ArrayList;
import java.util.List;

public final class LyricHyphenCalculator {

    private LyricHyphenCalculator() {}

    public static List<LyricLayout.HyphenLayout> calculateHyphens(LyricLayout lyricLayout, ScoreLayout scoreLayout) {
        List<LyricLayout.HyphenLayout> hyphens = new ArrayList<>();
        if (lyricLayout == null || lyricLayout.getLyric() == null) return hyphens;

        Lyric lyric = lyricLayout.getLyric();
        NoteLayout noteLayout = lyricLayout.getNoteLayout();
        if (noteLayout == null) return hyphens;

        int verse = lyricLayout.getVerse();
        SyllableType type = lyric.getType();
        boolean hasText = lyric.getText() != null && !lyric.getText().trim().isEmpty();

        double noteCenterX = lyricLayout.getNoteCenterX();

        if (type == SyllableType.MIDDLE || type == SyllableType.END) {
            if (isPreviousLyricConnectedFromPreviousSystem(lyricLayout, scoreLayout)) {
                double textWidth = lyricLayout.getTotalWidth();
                double startLeftX = (textWidth > 0) ? (noteCenterX - (textWidth / 2.0)) : noteCenterX;
                double hyphenX = startLeftX - 6.0;
                hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, lyricLayout.getFontSize()));
            }
        }

        boolean isConnected = (type == SyllableType.BEGIN || type == SyllableType.MIDDLE);
        if (!isConnected) return hyphens;

        NoteLayout prevNote = findPreviousNoteLayout(noteLayout);
        boolean isStartOfSystemSegment = false;

        if (hasText) {
            isStartOfSystemSegment = true;
        } else if (prevNote != null && prevNote.getNote() != null) {
            Lyric prevLyric = prevNote.getNote().getLyric(verse);
            if (prevLyric != null && (prevLyric.getType() == SyllableType.BEGIN || prevLyric.getType() == SyllableType.MIDDLE)) {
                LayoutHitTester.Point currentAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, noteLayout, verse);
                LayoutHitTester.Point prevAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, prevNote, verse);

                if (!isSameSystem(prevAbs, currentAbs)) {
                    isStartOfSystemSegment = true;
                }
            }
        }

        if (!isStartOfSystemSegment) {
            return hyphens;
        }

        NoteLayout chainEndNote = noteLayout;
        NoteLayout current = noteLayout;
        boolean continuesToNextSystem = false;

        LayoutHitTester.Point currentAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, noteLayout, verse);

        while (true) {
            NoteLayout next = findNextNoteLayout(current);
            if (next == null || next.getNote() == null) break;

            Lyric nextLyric = next.getNote().getLyric(verse);
            if (nextLyric == null) break;

            SyllableType nextType = nextLyric.getType();
            if (nextType == SyllableType.MIDDLE || nextType == SyllableType.END) {
                LayoutHitTester.Point nextAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, next, verse);

                if (!isSameSystem(currentAbs, nextAbs)) {
                    continuesToNextSystem = true;
                    break;
                }

                chainEndNote = next;
                current = next;

                if (nextLyric.getText() != null && !nextLyric.getText().trim().isEmpty()) {
                    break;
                }
            } else {
                break;
            }
        }

        double currentTextWidth = lyricLayout.getTotalWidth();
        double startRightX = noteCenterX + (currentTextWidth / 2.0);

        double endLeftX;
        if (chainEndNote == noteLayout) {
            if (continuesToNextSystem) {
                endLeftX = startRightX + 18.0;
            } else {
                return hyphens;
            }
        } else {
            LayoutHitTester.Point targetAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, chainEndNote, verse);
            double diffModelX = targetAbs.x() - currentAbs.x();
            double targetNoteCenterX = noteCenterX + diffModelX;

            LyricLayout targetLyricLayout = chainEndNote.getLyric(verse);
            double targetTextWidth = (targetLyricLayout != null) ? targetLyricLayout.getTotalWidth() : 0.0;

            endLeftX = targetNoteCenterX - (targetTextWidth / 2.0);
        }

        double gap = endLeftX - startRightX;
        double fontSizeSp = lyricLayout.getFontSize();

        if (gap >= 6.0) {
            double targetSpacing = Math.max(16.0, fontSizeSp * 1.5);
            int numHyphens = (int) Math.floor(gap / targetSpacing);
            numHyphens = Math.clamp(numHyphens, 1, 15);

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
        } else if (chainEndNote != noteLayout) {
            double hyphenX = (startRightX + endLeftX) / 2.0;
            hyphens.add(new LyricLayout.HyphenLayout(hyphenX, lyricLayout.getModelY(), 1.0, fontSizeSp));
        }

        return hyphens;
    }

    private static boolean isSameSystem(LayoutHitTester.Point p1, LayoutHitTester.Point p2) {
        if (p1 == null || p2 == null) return false;
        return p2.x() > p1.x() && Math.abs(p2.y() - p1.y()) < 2.0;
    }

    private static boolean isPreviousLyricConnectedFromPreviousSystem(LyricLayout lyricLayout, ScoreLayout scoreLayout) {
        NoteLayout noteLayout = lyricLayout.getNoteLayout();
        if (noteLayout == null) return false;

        NoteLayout prevNote = findPreviousNoteLayout(noteLayout);
        if (prevNote != null && prevNote.getNote() != null) {
            Lyric prevLyric = prevNote.getNote().getLyric(lyricLayout.getVerse());
            if (prevLyric != null) {
                SyllableType type = prevLyric.getType();
                if (type == SyllableType.BEGIN || type == SyllableType.MIDDLE) {
                    LayoutHitTester.Point currentAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, noteLayout, lyricLayout.getVerse());
                    LayoutHitTester.Point prevAbs = LayoutHitTester.getLyricAbsolutePosition(scoreLayout, prevNote, lyricLayout.getVerse());
                    return !isSameSystem(prevAbs, currentAbs);
                }
            }
        }
        return false;
    }

    private static NoteLayout findNextNoteLayout(NoteLayout fromNote) {
        if (fromNote == null || fromNote.getNote() == null || fromNote.getSegment() == null) return null;

        int targetVoice = fromNote.getNote().getVoice();
        int targetStaff = (fromNote.getStaff() != null) ? fromNote.getStaff().getStaffIndex() : 0;

        SegmentLayout segmentNode = fromNote.getSegment().getNextSameType();

        while (segmentNode != null) {
            for (ElementLayout element : segmentNode.getElements()) {
                if (element instanceof NoteLayout nextNote) {
                    if (nextNote.getNote() != null
                            && nextNote.getNote().getVoice() == targetVoice
                            && (nextNote.getStaff() == null || nextNote.getStaff().getStaffIndex() == targetStaff)) {
                        return nextNote;
                    }
                }
            }
            segmentNode = segmentNode.getNextSameType();
        }
        return null;
    }

    private static NoteLayout findPreviousNoteLayout(NoteLayout fromNote) {
        if (fromNote == null || fromNote.getNote() == null || fromNote.getSegment() == null) return null;

        int targetVoice = fromNote.getNote().getVoice();
        int targetStaff = (fromNote.getStaff() != null) ? fromNote.getStaff().getStaffIndex() : 0;

        SegmentLayout segmentNode = fromNote.getSegment().getPrevSameType();

        while (segmentNode != null) {
            for (ElementLayout element : segmentNode.getElements()) {
                if (element instanceof NoteLayout prevNote) {
                    if (prevNote.getNote() != null
                            && prevNote.getNote().getVoice() == targetVoice
                            && (prevNote.getStaff() == null || prevNote.getStaff().getStaffIndex() == targetStaff)) {
                        return prevNote;
                    }
                }
            }
            segmentNode = segmentNode.getPrevSameType();
        }
        return null;
    }
}