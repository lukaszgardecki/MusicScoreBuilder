package org.example.musicscorebuilder.components.music;

import java.util.*;

public class Segment {
    private Segment next;
    private Segment prev;
    private Measure parent;
    private final SegmentType type;
    private final Map<Staff, List<Element>> staffElements = new HashMap<>();

    public Segment(SegmentType type, Measure parent) {
        this.type = type;
        this.parent = parent;
        for (Staff staff : parent.getStaves()) {
            staffElements.put(staff, new ArrayList<>());
        }
    }

    public void insertNote(Staff staff, Note newNote) {
        List<Note> currentNotesInVoice = getNotesByStaffAndVoice(staff, newNote.getVoice());

        if (!currentNotesInVoice.isEmpty()) {
            for (Note oldNote : currentNotesInVoice) {
                if (oldNote.getType() != newNote.getType()) {
                    removeElement(staff, oldNote);
                }
            }
        }

        addElement(staff, newNote);
    }

    public void removeElement(Staff staff, Element element) {
        List<Element> elements = staffElements.get(staff);
        if (elements != null) {
            elements.remove(element);
        }
    }

    public void addElement(Staff staff, Element element) {
        staffElements.computeIfAbsent(staff, k -> new ArrayList<>()).add(element);
    }

    public List<Element> getElementsByStaff(Staff staff) {
        return staffElements.getOrDefault(staff, Collections.emptyList());
    }

    public int getVoiceCountByStaff(Staff staff) {
        return (int) getElementsByStaff(staff).stream()
                .filter(Note.class::isInstance)
                .map(Note.class::cast)
                .map(Note::getVoice)
                .distinct()
                .count();
    }

    public List<Note> getNotesByStaffAndVoice(Staff staff, int voice) {
        return getElementsByStaff(staff).stream()
                .filter(Note.class::isInstance)
                .map(Note.class::cast)
                .filter(n -> n.getVoice() == voice)
                .toList();
    }

    public Measure getParent() { return parent; }
    public SegmentType getType() { return type; }
    public int getDuration() {
        return staffElements.values().stream()
                .flatMap(List::stream)
                .mapToInt(Element::getDuration)
                .min()
                .orElse(0);
    }

    public Segment getNextSameType() {
        Segment current = this.next;
        while (current != null) {
            if (current.getType() == this.type) {
                return current;
            }
            current = current.next;
        }
        return null;
    }
    public Segment getPrevSameType() {
        Segment current = this.prev;
        while (current != null) {
            if (current.getType() == this.type) {
                return current;
            }
            current = current.prev;
        }
        return null;
    }

    public void setNext(Segment next) { this.next = next; }
    public void setPrev(Segment prev) { this.prev = prev; }
}
