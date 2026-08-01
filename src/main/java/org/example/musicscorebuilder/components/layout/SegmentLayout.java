package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.edit.CursorLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.layout.util.NoteCollisionResolver;
import org.example.musicscorebuilder.components.music.*;
import java.util.*;

public class SegmentLayout {
    private Segment segment;
    private final ScoreStyle style;
    private SegmentLayout next;
    private SegmentLayout prev;
    private final MeasureLayout parent;
    private final Map<StaffLayout, List<ElementLayout>> staffElements = new HashMap<>();
    private SegmentType type;
    private double x, y = 0, height;
    private double extraWidth = 0.0;
    private CursorLayout cursorLayout;
    private boolean systemGenerated = false;

    public SegmentLayout(Segment segment, MeasureLayout parent) {
        this(segment.getType(), parent);
        this.segment = segment;
    }

    public SegmentLayout(SegmentType type, MeasureLayout parent) {
        this.style = parent.getScoreStyle();
        this.parent = parent;
        this.type = type;
        this.height = parent.getHeight() - style.getStaffLineWidth();
        for (StaffLayout staffLayout : parent.getStaffs()) {
            staffElements.put(staffLayout, new ArrayList<>());
        }
    }

    public void addByStaff(StaffLayout staffLayout, ElementLayout elementLayout) {
        staffElements.computeIfAbsent(staffLayout, k -> new ArrayList<>()).add(elementLayout);

        if (type == SegmentType.NOTEREST && elementLayout instanceof NoteLayout) {
            resolveCollisionsForStaff(staffLayout);
        }
    }

    public void addStartBarline(Barline startBarline) {
        staffElements.forEach((staff, elements) ->
                elements.add(new BarlineLayout(startBarline, staff, this))
        );
    }

    public void addClef() {
        staffElements.forEach((staff, elements) ->
                elements.add(new ClefLayout(staff.getStaff().getDefaultClef(), staff, this))
        );
    }

    public void addKeySignature(KeySignature keySignature) {
        staffElements.forEach((staff, elements) ->
                elements.add(new KeySigLayout(keySignature, staff, this))
        );
    }

    public void addTimeSignature(TimeSignature timeSignature) {
        staffElements.forEach((staff, elements) ->
                elements.add(new TimeSigLayout(timeSignature, staff, this))
        );
    }

    public void resolveCollisions() {
        if (type != SegmentType.NOTEREST) return;
        for (StaffLayout staffLayout : staffElements.keySet()) {
            resolveCollisionsForStaff(staffLayout);
        }
    }

    private void resolveCollisionsForStaff(StaffLayout staffLayout) {
        if (type != SegmentType.NOTEREST) return;

        List<ElementLayout> elements = staffElements.get(staffLayout);
        if (elements == null) return;

        List<NoteLayout> notes = elements.stream()
                .filter(NoteLayout.class::isInstance)
                .map(NoteLayout.class::cast)
                .toList();

        NoteCollisionResolver.resolve(notes);
    }

    public List<ElementLayout> getElementsByStaff(StaffLayout staffLayout) {
        return staffElements.getOrDefault(staffLayout, Collections.emptyList());
    }

    public Map<StaffLayout, List<ElementLayout>> getStaffElements() {
        return staffElements;
    }

    public List<ElementLayout> getElements() {
        return staffElements.values().stream()
                .flatMap(List::stream)
                .toList();
    }
    public int getVoiceCountForStaff(StaffLayout staff) {
        return parent.getVoiceCountForStaff(staff);
    }

    public boolean hasAnyNoteRestAtStaffByVoice(StaffLayout staff, int voice) {
        return !segment.getNoteRestByStaffAndVoice(staff.getStaff(), voice).isEmpty();
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

        double maxElementWidth = allElements.stream()
                .mapToDouble(ElementLayout::getWidth)
                .max()
                .orElse(0);

        var margin = switch(type) {
            case CLEF -> style.getSegmentClefRightMargin();
            case START_BARLINE -> style.getSegmentStartBarlineRightMargin();
            case BARLINE -> style.getSegmentBarlineRightMargin();
            case END_BARLINE -> style.getSegmentEndBarlineRightMargin();
            case NOTEREST -> style.getSegmentNoteRestRightMargin();
            case KEY_SIG -> style.getSegmentKeySigRightMargin();
            case TIME_SIG -> style.getSegmentTimeSigRightMargin();
        };
        double f = type == SegmentType.NOTEREST ? calculateNoteRestWidthFactor() : 1.0;
        return maxElementWidth + margin * f + extraWidth;
    }
    public double getHeight() { return height; }
    public boolean hasDynamicWidth() { return getElements().stream().anyMatch(ElementLayout::hasDynamicWidth); }
    public MeasureLayout getParent() { return parent; }
    public ScoreStyle getScoreStyle() { return style; }
    public boolean isSystemGenerated() { return systemGenerated; }
    public CursorLayout getCursor() { return cursorLayout; }
    public boolean hasActiveCursor() { return cursorLayout != null; }
    public Segment getSegment() { return segment; }
    public SegmentLayout getNext() { return next; }
    public SegmentLayout getPrev() { return prev; }
    public SegmentLayout getNextSameType() {
        SegmentLayout current = this.next;
        while (current != null) {
            if (current.getType() == this.type) {
                return current;
            }
            current = current.next;
        }
        return null;
    }

    public SegmentLayout getPrevSameType() {
        SegmentLayout current = this.prev;
        while (current != null) {
            if (current.getType() == this.type) {
                return current;
            }
            current = current.prev;
        }
        return null;
    }


    public void setX(double x) { this.x = x; }
    public void setExtraWidth(double extraWidth) {
        this.extraWidth = extraWidth;
    }
    public void setType(SegmentType type) { this.type = type; }
    public void setCursor(CursorLayout cursor) { this.cursorLayout = cursor; }
    public void setSystemGenerated(boolean systemGenerated) { this.systemGenerated = systemGenerated; }
    public void setNext(SegmentLayout next) { this.next = next; }
    public void setPrev(SegmentLayout prev) { this.prev = prev; }

    private double calculateNoteRestWidthFactor() {
        Optional<NoteType> shortestNoteType = getElements().stream()
                .map(e -> switch (e) {
                    case NoteLayout nl -> nl.getNote().getType();
                    case RestLayout rl -> rl.getRest().getType();
                    default -> null;
                })
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(NoteType::getTicks));

        return shortestNoteType.map(noteType -> switch (noteType) {
            case WHOLE -> 8.0;
            case HALF -> 4.0;
            case QUARTER -> 2.0;
            case EIGHTH -> 1.0;
            case SIXTEENTH -> 0.75;
            case THIRTY_SECOND -> 0.5;
        }).orElse(1.0);
    }
}