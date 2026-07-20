package org.example.musicscorebuilder.components.layout;

import java.util.Comparator;
import java.util.List;

public class ChordCollisionResolver {

    public void resolveCollisions(ChordLayout chord) {
        List<NoteLayout> notes = chord.getNotes();
        if (notes.size() <= 1) {
            if (!notes.isEmpty()) notes.getFirst().setXOffset(0.0);
            return;
        }

        notes.forEach(note -> note.setXOffset(0.0));
        List<NoteLayout> sortedNotes = notes.stream()
                .sorted(Comparator.comparingInt(NoteLayout::getDiatonicStep))
                .toList();

        resolveCollisions(chord.getStemDirection(), sortedNotes);
    }

    private void resolveCollisions(StemDirection stemDirection, List<NoteLayout> notes) {
        for (int i = 0; i < notes.size() - 1; i++) {
            NoteLayout currentNote = notes.get(i);
            NoteLayout nextNote = notes.get(i + 1);

            int diatonicDistance = nextNote.getDiatonicStep() - currentNote.getDiatonicStep();

            if (diatonicDistance <= 1) {
                if (stemDirection == StemDirection.UP) {
                    nextNote.setXOffset(currentNote.getWidth());
                } else {
                    currentNote.setXOffset(nextNote.getWidth());
                }
                i++;
            }
        }
    }
}