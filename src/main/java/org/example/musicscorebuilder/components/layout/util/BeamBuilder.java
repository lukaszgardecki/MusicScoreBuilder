package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.BeamType;

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

        notesToProcess.sort(Comparator.comparingDouble(NoteLayout::getX));

        Map<StaffLayout, Map<Integer, List<NoteLayout>>> rawNotesByStaffAndVoice = new HashMap<>();

        for (NoteLayout noteLayout : notesToProcess) {
            StaffLayout staff = noteLayout.getStaffLayout();
            int voice = noteLayout.getNote().getVoice();

            rawNotesByStaffAndVoice
                    .computeIfAbsent(staff, k -> new HashMap<>())
                    .computeIfAbsent(voice, k -> new ArrayList<>())
                    .add(noteLayout);
        }

        for (Map<Integer, List<NoteLayout>> voicesMap : rawNotesByStaffAndVoice.values()) {
            for (List<NoteLayout> voiceNotes : voicesMap.values()) {

                BeamGroupLayout currentGroup = null;

                for (NoteLayout noteLayout : voiceNotes) {
                    BeamType beamType = noteLayout.getNote().getBeam();

                    if (beamType == BeamType.BEGIN) {
                        currentGroup = new BeamGroupLayout();
                        resultGroups.add(currentGroup);
                        currentGroup.addNote(noteLayout);
                    } else if (beamType == BeamType.CONTINUE || beamType == BeamType.END) {
                        if (currentGroup != null) {
                            currentGroup.addNote(noteLayout);
                        } else {
                            currentGroup = new BeamGroupLayout();
                            resultGroups.add(currentGroup);
                            currentGroup.addNote(noteLayout);
                        }

                        if (beamType == BeamType.END) {
                            currentGroup = null;
                        }
                    } else {
                        currentGroup = null;
                    }
                }
            }
        }

        resultGroups.removeIf(group -> group.size() <= 1);

        notesToProcess.clear();
        return resultGroups;
    }
}
