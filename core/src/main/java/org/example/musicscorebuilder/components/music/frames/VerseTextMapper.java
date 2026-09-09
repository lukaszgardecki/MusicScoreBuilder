package org.example.musicscorebuilder.components.music.frames;

import org.example.musicscorebuilder.components.music.*;

import java.util.*;

public class VerseTextMapper {

    public static String toEditorText(TextFrameVerse textFrameVerse) {
        if (textFrameVerse == null || textFrameVerse.getLines() == null) return "";
        StringBuilder sb = new StringBuilder();
        List<TextLine> lines = textFrameVerse.getLines();

        for (int i = 0; i < lines.size(); i++) {
            TextLine line = lines.get(i);
            if (line.getFragments() != null) {
                for (LyricFragment fragment : line.getFragments()) {
                    sb.append(fragment.getText());
                }
            }
            if (i < lines.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString().trim();
    }

    public static void applyLineBreaksFromText(Verse verse, String editorText) {
        if (verse == null || editorText == null) return;

        verse.setCustomLineBreaks(true);

        List<Lyric> lyrics = verse.getSyllables().values().stream()
                .filter(Objects::nonNull)
                .toList();

        if (lyrics.isEmpty()) return;

        for (Lyric lyric : lyrics) {
            lyric.setLineBreakAfter(false);
        }

        String[] lines = editorText.split("\r?\n");
        if (lines.length <= 1) return;

        int currentLyricIdx = 0;

        for (int i = 0; i < lines.length - 1; i++) {
            String line = lines[i];
            String normalizedLine = normalizeText(line);
            if (normalizedLine.isEmpty()) continue;

            StringBuilder currentLyricText = new StringBuilder();

            while (currentLyricIdx < lyrics.size()) {
                Lyric lyric = lyrics.get(currentLyricIdx);
                currentLyricText.append(normalizeText(lyric.getText()));

                if (currentLyricText.length() >= normalizedLine.length()) {
                    lyric.setLineBreakAfter(true);
                    currentLyricIdx++;
                    break;
                }
                currentLyricIdx++;
            }
        }
    }

    public static void applyLineBreaksToAllVerses(Collection<Verse> allVerses, Verse targetVerse, String editorText) {
        applyLineBreaksFromText(targetVerse, editorText);

        Set<Note> breakNotes = new HashSet<>();
        for (Map.Entry<Note, Lyric> entry : targetVerse.getSyllables().entrySet()) {
            if (entry.getValue() != null && entry.getValue().isLineBreakAfter()) {
                breakNotes.add(entry.getKey());
            }
        }

        for (Verse verse : allVerses) {
            if (verse == targetVerse) continue;

            verse.setCustomLineBreaks(true);

            for (Lyric l : verse.getSyllables().values()) {
                if (l != null) l.setLineBreakAfter(false);
            }

            for (Note breakNote : breakNotes) {
                Lyric lyric = verse.getSyllables().get(breakNote);
                if (lyric != null) {
                    lyric.setLineBreakAfter(true);
                }
            }
        }
    }

    private static String normalizeText(String text) {
        if (text == null) return "";
        // Usuwamy "1. " na początku, gwiazdki, spacje i interpunkcję – zostawiamy same litery/cyfry
        return text.replaceAll("^[0-9]+\\.\\s*", "")
                .replaceAll("[^a-zA-ZąćęłńóśźżĄĆĘŁŃÓŚŹŻ0-9]", "")
                .toLowerCase();
    }

    private static String stripSpaces(CharSequence cs) {
        return cs.toString().replaceAll("\\s+", "");
    }
}