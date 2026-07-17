package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Chord;

import java.util.ArrayList;
import java.util.List;

public class ChordLayout extends ElementLayout {
    private final Chord chord;
    private final List<NoteLayout> notes = new ArrayList<>();
    private double x;

    public ChordLayout(Chord chord, double x, ScoreStyle scoreStyle) {
        super(true, scoreStyle);
        this.chord = chord;
        this.x = x;
    }

    public void add(NoteLayout note) {
        notes.add(note);
    }

    @Override public double getX() { return x; }
    @Override public double getY() { return notes.stream().mapToDouble(NoteLayout::getBoxY).min().orElse(0); }
    @Override public double getBoxY() { return getY(); }
    @Override
    public double getWidth() {
        if (notes.isEmpty()) return 0.0;
        double minX = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double singleNoteWidth = 0.0;

        for (NoteLayout note : notes) {
            if (note.getX() < minX) minX = note.getX();
            if (note.getX() > maxX) maxX = note.getX();
            singleNoteWidth = Math.max(singleNoteWidth, note.getBoxWidth());
        }
        return (maxX - minX) + singleNoteWidth;
    }

    @Override
    public double getHeight() {
        if (notes.isEmpty()) return 0.0;
        double highestBoxY = Double.MAX_VALUE;
        double lowestBoxBottom = -Double.MAX_VALUE;

        for (NoteLayout note : notes) {
            double currentBoxY = note.getBoxY();
            if (currentBoxY < highestBoxY) highestBoxY = currentBoxY;

            double currentBoxBottom = currentBoxY + note.getHeight();
            if (currentBoxBottom > lowestBoxBottom) lowestBoxBottom = currentBoxBottom;
        }
        return lowestBoxBottom - highestBoxY;
    }

    public List<NoteLayout> getNotes() { return notes; }

    public void setX(double x) { this.x = x; }
}
