package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class NoteLayout extends ElementLayout {
    private final Leland fontData;
    private final Note note;
    private final StemLayout stem;
    private final BeamSingleLayout singleBeam;
    private final List<DotLayout> dots = new ArrayList<>();
    private BeamGroupLayout beamGroup;
    private AccidentalLayout accidental;
    private double xOffset = 0.0;

    public record LedgerLine(double startX, double endX, double y, double thickness) {}

    public NoteLayout(Note note, StaffLayout staff, SegmentLayout parent) {
        super(true, parent, staff);
        this.note = note;
        this.fontData = switch (note.getType()) {
            case HALF -> Leland.NOTE_HEAD_HALF;
            case WHOLE -> Leland.NOTE_HEAD_WHOLE;
            default -> Leland.NOTE_HEAD_BLACK;
        };
        this.stem = note.getType() == NoteType.WHOLE ? null : new StemLayout(this);
        this.singleBeam = !note.isBeamed() && note.getType().hasFlag() ? new BeamSingleLayout(this) : null;
        refresh();
    }

    @Override public double getX() { return xOffset + parent.getMarginLeft(); }
    @Override
    public double getY() {
        Clef clef = staff.getStaff().getDefaultClef();
        return calculateY(clef) + staff.getY();
    }
    @Override public double getBoxY() { return getY() - (0.5 * style.getStaffLineSpacing()); }
    @Override public double getWidth() {
        var headWidth = getFontWidth();
        var flagWidth = getBeamSingle() == null ? 0 : getStem().isUp() ? getBeamSingle().getFontWidth() : 0;
        double dotsExtent = dots.isEmpty() ? 0 : (dots.getLast().getX() + dots.getLast().getWidth());
        return Math.max(headWidth + flagWidth, dotsExtent);
    }
    @Override public double getHeight() { return style.getStaffLineSpacing(); }
    @Override public int getVoice() { return note.getVoice(); }
    @Override
    public boolean contains(double x, double y) {
        double noteMinX = getBoxX();
        double noteMaxX = noteMinX + getBoxWidth();
        double noteMinY = getBoxY();
        double noteMaxY = noteMinY + getHeight();
        return x >= noteMinX && x <= noteMaxX && y >= noteMinY && y <= noteMaxY;
    }

    public Note getNote() { return note; }
    public double getBoxX() { return getX(); }
    public double getFontWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getBoxWidth() { return getFontWidth(); }
    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return fontData.getCode(); }
    public List<DotLayout> getDots() { return dots; }
    public int getDiatonicStep() { return note.getPitch().getAbsoluteDiatonicStep(); }
    public StemLayout getStem() { return stem; }
    public BeamSingleLayout getBeamSingle() { return singleBeam; }
    public BeamGroupLayout getBeamGroup() { return beamGroup; }
    public AccidentalLayout getAccidental() { return accidental; }

    public List<LedgerLine> getLedgerLines() {
        List<LedgerLine> lines = new ArrayList<>();
        double spacing = style.getStaffLineSpacing();
        double topLineY = staff.getY();
        double bottomLineY = staff.getY() + (4 * spacing);
        double lengthFactor = style.getNoteLedgerLengthFactor();
        double thickness = style.getNoteLedgerLineThickness();
        double boxX = getBoxX();
        double boxWidth = getBoxWidth();
        double centerX = boxX + (boxWidth / 2.0);
        double targetWidth = boxWidth * lengthFactor;
        double startX = centerX - (targetWidth / 2.0);
        double endX = centerX + (targetWidth / 2.0);
        double currentY = getY();

        if (currentY < topLineY - (0.25 * spacing)) {
            double currentLineY = topLineY - spacing;
            while (currentLineY >= currentY - (0.25 * spacing)) {
                lines.add(new LedgerLine(startX, endX, currentLineY, thickness));
                currentLineY -= spacing;
            }
        } else if (currentY > bottomLineY + (0.25 * spacing)) {
            double currentLineY = bottomLineY + spacing;
            while (currentLineY <= currentY + (0.25 * spacing)) {
                lines.add(new LedgerLine(startX, endX, currentLineY, thickness));
                currentLineY += spacing;
            }
        }

        return lines;
    }

    public void setXOffset(double xOffset) { this.xOffset = xOffset; }
    public void setBeamGroup(BeamGroupLayout beamGroup) { this.beamGroup = beamGroup; }

    public void refresh() {
        Clef clef = staff.getStaff().getDefaultClef();
        calculateDots(clef);
        this.accidental = (this.note != null && this.note.getPitch() != null) ? new AccidentalLayout(this) : null;
    }

    public void updatePitchFromY(double newY) {
        if (parent == null) return;

        Clef clef = staff.getStaff().getDefaultClef();
        ClefType clefType = clef.getType();
        double spacing = style.getStaffLineSpacing();
        double topLineY = staff.getY();
        double bottomLineY = staff.getY() + (4 * spacing);
        int ledgersLimit = style.getNoteMaxLedgerLines();
        double minAllowedY = topLineY - (ledgersLimit * spacing);
        double maxAllowedY = bottomLineY + (ledgersLimit * spacing);

        double clampedY = Math.max(minAllowedY, Math.min(maxAllowedY, newY));
        double relativeY = clampedY - staff.getY();
        double halfSpacing = 0.5 * spacing;

        double referenceY = clefType.getOffsetY() * style.getStaffLineSpacing();
        int stepDifference = (int) Math.round((referenceY - relativeY) / halfSpacing);
        int targetDiatonicStep = stepDifference + clefType.getDiatonicStep();

        int currentDiatonicStep = this.note.getOctave() * 7 + this.note.getStep().ordinal();
        if (targetDiatonicStep == currentDiatonicStep) return;

        int octave = targetDiatonicStep / 7;
        int stepValue = targetDiatonicStep % 7;

        if (stepValue < 0) {
            stepValue += 7;
            octave -= 1;
        }

        PitchStep newStep = PitchStep.values()[stepValue];

        Segment segment = parent.getSegment();
        Measure measure = (segment != null) ? segment.getParent() : null;

        int effectiveAlter = measure != null
                ? measure.getEffectiveAlterBefore(segment, staff.getStaffIndex(), newStep, octave)
                : 0;

        this.note.setPitch(newStep, octave);
        if (this.note.getPitch() != null) {
            this.note.getPitch().setAlter(effectiveAlter);
        }

        refreshMeasureAccidentals();
        parent.resolveCollisions();
    }

    public void refreshMeasureAccidentals() {
        if (parent != null && parent.getParent() instanceof MeasureLayout measureLayout) {
            for (SegmentLayout segLayout : measureLayout.getSegments()) {
                for (ElementLayout element : segLayout.getElements()) {
                    if (element instanceof NoteLayout noteLayout) {
                        noteLayout.refresh();
                    }
                }
            }
        } else {
            refresh();
        }
    }

    private double calculateY(Clef clef) {
        ClefType clefType = clef.getType();
        int stepDifference = note.getPitch().getAbsoluteDiatonicStep() - clefType.getDiatonicStep();
        double referenceY = clefType.getOffsetY() * style.getStaffLineSpacing();
        double halfSpacing = 0.5 * style.getStaffLineSpacing();
        return referenceY - (stepDifference * halfSpacing);
    }

    private void calculateDots(Clef clef) {
        dots.clear();
        if (note.getDots() <= 0) return;

        ClefType clefType = clef.getType();
        int stepDifference = note.getPitch().getAbsoluteDiatonicStep() - clefType.getDiatonicStep();

        double spacing = style.getStaffLineSpacing();
        double halfSpacing = 0.5 * spacing;

        boolean isOnLine = (stepDifference % 2 == 0);
        double relY = isOnLine ? -halfSpacing : 0.0;

        double headWidth = getFontWidth();
        double startRelX = headWidth + style.getNoteDotMargin();

        for (int i = 0; i < note.getDots(); i++) {
            double dotRelX = startRelX + (i * style.getNoteDotSpacing());
            dots.add(new DotLayout(this, dotRelX, relY));
        }
    }
}