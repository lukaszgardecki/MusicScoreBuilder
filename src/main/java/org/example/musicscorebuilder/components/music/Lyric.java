package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Lyric {
    private List<LyricFragment> fragments;
    private int verse;
    private SyllableType type;
    private double fontSize;

    public Lyric() {
        this.fragments = new ArrayList<>();
    }

    public Lyric(List<LyricFragment> fragments, SyllableType type, int verse, double fontSize) {
        this.fragments = fragments != null ? fragments : new ArrayList<>();
        this.type = type;
        this.verse = verse;
        this.fontSize = fontSize;
    }

    public List<LyricFragment> getFragments() { return fragments; }
    public void setFragments(List<LyricFragment> fragments) { this.fragments = fragments; }

    public String getText() {
        if (fragments == null || fragments.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (LyricFragment fragment : fragments) {
            sb.append(fragment.getText());
        }
        return sb.toString();
    }

    public SyllableType getType() { return type; }
    public void setType(SyllableType type) { this.type = type; }

    public int getVerse() { return verse; }
    public void setVerse(int verse) { this.verse = verse; }

    public double getFontSize() { return fontSize; }
    public void setFontSize(double fontSize) { this.fontSize = fontSize; }
}