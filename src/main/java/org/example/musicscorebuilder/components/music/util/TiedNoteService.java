package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.Pitch;
import org.example.musicscorebuilder.components.music.PitchStep;

import java.util.ArrayList;
import java.util.List;

public class TiedNoteService {

    public static void syncTiedNotesPitch(NoteLayout sourceNoteLayout) {
        if (sourceNoteLayout == null) return;

        Note sourceNote = sourceNoteLayout.getNote();
        if (sourceNote == null || !sourceNote.hasTie()) return;

        Pitch targetPitch = sourceNote.getPitch();
        if (targetPitch == null) return;

        List<NoteLayout> tiedChain = findTiedChain(sourceNoteLayout);
        if (tiedChain.size() <= 1) return;

        PitchStep step = targetPitch.getStep();
        int alter = targetPitch.getAlter();
        int octave = targetPitch.getOctave();

        for (int i = 0; i < tiedChain.size(); i++) {
            NoteLayout tiedNoteLayout = tiedChain.get(i);
            if (tiedNoteLayout == sourceNoteLayout) continue;

            Note tiedNote = tiedNoteLayout.getNote();
            if (tiedNote != null) {
                tiedNote.setPitch(new Pitch(step, alter, octave));
                tiedNoteLayout.refreshMeasureAccidentals();
            }
        }
    }

    private static List<NoteLayout> findTiedChain(NoteLayout targetNoteLayout) {
        List<NoteLayout> chain = new ArrayList<>(8);
        if (targetNoteLayout == null || targetNoteLayout.getSegment() == null) return chain;

        chain.add(targetNoteLayout);

        if (targetNoteLayout.getStaff() == null) return chain;
        int staffId = targetNoteLayout.getStaff().getStaffIndex();
        int voice = targetNoteLayout.getVoice();

        NoteLayout current = targetNoteLayout;
        while (current.getNote() != null && current.getNote().isTieStop()) {
            NoteLayout prev = findPrevNoteInVoice(current.getSegment(), staffId, voice);
            if (prev != null && prev.getNote() != null && prev.getNote().isTieStart()) {
                chain.add(prev);
                current = prev;
            } else {
                break;
            }
        }

        current = targetNoteLayout;
        while (current.getNote() != null && current.getNote().isTieStart()) {
            NoteLayout next = findNextNoteInVoice(current.getSegment(), staffId, voice);
            if (next != null && next.getNote() != null && next.getNote().isTieStop()) {
                chain.add(next);
                current = next;
            } else {
                break;
            }
        }

        return chain;
    }

    private static NoteLayout findNextNoteInVoice(SegmentLayout startSegment, int staffId, int voice) {
        if (startSegment == null) return null;
        SegmentLayout current = startSegment.getNext();

        while (current != null) {
            List<ElementLayout> elements = current.getElements();
            for (int i = 0; i < elements.size(); i++) {
                ElementLayout el = elements.get(i);
                if (el.getStaff() != null && el.getStaff().getStaffIndex() == staffId && el.getVoice() == voice) {
                    if (el instanceof NoteLayout noteLayout) {
                        return noteLayout;
                    } else if (el instanceof RestLayout) {
                        return null; // Pauza przerywa łańcuch
                    }
                }
            }
            current = current.getNext();
        }
        return null;
    }

    private static NoteLayout findPrevNoteInVoice(SegmentLayout startSegment, int staffId, int voice) {
        if (startSegment == null) return null;
        SegmentLayout current = startSegment.getPrev();

        while (current != null) {
            List<ElementLayout> elements = current.getElements();
            for (int i = 0; i < elements.size(); i++) {
                ElementLayout el = elements.get(i);
                if (el.getStaff() != null && el.getStaff().getStaffIndex() == staffId && el.getVoice() == voice) {
                    if (el instanceof NoteLayout noteLayout) {
                        return noteLayout;
                    } else if (el instanceof RestLayout) {
                        return null; // Pauza przerywa łańcuch
                    }
                }
            }
            current = current.getPrev();
        }
        return null;
    }
}