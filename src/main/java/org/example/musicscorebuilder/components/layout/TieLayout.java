package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.SegmentType;

public class TieLayout implements Selectable {
    private final NoteLayout startNote;
    private final NoteLayout endNote;
    private final SystemLayout system;
    private boolean selected;

    public TieLayout(SystemLayout system, NoteLayout startNote, NoteLayout endNote) {
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

        double strokePadding = getScoreStyle().getBowTipRoundingFactor() / 2.0;

        if (x < geom.getStartX() - strokePadding || x > geom.getEndX() + strokePadding) {
            return false;
        }

        double targetX = Math.max(geom.getStartX(), Math.min(geom.getEndX(), x));
        double t = geom.findTForX(targetX);

        double yOuter = geom.calculateBezierCoordinate(t, geom.getStartY(), geom.getCp1yOuter(), geom.getCp2yOuter(), geom.getEndY());
        double yInner = geom.calculateBezierCoordinate(t, geom.getStartY(), geom.getCp1yInner(), geom.getCp2yInner(), geom.getEndY());

        double minY = Math.min(yOuter, yInner) - strokePadding;
        double maxY = Math.max(yOuter, yInner) + strokePadding;

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
                isCurveUp(), getScoreStyle()
        );
    }

    public boolean isCurveUp() {
        NoteLayout ref = startNote != null ? startNote : endNote;
        if (isPolyphonic(ref)) {
            return ref.getVoice() % 2 == 1;
        }

        Boolean isStem1Up = (startNote != null && startNote.getStem() != null) ? startNote.getStem().isUp() : null;
        Boolean isStem2Up = (endNote != null && endNote.getStem() != null) ? endNote.getStem().isUp() : null;

        if (Boolean.TRUE.equals(isStem1Up) && Boolean.TRUE.equals(isStem2Up)) return false;
        if (Boolean.FALSE.equals(isStem1Up) && Boolean.FALSE.equals(isStem2Up)) return true;

        if (isStem1Up != null) return !isStem1Up;
        if (isStem2Up != null) return !isStem2Up;
        return true;
    }

    private boolean isPolyphonic(NoteLayout note) {
        var segment = note.getSegment();
        if (segment.getType() != SegmentType.NOTEREST) return false;
        return segment.getVoiceCountForStaff(note.getStaff()) > 1;
    }

    public double getStartX() {
        if (startNote == null) {
            if (system != null && !system.getMeasures().isEmpty()) {
                MeasureLayout firstMeasure = system.getMeasures().getFirst();
                double measureX = firstMeasure.getX();

                double segmentX = firstMeasure.getSegments().stream()
                        .filter(s -> s.getType() == SegmentType.NOTEREST)
                        .findFirst()
                        .map(SegmentLayout::getX)
                        .orElse(0.0);

                return measureX + segmentX;
            }
            return 0.0;
        }

        MeasureLayout measure = startNote.getSegment().getParent();
        double absoluteNoteX = measure.getX() + startNote.getSegment().getX() + startNote.getX();
        double headWidth = startNote.getFontWidth();
        double correctedX = headWidth * ((startNote.getStem() != null && startNote.getStem().isUp() && isCurveUp()) ? 1.0 : 0.5);
        return absoluteNoteX + correctedX + getScoreStyle().getBowXNoteSpace();
    }

    public double getEndX() {
        if (endNote == null) return system.getWidth() - getScoreStyle().getBowSystemBreakEndXMargin();

        MeasureLayout measure = endNote.getSegment().getParent();
        double absoluteNoteX = measure.getX() + endNote.getSegment().getX() + endNote.getX();
        double headWidth = endNote.getFontWidth();
        double correctedX = headWidth * ((endNote.getStem() != null && endNote.getStem().isDown() && !isCurveUp()) ? 0.0 : 0.5);
        return absoluteNoteX + correctedX - getScoreStyle().getBowXNoteSpace();
    }

    public double getStartY() { return calculateY(startNote, endNote); }
    public double getEndY() { return calculateY(endNote, startNote); }

    private double calculateY(NoteLayout primaryNote, NoteLayout secondaryNote) {
        NoteLayout targetNote = primaryNote != null ? primaryNote : secondaryNote;
        double noteCenterY = targetNote.getY();
        double halfSpacing = targetNote.getScoreStyle().getStaffLineSpacing() / 2.0 ;
        double verticalMargin = halfSpacing + getScoreStyle().getBowYNoteSpace();
        return noteCenterY + (isCurveUp() ? -verticalMargin : verticalMargin);
    }
}