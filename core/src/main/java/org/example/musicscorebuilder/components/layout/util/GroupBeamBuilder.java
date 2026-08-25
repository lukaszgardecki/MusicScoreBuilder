package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.BeamType;

import java.util.ArrayList;
import java.util.List;

public class GroupBeamBuilder {
    private final List<NoteLayout> notesToProcess = new ArrayList<>();

    public void add(NoteLayout noteLayout) {
        notesToProcess.add(noteLayout);
    }

    public List<BeamGroupLayout> build() {
        int noteCount = notesToProcess.size();
        if (noteCount == 0) return new ArrayList<>();

        List<BeamGroupLayout> resultGroups = new ArrayList<>(noteCount >> 1);
        List<StaffVoiceBucket> buckets = new ArrayList<>(4);

        for (int i = 0; i < noteCount; i++) {
            NoteLayout noteLayout = notesToProcess.get(i);
            StaffLayout staff = noteLayout.getStaff();
            int voice = noteLayout.getNote().getVoice();

            StaffVoiceBucket targetBucket = null;
            int bucketCount = buckets.size();
            for (int b = 0; b < bucketCount; b++) {
                StaffVoiceBucket bucket = buckets.get(b);
                if (bucket.voice == voice && bucket.staff == staff) {
                    targetBucket = bucket;
                    break;
                }
            }

            if (targetBucket == null) {
                targetBucket = new StaffVoiceBucket(staff, voice);
                buckets.add(targetBucket);
            }

            targetBucket.notes.add(noteLayout);
        }

        int bucketCount = buckets.size();
        for (int b = 0; b < bucketCount; b++) {
            List<NoteLayout> voiceNotes = buckets.get(b).notes;
            int voiceNotesSize = voiceNotes.size();
            BeamGroupLayout currentGroup = null;

            for (int i = 0; i < voiceNotesSize; i++) {
                NoteLayout noteLayout = voiceNotes.get(i);
                BeamType beamType = noteLayout.getNote().getBeam();

                if (beamType == BeamType.BEGIN) {
                    if (currentGroup != null) {
                        finalizeGroup(currentGroup, resultGroups);
                    }
                    currentGroup = new BeamGroupLayout();
                    currentGroup.addNote(noteLayout);
                    noteLayout.setBeamGroup(currentGroup);
                } else if (beamType == BeamType.CONTINUE || beamType == BeamType.END) {
                    if (currentGroup == null) {
                        currentGroup = new BeamGroupLayout();
                    }
                    currentGroup.addNote(noteLayout);
                    noteLayout.setBeamGroup(currentGroup);

                    if (beamType == BeamType.END) {
                        finalizeGroup(currentGroup, resultGroups);
                        currentGroup = null;
                    }
                } else {
                    if (currentGroup != null) {
                        finalizeGroup(currentGroup, resultGroups);
                        currentGroup = null;
                    }
                    noteLayout.setBeamGroup(null);
                }
            }

            if (currentGroup != null) {
                finalizeGroup(currentGroup, resultGroups);
            }
        }

        notesToProcess.clear();
        return resultGroups;
    }

    private void finalizeGroup(BeamGroupLayout group, List<BeamGroupLayout> resultGroups) {
        if (group.size() > 1) {
            resultGroups.add(group);
        } else {
            group.clear();
        }
    }

    private static final class StaffVoiceBucket {
        final StaffLayout staff;
        final int voice;
        final List<NoteLayout> notes = new ArrayList<>();

        StaffVoiceBucket(StaffLayout staff, int voice) {
            this.staff = staff;
            this.voice = voice;
        }
    }
}