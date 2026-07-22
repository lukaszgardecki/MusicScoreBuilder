package org.example.musicscorebuilder.components.layout;

import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public class BeamGroupLayout implements Selectable {
    private final List<NoteLayout> notes = new ArrayList<>();
    private boolean selected;

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return notes.isEmpty() ? 1 : notes.getFirst().getVoice(); }
    @Override
    public boolean contains(double measureX, double measureY) {
        if (notes.isEmpty()) return false;

        NoteLayout first = getFirstNote();
        NoteLayout last = getLastNote();
        if (first == null || last == null) return false;

        ScoreStyle style = first.getScoreStyle();

        var stemWidth = style.getNoteStemWidth();
        var halfBeamThickness = 0.5 * style.getNoteBeamThickness();

        double firstStemLocalX = (first.getStem().getDirection() == StemDirection.UP) ? first.getBoxWidth() - first.getStem().getWidth() : 0;
        double startX = first.getParent().getX() + first.getX() + firstStemLocalX;

        double lastStemLocalX = (last.getStem().getDirection() == StemDirection.UP) ? last.getBoxWidth() - last.getStem().getWidth() : 0;
        double endX = last.getParent().getX() + last.getX() + lastStemLocalX + stemWidth;

        double startY = first.getParent().getY() + first.getStem().getEndY();
        double endY = last.getParent().getY() + last.getStem().getEndY();

        Polygon poly = new Polygon(
                startX, startY - halfBeamThickness,
                endX, endY - halfBeamThickness,
                endX, endY + halfBeamThickness,
                startX, startY + halfBeamThickness
        );
        return poly.contains(measureX, measureY);
    }

    public void addNote(NoteLayout note) { notes.add(note); }

    public void clear() {
        for (NoteLayout note : notes) {
            note.setBeamGroup(null);
        }
    }

    public NoteLayout getFirstNote() {
        if (notes.isEmpty()) return null;
        return notes.getFirst();
    }

    public NoteLayout getLastNote() {
        if (notes.isEmpty()) return null;
        return notes.getLast();
    }

    public int size() { return notes.size(); }
    public boolean isEmpty() { return notes.isEmpty(); }
}
