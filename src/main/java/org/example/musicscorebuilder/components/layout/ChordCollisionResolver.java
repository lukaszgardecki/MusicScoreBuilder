package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.NoteType;

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

            if (shouldOffset(nextNote, currentNote)) {
                double offsetWidth = currentNote.getBoxWidth();

                if (stemDirection == StemDirection.UP) {
                    nextNote.setXOffset(offsetWidth);
                } else {
                    currentNote.setXOffset(offsetWidth);
                }
                i++;
            }
        }
    }

    private boolean shouldOffset(NoteLayout nextNote, NoteLayout currentNote) {
        int diatonicDistance = nextNote.getDiatonicStep() - currentNote.getDiatonicStep();

        if (diatonicDistance == 1) return true;
        if (diatonicDistance == 0) {
            NoteType type1 = currentNote.getNote().getType();
            NoteType type2 = nextNote.getNote().getType();
            boolean bothAreBlack = type1.isBlack() && type2.isBlack();
            boolean bothAreHalf = type1.isHalf() && type2.isHalf();
            return !(bothAreBlack || bothAreHalf);
        }
        return false;
    }
}