package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;
import java.util.stream.Collectors;

public class Segment {
    @JsonIgnore
    private Measure parent;

    private final SegmentType type;

    @JsonInclude(
            content = JsonInclude.Include.NON_EMPTY,
            value = JsonInclude.Include.NON_EMPTY
    )
    private final Map<Integer, List<Element>> staffElements = new HashMap<>();
    private int duration;

    @JsonCreator
    public Segment(
            @JsonProperty("type") SegmentType type,
            @JsonProperty("duration") int duration,
            @JsonProperty("staffElements") Map<Integer, List<Element>> staffElements
    ) {
        this.type = type;
        this.duration = duration;
        if (staffElements != null) {
            this.staffElements.putAll(staffElements);
        }
    }

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
                    .collect(Collectors.toList());
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

    public void clearStaff(int staffId) {
        List<Element> elements = staffElements.get(staffId);
        if (elements != null) {
            elements.clear();
        }
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

    public Map<Integer, List<Element>> getStaffElements() {
        return staffElements;
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
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Measure getParent() { return parent; }
    public SegmentType getType() { return type; }
    public int getDuration() { return duration; }

    @JsonIgnore
    public boolean isEmpty() {
        return staffElements.values().stream().allMatch(list -> list == null || list.isEmpty());
    }

    @JsonIgnore
    public boolean isNoteRest() { return type == SegmentType.NOTEREST; }

    public void setParent(Measure parent) { this.parent = parent; }
    public void setDuration(int d) { duration = d; }
}