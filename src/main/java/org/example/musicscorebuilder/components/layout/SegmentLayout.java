package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.layout.util.NoteCollisionResolver;
import org.example.musicscorebuilder.components.music.*;
import java.util.*;

public class SegmentLayout {
    private final ScoreStyle style;
    private final MeasureLayout parent;
    private final Map<StaffLayout, List<ElementLayout>> elementsByStaff = new HashMap<>();
    private SegmentType type;
    private double x, y = 0, height;
    private double extraWidth = 0.0;
    private boolean highlighted = false;
    private boolean systemGenerated = false;

    public SegmentLayout(SegmentType type, MeasureLayout parent) {
        this.style = parent.getScoreStyle();
        this.parent = parent;
        this.type = type;
        this.height = parent.getHeight() - style.getStaffLineWidth();
        for (StaffLayout staffLayout : parent.getStaffs()) {
            elementsByStaff.put(staffLayout, new ArrayList<>());
        }
    }

    public void addByStaff(StaffLayout staffLayout, ElementLayout elementLayout) {
        elementsByStaff.computeIfAbsent(staffLayout, k -> new ArrayList<>()).add(elementLayout);

        if (type == SegmentType.NOTEREST && elementLayout instanceof NoteLayout) {
            resolveCollisionsForStaff(staffLayout);
        }
    }

    public void addStartBarline(Barline startBarline) {
        elementsByStaff.forEach((staff, elements) ->
                elements.add(new BarlineLayout(startBarline, staff, this))
        );
    }

    public void addClef() {
        elementsByStaff.forEach((staff, elements) ->
                elements.add(new ClefLayout(staff.getStaff().getDefaultClef(), staff, this))
        );
    }

    public void addKeySignature(KeySignature keySignature) {
        elementsByStaff.forEach((staff, elements) ->
                elements.add(new KeySigLayout(keySignature, staff, this))
        );
    }

    public void addTimeSignature(TimeSignature timeSignature) {
        elementsByStaff.forEach((staff, elements) ->
                elements.add(new TimeSigLayout(timeSignature, staff, this))
        );
    }

    public void resolveCollisions() {
        if (type != SegmentType.NOTEREST) return;
        for (StaffLayout staffLayout : elementsByStaff.keySet()) {
            resolveCollisionsForStaff(staffLayout);
        }
    }

    private void resolveCollisionsForStaff(StaffLayout staffLayout) {
        if (type != SegmentType.NOTEREST) return;

        List<ElementLayout> elements = elementsByStaff.get(staffLayout);
        if (elements == null) return;

        List<NoteLayout> notes = elements.stream()
                .filter(NoteLayout.class::isInstance)
                .map(NoteLayout.class::cast)
                .toList();

        NoteCollisionResolver.resolve(notes);
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
    public int getVoiceCountForStaff(StaffLayout staff) {
        return parent.getVoiceCountForStaff(staff);
    }

    public SegmentType getType() { return type; }
    public double getX() {
        var segments = parent.getSegments();
        int i = segments.indexOf(this);
        if (i <= 0) return 0;
        SegmentLayout prevSeg = segments.get(i - 1);
        return prevSeg.getX() + prevSeg.getWidth();
    }
    public double getY() { return y; }
    public double getWidth() {
        List<ElementLayout> allElements = getElements();
        if (allElements.isEmpty()) return 0;

        double maxRight = allElements.stream()
                .mapToDouble(e -> e.getX() + e.getWidth())
                .max()
                .orElse(0);

        var margin = type == SegmentType.END_BARLINE ? 0 : style.getSegmentRightMargin();
        return maxRight + margin + extraWidth;
    }
    public double getHeight() { return height; }
    public boolean hasDynamicWidth() { return getElements().stream().anyMatch(ElementLayout::hasDynamicWidth); }
    public ScoreStyle getScoreStyle() { return style; }
    public boolean isHighlighted() { return highlighted; }
    public boolean isSystemGenerated() { return systemGenerated; }

    public void setX(double x) { this.x = x; }
    public void setExtraWidth(double extraWidth) {
        this.extraWidth = extraWidth;
    }
    public void setType(SegmentType type) { this.type = type; }
    public void setHighlighted(boolean highlighted) { this.highlighted = highlighted; }
    public void setSystemGenerated(boolean systemGenerated) { this.systemGenerated = systemGenerated; }
}