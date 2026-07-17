package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.Note;

public class NoteLayout {
    private final Leland fontData = Leland.NOTE_BLACK;
    private final ScoreStyle style;
    private final Note note;
    private final double x, y;

    public NoteLayout(Note note, double x, double y, ScoreStyle scoreStyle) {
        style = scoreStyle;
        this.note = note;
        this.x = x;
        this.y = y * style.getStaffLineSpacing();
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getBoxY() { return y - (0.5 * style.getStaffLineSpacing()); }

    public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getHeight() { return style.getStaffLineSpacing(); }

    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return fontData.getCode(); }
}
