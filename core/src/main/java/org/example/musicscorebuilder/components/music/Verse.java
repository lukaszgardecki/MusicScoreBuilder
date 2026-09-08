package org.example.musicscorebuilder.components.music;

import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;
import org.example.musicscorebuilder.components.music.frames.TextLine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Verse {
    private final int number;
    private final Map<Note, Lyric> syllables = new LinkedHashMap<>();

    public Verse(int number) {
        this.number = number;
    }


    public int getNumber() { return number; }
    public Map<Note, Lyric> getSyllables() { return syllables; }

    public void addSyllable(Note note, Lyric lyric) {
        syllables.put(note, lyric);
    }

    public void removeSyllable(Note note) {
        syllables.remove(note);
    }

    public String getPreviewText(int limit) {
        if (syllables.isEmpty()) {
            return number + ". (pusta zwrotka)";
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (Lyric lyric : syllables.values()) {
            if (lyric == null) continue;

            String text = lyric.getText();
            if (text == null || text.isBlank()) continue;

            if (count >= limit) {
                sb.append("...");
                break;
            }

            sb.append(text);

            SyllableType type = lyric.getType();
            if (type == SyllableType.SINGLE || type == SyllableType.END) {
                sb.append(" ");
            }

            count++;
        }

        String resultText = sb.toString().trim();

        if (resultText.isEmpty()) {
            return number + ". (pusta zwrotka)";
        }

        return resultText;
    }

    public TextFrameVerse toTextFrameVerse() {
        List<TextLine> lines = new ArrayList<>();
        TextLine currentLine = new TextLine();

        for (Lyric lyric : syllables.values()) {
            if (lyric == null || lyric.getFragments() == null) continue;

            if (currentLine.getFontSize() == null && lyric.getFontSize() != null) {
                currentLine.setFontSize(lyric.getFontSize());
            }

            for (LyricFragment fragment : lyric.getFragments()) {
                currentLine.addFragment(fragment);
            }

            SyllableType type = lyric.getType();
            if (type == SyllableType.SINGLE || type == SyllableType.END) {
                boolean bold = false, italic = false, underline = false;
                List<LyricFragment> frags = lyric.getFragments();
                if (frags != null && !frags.isEmpty()) {
                    LyricFragment lastFrag = frags.get(frags.size() - 1);
                    bold = lastFrag.isBold();
                    italic = lastFrag.isItalic();
                    underline = lastFrag.isUnderline();
                }

                currentLine.addFragment(new LyricFragment(" ", bold, italic, underline));
            }
        }

        if (!currentLine.getFragments().isEmpty()) {
            lines.add(currentLine);
        }

        return new TextFrameVerse(this.number, lines);
    }
}
