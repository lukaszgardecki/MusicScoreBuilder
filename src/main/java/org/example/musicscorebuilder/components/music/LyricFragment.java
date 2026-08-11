package org.example.musicscorebuilder.components.music;

public class LyricFragment {
    private String text;
    private boolean bold;
    private boolean italic;
    private boolean underline;

    public LyricFragment() {}

    public LyricFragment(String text, boolean bold, boolean italic, boolean underline) {
        this.text = text;
        this.bold = bold;
        this.italic = italic;
        this.underline = underline;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }
}