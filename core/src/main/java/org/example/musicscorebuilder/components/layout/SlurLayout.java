package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.List;

public class SlurLayout implements Selectable {
    private final NoteLayout startNote;
    private final NoteLayout endNote;
    private final SystemLayout system;
    private boolean selected;

    public SlurLayout(SystemLayout system, NoteLayout startNote, NoteLayout endNote) {
        this.system = system;
        this.startNote = startNote;
        this.endNote = endNote;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return startNote != null ? startNote.getVoice() : endNote.getVoice(); }

    @Override
    public boolean contains(double x, double y) {
        BowCurveGeometry geom = getCurveGeometry();
        if (geom == null || geom.getDx() <= 0) return false;

        ScoreStyle style = getScoreStyle();
        double strokePadding = style.getBowTipRoundingFactor() * 0.5;

        double startX = geom.getStartX();
        double endX = geom.getEndX();

        if (x < startX - strokePadding || x > endX + strokePadding) {
            return false;
        }

        double targetX = Math.max(startX, Math.min(endX, x));
        double t = geom.findTForX(targetX);

        double startY = geom.getStartY();
        double endY = geom.getEndY();

        double yOuter = geom.calculateBezierCoordinate(t, startY, geom.getCp1yOuter(), geom.getCp2yOuter(), endY);
        double yInner = geom.calculateBezierCoordinate(t, startY, geom.getCp1yInner(), geom.getCp2yInner(), endY);

        double minY, maxY;
        if (yOuter < yInner) {
            minY = yOuter - strokePadding;
            maxY = yInner + strokePadding;
        } else {
            minY = yInner - strokePadding;
            maxY = yOuter + strokePadding;
        }

        return y >= minY && y <= maxY;
    }

    @Override public SegmentLayout getSegment() { return startNote != null ? startNote.getSegment() : endNote.getSegment(); }
    @Override public StaffLayout getStaff() { return startNote != null ? startNote.getStaff() : endNote.getStaff(); }

    public NoteLayout getStartNote() { return startNote; }
    public NoteLayout getEndNote() { return endNote; }
    public SystemLayout getSystem() { return system; }
    public ScoreStyle getScoreStyle() { return startNote != null ? startNote.getScoreStyle() : endNote.getScoreStyle(); }

    public BowCurveGeometry getCurveGeometry() {
        return new BowCurveGeometry(
                getStartX(), getStartY(),
                getEndX(), getEndY(),
                isCurveUp(), getScoreStyle(),
                true
        );
    }

    public boolean isCurveUp() {
        NoteLayout ref = startNote != null ? startNote : endNote;
        if (ref != null && isPolyphonic(ref)) {
            return (ref.getVoice() % 2) == 1;
        }

        if (startNote != null) {
            StemLayout stem = startNote.getStem();
            if (stem != null) {
                return !stem.isUp();
            }
        }
        if (endNote != null) {
            StemLayout stem = endNote.getStem();
            if (stem != null) {
                return !stem.isUp();
            }
        }
        return true;
    }

    private boolean isPolyphonic(NoteLayout note) {
        SegmentLayout segment = note.getSegment();
        if (segment == null || segment.getType() != SegmentType.NOTEREST) return false;
        StaffLayout staff = note.getStaff();
        return staff != null && segment.getVoiceCountForStaff(staff.getStaffIndex()) > 1;
    }

    public double getStartX() {
        if (startNote == null) {
            if (system != null) {
                List<MeasureLayout> measures = system.getMeasures();
                if (!measures.isEmpty()) {
                    MeasureLayout firstMeasure = measures.getFirst();
                    double measureX = firstMeasure.getX();

                    double segmentX = 0.0;
                    List<SegmentLayout> segments = firstMeasure.getSegments();
                    int segCount = segments.size();
                    for (int i = 0; i < segCount; i++) {
                        SegmentLayout s = segments.get(i);
                        if (s.getType() == SegmentType.NOTEREST) {
                            segmentX = s.getX();
                            break;
                        }
                    }
                    return measureX + segmentX;
                }
            }
            return 0.0;
        }

        SegmentLayout seg = startNote.getSegment();
        MeasureLayout measure = seg.getParent();
        double absoluteNoteX = measure.getX() + seg.getX() + startNote.getX();
        double headWidth = startNote.getFontWidth();
        StemLayout stem = startNote.getStem();

        boolean isStemUp = stem != null && stem.isUp();
        double correctedX = headWidth * (isStemUp && isCurveUp() ? 1.0 : 0.5);
        return absoluteNoteX + correctedX + getScoreStyle().getBowXNoteSpace();
    }

    public double getEndX() {
        ScoreStyle style = getScoreStyle();
        if (endNote == null) {
            return system != null ? system.getWidth() - style.getBowSystemBreakEndXMargin() : 0.0;
        }

        SegmentLayout seg = endNote.getSegment();
        MeasureLayout measure = seg.getParent();
        double absoluteNoteX = measure.getX() + seg.getX() + endNote.getX();
        double headWidth = endNote.getFontWidth();
        StemLayout stem = endNote.getStem();

        boolean isStemDown = stem != null && stem.isDown();
        double correctedX = headWidth * (isStemDown && !isCurveUp() ? 0.0 : 0.5);
        return absoluteNoteX + correctedX - style.getBowXNoteSpace();
    }

    public double getStartY() { return calculateY(startNote, endNote); }
    public double getEndY() { return calculateY(endNote, startNote); }

    private double calculateY(NoteLayout primaryNote, NoteLayout secondaryNote) {
        NoteLayout targetNote = primaryNote != null ? primaryNote : secondaryNote;
        if (targetNote == null) return 0.0;

        double noteCenterY = targetNote.getY();
        ScoreStyle style = targetNote.getScoreStyle();
        double halfSpacing = style.getStaffLineSpacing() * 0.5;
        double verticalMargin = halfSpacing + style.getBowYNoteSpace();
        return noteCenterY + (isCurveUp() ? -verticalMargin : verticalMargin);
    }
}