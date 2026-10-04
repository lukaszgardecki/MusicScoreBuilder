package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Staff;

public class StaffLayout {
    private final MeasureLayout parent;
    private final Staff staff;
    private final double lineSpacing;
    private final double lineWidth;
    private double spaceBelow;
    private double x = 0, y;
    private double systemNoteBodyBottomOverflow = 0.0;

    public StaffLayout(Staff staff, MeasureLayout parent, ScoreStyle scoreStyle) {
        this.parent = parent;
        this.staff = staff;
        this.lineSpacing = scoreStyle.getStaffLineSpacing();
        this.lineWidth = scoreStyle.getStaffLineWidth();
        boolean isLastOne = staff.getIndex() == parent.getMeasure().getStaves().size() - 1;
        if (isLastOne) this.spaceBelow = 0;
        else this.spaceBelow = scoreStyle.getStaffSpacing();
        y = staff.getIndex() * (getHeight() + scoreStyle.getStaffSpacing());
    }

    public MeasureLayout getParent() { return parent; }
    public Staff getStaff() { return staff; }
    public int getStaffIndex() { return staff.getIndex(); }
    public int getLinesNumber() { return staff.getLinesNumber(); }
    public double getLineWidth() { return lineWidth; }
    public double getLineSpacing() { return lineSpacing; }
    public double getHeight() { return (staff.getLinesNumber() - 1) * lineSpacing + lineWidth; }
    public double getWidth() { return parent.getWidth(); }
    public double getSpaceBelow() { return spaceBelow; }
    public double getX() { return x; }
    public double getY() { return y; }

    public void setY(double y) { this.y = y; }
    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }

    public double getSystemNoteBodyBottomOverflow() {
        return systemNoteBodyBottomOverflow;
    }

    public void setSystemNoteBodyBottomOverflow(double systemNoteBodyBottomOverflow) {
        this.systemNoteBodyBottomOverflow = systemNoteBodyBottomOverflow;
    }

    public double getTopOverflow() {
        if (parent == null) return 0.0;
        double minRelY = 0.0;

        for (SegmentLayout segment : parent.getSegments()) {
            for (ElementLayout element : segment.getElements()) {
                if (element.getStaff() != this) continue;

                double elementTopAbsY = (element instanceof NoteLayout n) ? n.getMinY() : element.getBoxY();
                double relY = elementTopAbsY - this.getY();

                if (relY < minRelY) {
                    minRelY = relY;
                }
            }
        }
        return Math.abs(minRelY);
    }

    public double getBottomOverflow() {
        if (parent == null) return 0.0;
        double staffHeight = getHeight();
        double maxRelY = staffHeight;

        for (SegmentLayout segment : parent.getSegments()) {
            for (ElementLayout element : segment.getElements()) {
                if (element.getStaff() != this) continue;

                if (element instanceof NoteLayout note) {
                    double noteBottomAbsY = note.getBottomY();
                    double relY = noteBottomAbsY - this.getY();
                    if (relY > maxRelY) {
                        maxRelY = relY;
                    }

                    if (note.getLyrics() != null) {
                        for (LyricLayout lyric : note.getLyrics()) {
                            double lyricRelTop = lyric.getRelY();
                            double lyricHeight = lyric.getFontSize() > 0 ? lyric.getFontSize() * 1.1 : 10.0;
                            double lyricBottomRelY = lyricRelTop + lyricHeight;

                            if (lyricBottomRelY > maxRelY) {
                                maxRelY = lyricBottomRelY;
                            }
                        }
                    }
                } else {
                    double elementBottomAbsY = element.getBoxY() + element.getHeight();
                    double relY = elementBottomAbsY - this.getY();
                    if (relY > maxRelY) {
                        maxRelY = relY;
                    }
                }
            }
        }

        return Math.max(0.0, maxRelY - staffHeight);
    }

    public double getNoteBodyBottomOverflow() {
        if (parent == null) return 0.0;
        double staffHeight = getHeight();
        double maxRelY = staffHeight;

        for (SegmentLayout segment : parent.getSegments()) {
            for (ElementLayout element : segment.getElements()) {
                if (element.getStaff() != this) continue;

                if (element instanceof NoteLayout note) {
                    double noteBottomAbsY = note.getBodyBottomY();
                    double relY = noteBottomAbsY - this.getY();

                    if (relY > maxRelY) {
                        maxRelY = relY;
                    }
                }
            }
        }
        return Math.max(0.0, maxRelY - staffHeight);
    }
}