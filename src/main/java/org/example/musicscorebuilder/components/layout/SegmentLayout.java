package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Barline;
import org.example.musicscorebuilder.components.music.KeySignature;
import org.example.musicscorebuilder.components.music.SegmentType;
import org.example.musicscorebuilder.components.music.TimeSignature;

import java.util.*;

public class SegmentLayout {
    private final ScoreStyle style;
    private final MeasureLayout parent;
    private final SegmentType type;
    private final Map<StaffLayout, List<ElementLayout>> elementsByStaff = new HashMap<>();
    private double y = 0, height;

    public SegmentLayout(SegmentType type, MeasureLayout parent) {
        this.style = parent.getScoreStyle();
        this.parent = parent;
        this.type = type;
        this.height = parent.getHeight()-style.getStaffLineWidth();
        for (StaffLayout staffLayout : parent.getStaffs()) {
            elementsByStaff.put(staffLayout, new ArrayList<>());
        }
    }

    public void addElement(StaffLayout staffLayout, ElementLayout elementLayout) {
        elementLayout.setX(style.getSegmentLeftMargin());
        elementsByStaff.computeIfAbsent(staffLayout, k -> new ArrayList<>()).add(elementLayout);
    }

    public void addStartBarline(Barline startBarline) {
        elementsByStaff.forEach((staff, elements) -> {
            elements.add(new BarlineLayout(startBarline, this, staff));
        });
    }

    public void addClef() {
        elementsByStaff.forEach((staff, elements) -> {
            ClefLayout clef = new ClefLayout(staff, this);
            clef.setX(style.getSegmentLeftMargin());
            elements.add(clef);
        });
    }

    public void addKeySignature(KeySignature keySignature) {
        elementsByStaff.forEach((staff, elements) -> {
            KeySigLayout keySig = new KeySigLayout(keySignature, staff, this);
            keySig.setX(style.getSegmentLeftMargin());
            elements.add(keySig);
        });
    }

    public void addTimeSignature(TimeSignature timeSignature) {
        elementsByStaff.forEach((staff, elements) -> {
            TimeSigLayout timeSig = new TimeSigLayout(timeSignature, staff, this);
            timeSig.setX(style.getSegmentLeftMargin());
            elements.add(timeSig);
        });
    }

    public List<ElementLayout> getElementsForStaff(StaffLayout staffLayout) {
        return elementsByStaff.getOrDefault(staffLayout, Collections.emptyList());
    }

    public Map<StaffLayout, List<ElementLayout>> getElementsByStaff() {
        return elementsByStaff;
    }

    public List<ElementLayout> getElements() {
        return elementsByStaff.values().stream()
                .flatMap(List::stream)
                .toList();
    }
    public double getX() {
        var segments = parent.getSegments();
        int i = segments.indexOf(this);
        if (i == 0) return 0;
        SegmentLayout prevSeg = segments.get(i - 1);
        return prevSeg.getX() + prevSeg.getWidth();
    }
    public double getY() { return y; }
    public double getWidth() {
        List<ElementLayout> allElements = getElements();
        if (allElements.isEmpty()) return 0;
        if (type == SegmentType.START_BARLINE) return 0;

        return allElements.stream()
                .mapToDouble(ElementLayout::getWidth)
                .max()
                .orElse(0) + style.getSegmentLeftMargin();
    }
    public double getHeight() { return height; }
    public boolean hasDynamicWidth() { return getElements().stream().anyMatch(ElementLayout::hasDynamicWidth); }
    public ScoreStyle getScoreStyle() { return style; }
}
