package org.example.musicscorebuilder.components.music;

public class Lyric {
    private String text;
    private int verse;
    private SyllableType type;
    private double fontSize;

    public Lyric() {}

    public Lyric(String text, SyllableType type, int verse, double fontsize) {
        this.text = text;
        this.type = type;
        this.verse = verse;
        this.fontSize = fontsize;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public SyllableType getType() { return type; }
    public void setType(SyllableType type) { this.type = type; }

    public int getVerse() { return verse; }
    public void setVerse(int verse) { this.verse = verse; }

    public double getFontSize() { return fontSize; }
    public void setFontSize(double fontSize) { this.fontSize = fontSize; }
}