package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StemLayout;
import org.example.musicscorebuilder.components.music.NoteType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class NoteCollisionResolver {

    private static final Comparator<NoteLayout> DIATONIC_STEP_COMPARATOR =
            Comparator.comparingInt(NoteLayout::getDiatonicStep);

    private NoteCollisionResolver() {}

    public static void resolve(List<NoteLayout> notes) {
        if (notes == null || notes.size() <= 1) {
            if (notes != null && !notes.isEmpty()) {
                notes.getFirst().setXOffset(0.0);
            }
            return;
        }

        int size = notes.size();

        List<NoteLayout> sortedNotes = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            NoteLayout note = notes.get(i);
            note.setXOffset(0.0);
            sortedNotes.add(note);
        }

        sortedNotes.sort(DIATONIC_STEP_COMPARATOR);

        NoteLayout firstNote = sortedNotes.getFirst();
        StemLayout stemLayout = firstNote.getStem();
        boolean stemIsUp = stemLayout == null || stemLayout.isUp();

        for (int i = 0; i < size - 1; i++) {
            NoteLayout currentNote = sortedNotes.get(i);
            NoteLayout nextNote = sortedNotes.get(i + 1);

            if (shouldOffset(nextNote, currentNote)) {
                double offsetWidth = currentNote.getBoxWidth();

                if (stemIsUp) {
                    nextNote.setXOffset(offsetWidth);
                } else {
                    currentNote.setXOffset(offsetWidth);
                }
                i++;
            }
        }
    }

    private static boolean shouldOffset(NoteLayout nextNote, NoteLayout currentNote) {
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