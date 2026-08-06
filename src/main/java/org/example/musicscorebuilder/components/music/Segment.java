package org.example.musicscorebuilder.components.music;

import java.util.*;

public class Segment {
    private Measure parent;
    private final SegmentType type;
    private final Map<Integer, List<Element>> staffElements = new HashMap<>();
    private int duration;

    public Segment(SegmentType type, Measure parent) {
        this.type = type;
        this.parent = parent;
        for (Staff staff : parent.getStaves()) {
            staffElements.put(staff.getIndex(), new ArrayList<>());
        }
    }

    public void insertNote(int staffId, Note newNote) {
        List<NoteRestElement> currentElements = getNoteRestByStaffAndVoice(staffId, newNote.getVoice());

        if (!currentElements.isEmpty()) {
            List<NoteRestElement> toRemove = currentElements.stream()
                    .filter(oldElement -> {
                        if (oldElement instanceof Rest) return true;
                        if (oldElement instanceof Note oldNote) return oldNote.getType() != newNote.getType();
                        return false;
                    })
                    .toList();
            for (NoteRestElement elementToRemove : toRemove) {
                removeNoteRest(staffId, elementToRemove);
            }
        }

        addElement(staffId, newNote);
    }

    public void removeNoteRest(int staffId, NoteRestElement element) {
        List<Element> elements = staffElements.get(staffId);
        if (elements != null && element instanceof Element el) {
            elements.remove(el);
        }
    }

    public void addElement(int staffId, Element element) {
        staffElements.computeIfAbsent(staffId, k -> new ArrayList<>()).add(element);
    }

    public void replaceElement(int staffId, Element oldElement, Element newElement) {
        List<Element> elements = staffElements.get(staffId);
        if (elements != null) {
            int index = elements.indexOf(oldElement);
            if (index != -1) {
                elements.set(index, newElement);
            }
        }
    }

    public List<Element> getElementsByStaff(int staffId) {
        return staffElements.getOrDefault(staffId, Collections.emptyList());
    }

    public int getVoiceCountByStaff(int staffId) {
        return (int) getElementsByStaff(staffId).stream()
                .filter(NoteRestElement.class::isInstance)
                .map(NoteRestElement.class::cast)
                .map(NoteRestElement::getVoice)
                .distinct()
                .count();
    }

    public List<NoteRestElement> getNoteRestByStaffAndVoice(int staffId, int voice) {
        return getElementsByStaff(staffId).stream()
                .filter(e -> e instanceof NoteRestElement nre && nre.getVoice() == voice)
                .map(NoteRestElement.class::cast)
                .toList();
    }

    public Measure getParent() { return parent; }
    public SegmentType getType() { return type; }
    public int getDuration() { return duration; }
    public void setDuration(int d) { duration = d; }

    public boolean isEmpty() {
        return staffElements.values().stream().allMatch(list -> list == null || list.isEmpty());
    }
    public boolean isNoteRest() { return type == SegmentType.NOTEREST; }
}