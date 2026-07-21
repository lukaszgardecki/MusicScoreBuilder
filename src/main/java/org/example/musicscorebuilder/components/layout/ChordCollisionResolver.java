package org.example.musicscorebuilder.components.layout;

import java.util.Comparator;
import java.util.List;

public class ChordCollisionResolver {

    public void resolveCollisions(List<NoteLayout> notes) {
        if (notes == null || notes.size() <= 1) {
            if (notes != null && !notes.isEmpty()) {
                notes.getFirst().setXOffset(0.0);
            }
            return;
        }

        notes.forEach(note -> note.setXOffset(0.0));
        List<NoteLayout> sortedNotes = notes.stream()
                .sorted(Comparator.comparingInt(NoteLayout::getDiatonicStep))
                .toList();

        StemLayout stemLayout = sortedNotes.getFirst().getStem();
        StemDirection stemDirection = stemLayout != null ? stemLayout.getDirection() : StemDirection.UP;

        for (int i = 0; i < sortedNotes.size() - 1; i++) {
            NoteLayout currentNote = sortedNotes.get(i);
            NoteLayout nextNote = sortedNotes.get(i + 1);

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