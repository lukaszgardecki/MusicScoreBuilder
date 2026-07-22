package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;

import java.util.*;

public class BeamBuilder {
    private final List<NoteLayout> notesToProcess = new ArrayList<>();

    public void add(NoteLayout noteLayout) {
        notesToProcess.add(noteLayout);
    }

    public List<BeamGroupLayout> build() {
        List<BeamGroupLayout> resultGroups = new ArrayList<>();

        if (notesToProcess.isEmpty()) {
            return resultGroups;
        }

        // 1. Sortujemy wszystko chronologicznie po X
        notesToProcess.sort(Comparator.comparingDouble(NoteLayout::getX));

        // 2. Klucz mapy: StaffLayout + Voice
        // Grupujemy surowe nuty po pięciolinii i głosie
        Map<StaffLayout, Map<Integer, List<NoteLayout>>> rawNotesByStaffAndVoice = new HashMap<>();

        for (NoteLayout noteLayout : notesToProcess) {
            StaffLayout staff = noteLayout.getStaffLayout();
            int voice = noteLayout.getNote().getVoice();

            rawNotesByStaffAndVoice
                    .computeIfAbsent(staff, k -> new HashMap<>())
                    .computeIfAbsent(voice, k -> new ArrayList<>())
                    .add(noteLayout);
        }

        // 3. Dla każdego głosu dzielimy nuty na sztywne paczki (np. po 4 nuty)
        for (Map<Integer, List<NoteLayout>> voicesMap : rawNotesByStaffAndVoice.values()) {
            for (List<NoteLayout> voiceNotes : voicesMap.values()) {

                BeamGroupLayout currentGroup = null;

                for (NoteLayout note : voiceNotes) {
                    // Jeśli nie mamy grupy lub obecna grupa ma już 4 nuty, tworzymy nową
                    if (currentGroup == null || currentGroup.size() >= 4) {
                        currentGroup = new BeamGroupLayout();
                        resultGroups.add(currentGroup);
                    }
                    currentGroup.addNote(note);
                }
            }
        }

        // Odrzucamy grupy, które mają tylko 1 nutę (bo pojedyncza nuta to nie belka)
        resultGroups.removeIf(group -> group.size() <= 1);

        notesToProcess.clear();
        return resultGroups;
    }
}
