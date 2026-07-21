package org.example.musicscorebuilder.components.music;

import java.util.*;

public class Segment {
    private final SegmentType type;
    private final Map<Staff, List<Element>> staffElements = new HashMap<>();

    public Segment(SegmentType type, List<Staff> staves) {
        this.type = type;
        for (Staff staff : staves) {
            staffElements.put(staff, new ArrayList<>());
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

    public SegmentType getType() { return type; }
}
