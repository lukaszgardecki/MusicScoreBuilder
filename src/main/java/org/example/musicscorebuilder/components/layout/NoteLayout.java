package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.List;

public class NoteLayout extends ElementLayout {
    private final Leland fontData;
    private final Note note;
    private final StaffLayout staff;
    private final StemLayout stem;
    private double y;
    private double xOffset = 0.0;

    public record LedgerLine(double startX, double endX, double y, double thickness) {}

    public NoteLayout(Note note, StaffLayout staff, SegmentLayout segmentLayout) {
        super(true, segmentLayout);
        this.note = note;
        this.staff = staff;
        this.fontData = switch (note.getType()) {
            case EIGHTH, QUARTER -> Leland.NOTE_HEAD_BLACK;
            case HALF -> Leland.NOTE_HEAD_HALF;
            case WHOLE -> Leland.NOTE_HEAD_WHOLE;
        };
        Clef clef = staff.getStaff().getDefaultClef();
        this.y = calculateY(clef) + staff.getY();
        this.stem = note.getType() == NoteType.WHOLE ? null : new StemLayout(this);
    }

    public void updatePitchFromY(double newY) {
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

        this.note.setPitch(PitchStep.values()[stepValue], octave);
        this.y = calculateY(clef) + staff.getY();

        parent.resolveCollisions();
    }

    @Override public double getX() { return xOffset; }
    @Override public double getY() { return y; }
    @Override public double getBoxY() { return y - (0.5 * style.getStaffLineSpacing()); }
    @Override public double getWidth() { return getFontWidth(); }
    @Override public double getHeight() { return style.getStaffLineSpacing(); }

    @Override
    public boolean contains(double segmentMusicX, double segmentMusicY) {
        double noteMinX = getBoxX();
        double noteMaxX = noteMinX + getBoxWidth();

        double noteMinY = getBoxY();
        double noteMaxY = noteMinY + getHeight();
        return segmentMusicX >= noteMinX && segmentMusicX <= noteMaxX &&
                segmentMusicY >= noteMinY && segmentMusicY <= noteMaxY;
    }

    public Note getNote() { return note; }
    public double getBoxX() { return getX(); }
    public double getFontWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getBoxWidth() { return getWidth(); }

    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return fontData.getCode(); }
    public int getDiatonicStep() { return note.getPitch().getAbsoluteDiatonicStep(); }
    public StemLayout getStem() { return stem; }
    public StaffLayout getStaffLayout() { return staff; }

    private double calculateY(Clef clef) {
        ClefType clefType = clef.getType();
        int noteDiatonicStep = (note.getOctave() * 7) + note.getStepValue();
        int stepDifference = noteDiatonicStep - clefType.getDiatonicStep();
        double referenceY = clefType.getOffsetY() * style.getStaffLineSpacing();
        double halfSpacing = 0.5 * style.getStaffLineSpacing();
        return referenceY - (stepDifference * halfSpacing);
    }

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

        if (this.y < topLineY - (0.25 * spacing)) {
            double currentLineY = topLineY - spacing;
            while (currentLineY >= this.y - (0.25 * spacing)) {
                lines.add(new LedgerLine(startX, endX, currentLineY, thickness));
                currentLineY -= spacing;
            }
        }
        else if (this.y > bottomLineY + (0.25 * spacing)) {
            double currentLineY = bottomLineY + spacing;
            while (currentLineY <= this.y + (0.25 * spacing)) {
                lines.add(new LedgerLine(startX, endX, currentLineY, thickness));
                currentLineY += spacing;
            }
        }

        return lines;
    }

    public void setXOffset(double xOffset) { this.xOffset = xOffset; }
}
