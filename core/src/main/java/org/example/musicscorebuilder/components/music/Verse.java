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
    private boolean customLineBreaks = false;

    public Verse(int number) {
        this.number = number;
    }

    public int getNumber() { return number; }
    public Map<Note, Lyric> getSyllables() { return syllables; }

    public boolean isCustomLineBreaks() { return customLineBreaks; }
    public void setCustomLineBreaks(boolean customLineBreaks) { this.customLineBreaks = customLineBreaks; }

    public void addSyllable(Note note, Lyric lyric) { syllables.put(note, lyric); }
    public void removeSyllable(Note note) { syllables.remove(note); }

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

    public TextFrameVerse toTextFrameVerse(Map<Note, Integer> noteToSystemMap) {
        List<TextLine> lines = new ArrayList<>();
        TextLine currentLine = new TextLine();
        Integer activeSystemIndex = null;
        Double lastFontSize = null;
        boolean previousLyricBreak = false;

        for (Map.Entry<Note, Lyric> entry : syllables.entrySet()) {
            Note note = entry.getKey();
            Lyric lyric = entry.getValue();
            if (lyric == null || lyric.getFragments() == null) continue;

            if (lyric.getFontSize() != null) {
                lastFontSize = lyric.getFontSize();
            }

            Integer noteSystemIndex = (noteToSystemMap != null) ? noteToSystemMap.get(note) : null;

            // Jeśli customLineBreaks == false -> łamiemy wg systemów
            boolean isSystemChanged = !customLineBreaks
                    && noteSystemIndex != null
                    && activeSystemIndex != null
                    && !noteSystemIndex.equals(activeSystemIndex);

            // Jeśli customLineBreaks == true -> łamiemy TYLKO po ręcznym Enterze
            boolean isManualBreak = customLineBreaks && previousLyricBreak;

            if (isSystemChanged || isManualBreak) {
                if (!currentLine.getFragments().isEmpty()) {
                    lines.add(currentLine);
                    currentLine = new TextLine();

                    if (lastFontSize != null) {
                        currentLine.setFontSize(lastFontSize);
                    }
                }
            }

            if (noteSystemIndex != null) {
                activeSystemIndex = noteSystemIndex;
            }

            if (currentLine.getFontSize() == null && lastFontSize != null) {
                currentLine.setFontSize(lastFontSize);
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

            previousLyricBreak = lyric.isLineBreakAfter();
        }

        if (!currentLine.getFragments().isEmpty()) {
            lines.add(currentLine);
        }

        return new TextFrameVerse(this.number, lines);
    }

    public TextFrameVerse toTextFrameVerse() {
        return toTextFrameVerse(null);
    }
}