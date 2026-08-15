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

        List<NoteLayout> notes = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++) {
            ElementLayout el = elements.get(i);
            if (el instanceof NoteLayout nl) {
                notes.add(nl);
            }
        }

        NoteCollisionResolver.resolve(notes);
    }

    public List<ElementLayout> getElementsByStaff(StaffLayout staffLayout) {
        return staffElements.getOrDefault(staffLayout, Collections.emptyList());
    }

    public Map<StaffLayout, List<ElementLayout>> getStaffElements() {
        return staffElements;
    }

    public List<ElementLayout> getElements() {
        List<ElementLayout> all = new ArrayList<>();
        for (List<ElementLayout> list : staffElements.values()) {
            if (list != null) {
                all.addAll(list);
            }
        }
        return all;
    }

    public int getVoiceCountForStaff(int staffId) {
        return parent.getVoiceCountForStaff(staffId);
    }

    public boolean hasAnyNoteRestAtStaffByVoice(int staffId, int voice) {
        return segment != null && !segment.getNoteRestByStaffAndVoice(staffId, voice).isEmpty();
    }

    public SegmentType getType() { return type; }

    public double getX() {
        List<SegmentLayout> segments = parent.getSegments();
        if (segments == null) return 0.0;

        double currentX = 0.0;
        for (int i = 0; i < segments.size(); i++) {
            SegmentLayout seg = segments.get(i);
            if (seg == this) {
                return currentX;
            }
            currentX += seg.getWidth();
        }
        return 0.0;
    }

    public double getY() { return y; }

    public double getMarginLeft() {
        if (type != SegmentType.NOTEREST) {
            return 0.0;
        }

        double minMargin = style.getSegmentNoteRestLeftMargin();
        double maxAccidentalSpace = 0.0;
        double maxLyricLeftSpace = 0.0;

        for (List<ElementLayout> elements : staffElements.values()) {
            for (int i = 0; i < elements.size(); i++) {
                ElementLayout el = elements.get(i);
                if (el instanceof NoteLayout nl) {
                    AccidentalLayout acc = nl.getAccidental();
                    if (acc != null && acc.isVisible()) {
                        double accSpace = acc.getWidth() + style.getNoteAccSpacing();
                        if (accSpace > maxAccidentalSpace) {
                            maxAccidentalSpace = accSpace;
                        }
                    }

                    double headWidth = nl.getFontWidth();
                    List<LyricLayout> lyrics = nl.getLyrics();
                    if (!lyrics.isEmpty()) {
                        double maxLyricWidth = 0.0;
                        for (int j = 0; j < lyrics.size(); j++) {
                            double lyricWidth = lyrics.get(j).getTotalWidth();
                            if (lyricWidth > maxLyricWidth) {
                                maxLyricWidth = lyricWidth;
                            }
                        }
                        double leftOverhang = (maxLyricWidth - headWidth) / 2.0;
                        double lyricSpace = Math.max(0.0, leftOverhang - nl.getXOffset());
                        if (lyricSpace > maxLyricLeftSpace) {
                            maxLyricLeftSpace = lyricSpace;
                        }
                    }
                }
            }
        }

        return Math.max(minMargin, Math.max(maxAccidentalSpace, maxLyricLeftSpace));
    }

    public double getWidth() {
        boolean empty = true;
        for (List<ElementLayout> elements : staffElements.values()) {
            if (!elements.isEmpty()) {
                empty = false;
                break;
            }
        }
        if (empty) return 0.0;

        double marginLeft = getMarginLeft();
        double maxContentWidth = 0.0;

        for (List<ElementLayout> elements : staffElements.values()) {
            for (int i = 0; i < elements.size(); i++) {
                ElementLayout el = elements.get(i);
                double width;
                if (el instanceof NoteLayout nl) {
                    double noteExtent = (nl.getX() - marginLeft) + nl.getWidth();
                    double noteCenterXRel = (nl.getX() - marginLeft) + (nl.getFontWidth() / 2.0);

                    double maxLyricExtent = 0.0;
                    List<LyricLayout> lyrics = nl.getLyrics();
                    if (!lyrics.isEmpty()) {
                        for (int j = 0; j < lyrics.size(); j++) {
                            double lyricExtent = noteCenterXRel + (lyrics.get(j).getTotalWidth() / 2.0);
                            if (lyricExtent > maxLyricExtent) {
                                maxLyricExtent = lyricExtent;
                            }
                        }
                    }
                    width = Math.max(noteExtent, maxLyricExtent);
                } else {
                    width = el.getWidth();
                }

                if (width > maxContentWidth) {
                    maxContentWidth = width;
                }
            }
        }

        var marginRight = switch(type) {
            case CLEF -> style.getSegmentClefRightMargin();
            case START_BARLINE -> style.getSegmentStartBarlineRightMargin();
            case BARLINE -> style.getSegmentBarlineRightMargin();
            case END_BARLINE -> style.getSegmentEndBarlineRightMargin();
            case NOTEREST -> style.getSegmentNoteRestRightMargin();
            case KEY_SIG -> style.getSegmentKeySigRightMargin();
            case TIME_SIG -> style.getSegmentTimeSigRightMargin();
        };
        double f = type == SegmentType.NOTEREST ? calculateNoteRestWidthFactor() : 1.0;
        return marginLeft + maxContentWidth + (marginRight * f) + extraWidth;
    }

    public double getHeight() { return height; }

    public boolean hasDynamicWidth() {
        for (List<ElementLayout> elements : staffElements.values()) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).hasDynamicWidth()) return true;
            }
        }
        return false;
    }

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
    public void setExtraWidth(double extraWidth) { this.extraWidth = extraWidth; }
    public void setType(SegmentType type) { this.type = type; }
    public void setCursor(CursorLayout cursor) { this.cursorLayout = cursor; }
    public void setSystemGenerated(boolean systemGenerated) { this.systemGenerated = systemGenerated; }
    public void setNext(SegmentLayout next) { this.next = next; }
    public void setPrev(SegmentLayout prev) { this.prev = prev; }

    private double calculateNoteRestWidthFactor() {
        NoteType shortest = null;

        for (List<ElementLayout> elements : staffElements.values()) {
            for (int i = 0; i < elements.size(); i++) {
                ElementLayout e = elements.get(i);
                NoteType nt = null;
                if (e instanceof NoteLayout nl && nl.getNote() != null) {
                    nt = nl.getNote().getType();
                } else if (e instanceof RestLayout rl && rl.getRest() != null) {
                    nt = rl.getRest().getType();
                }
                if (nt != null) {
                    if (shortest == null || nt.getTicks() < shortest.getTicks()) {
                        shortest = nt;
                    }
                }
            }
        }

        if (shortest == null) return 1.0;

        return switch (shortest) {
            case WHOLE -> 1.0;
            case HALF -> 0.5;
            case QUARTER -> 0.25;
            case EIGHTH -> 0.125;
            case SIXTEENTH -> 0.06;
            case THIRTY_SECOND -> 0.03;
        };
    }
}