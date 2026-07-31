package org.example.musicscorebuilder.components.music;

import java.util.*;

public class Segment {
    private Segment next;
    private Segment prev;
    private Measure parent;
    private final SegmentType type;
    private final Map<Staff, List<Element>> staffElements = new HashMap<>();
    private int duration;
    private int startTick;

    public Segment(SegmentType type, Measure parent) {
        this.type = type;
        this.parent = parent;
        for (Staff staff : parent.getStaves()) {
            staffElements.put(staff, new ArrayList<>());
        }
    }

    public void insertNote(Staff staff, Note newNote) {
        List<NoteRestElement> currentElements = getNoteRestByStaffAndVoice(staff, newNote.getVoice());

        if (!currentElements.isEmpty()) {
            List<NoteRestElement> toRemove = currentElements.stream()
                    .filter(oldElement -> {
                        if (oldElement instanceof Rest) return true;
                        if (oldElement instanceof Note oldNote) return oldNote.getType() != newNote.getType();
                        return false;
                    })
                    .toList();
            for (NoteRestElement elementToRemove : toRemove) {
                removeNoteRest(staff, elementToRemove);
            }
        }

        addElement(staff, newNote);
    }

    public void removeNoteRest(Staff staff, NoteRestElement element) {
        List<Element> elements = staffElements.get(staff);
        if (elements != null && element instanceof Element el) {
            elements.remove(el);
        }
    }

    public void addElement(Staff staff, Element element) {
        staffElements.computeIfAbsent(staff, k -> new ArrayList<>()).add(element);
    }

    public List<Element> getElementsByStaff(Staff staff) {
        return staffElements.getOrDefault(staff, Collections.emptyList());
    }

    public int getVoiceTicksByStaff(Staff staff, int voice) {
        List<NoteRestElement> elements = getNoteRestByStaffAndVoice(staff, voice);
        if (!elements.isEmpty()) {
            return elements.get(0).getType().getTicks();
        }
        return getDuration();
    }

    public int getVoiceCountByStaff(Staff staff) {
        return (int) getElementsByStaff(staff).stream()
                .filter(Note.class::isInstance)
                .map(Note.class::cast)
                .map(Note::getVoice)
                .distinct()
                .count();
    }

    public List<NoteRestElement> getNoteRestByStaffAndVoice(Staff staff, int voice) {
        return getElementsByStaff(staff).stream()
                .filter(e -> e instanceof NoteRestElement nre && nre.getVoice() == voice)
                .map(NoteRestElement.class::cast)
                .toList();
    }

    public Measure getParent() { return parent; }
    public SegmentType getType() { return type; }
//    public int getDuration() {
//        return staffElements.values().stream()
//                .filter(list -> !list.isEmpty())
//                .flatMap(List::stream)
//                .mapToInt(Element::getDuration)
//                .min()
//                .orElse(0);
//    }

    public int getDuration() { return duration; }
    public void setDuration(int d) { duration = d; }
    public int getStartTick() {
        return startTick;
    }

    public void setStartTick(int tick) {
        this.startTick = tick;
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

    public boolean isEmpty() {
        return staffElements.values().stream().allMatch(list -> list == null || list.isEmpty());
    }
    public boolean isNoteRest() { return type == SegmentType.NOTEREST; }


    public void setNext(Segment next) { this.next = next; }
    public void setPrev(Segment prev) { this.prev = prev; }
}
