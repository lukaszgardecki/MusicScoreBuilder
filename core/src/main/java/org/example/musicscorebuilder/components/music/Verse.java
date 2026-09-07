package org.example.musicscorebuilder.components.music;

import java.util.LinkedHashMap;
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
}
