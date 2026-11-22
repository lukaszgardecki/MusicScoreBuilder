package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Staff;

import java.util.List;

public class StaffLayout {
    private final MeasureLayout parent;
    private final Staff staff;
    private final double lineSpacing;
    private final double lineWidth;
    private double spaceBelow;
    private double x = 0, y;
    private double systemNoteBodyBottomOverflow = 0.0;

    private boolean overflowCalculated = false;
    private double cachedTopOverflow = 0.0;
    private double cachedNoteBodyOverflow = 0.0;
    private double cachedLyricOverflow = 0.0;

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

    public void setY(double y) {
        this.y = y;
        overflowCalculated = false;
    }
    public void setSpaceBelow(double spaceBelow) { this.spaceBelow = spaceBelow; }

    public double getSystemNoteBodyBottomOverflow() {
        return systemNoteBodyBottomOverflow;
    }

    public void setSystemNoteBodyBottomOverflow(double systemNoteBodyBottomOverflow) {
        this.systemNoteBodyBottomOverflow = systemNoteBodyBottomOverflow;
    }

    private void calculateOverflows() {

        if (overflowCalculated) {
            return;
        }

        if (parent == null) {
            cachedTopOverflow = 0.0;
            cachedNoteBodyOverflow = 0.0;
            cachedLyricOverflow = 0.0;
            overflowCalculated = true;
            return;
        }

        double minTopY = Double.POSITIVE_INFINITY;

        double staffHeight = getHeight();
        double maxNoteBodyRelY = staffHeight;
        double maxLyricRelY = staffHeight;

        for (SegmentLayout segment : parent.getSegments()) {

            for (ElementLayout element : segment.getElements()) {

                if (element.getStaff() != this) {
                    continue;
                }

                double elementTopY =
                        (element instanceof NoteLayout note)
                                ? note.getMinY()
                                : element.getBoxY();

                if (elementTopY < minTopY) {
                    minTopY = elementTopY;
                }

                if (element instanceof NoteLayout note) {

                    double noteBodyRelY =
                            note.getBodyBottomY() - y;

                    if (noteBodyRelY > maxNoteBodyRelY) {
                        maxNoteBodyRelY = noteBodyRelY;
                    }

                    List<LyricLayout> lyrics = note.getLyrics();

                    if (lyrics != null) {

                        for (LyricLayout lyric : lyrics) {

                            double lyricBottomRelY =
                                    lyric.getRelY()
                                            + (lyric.getFontSize() > 0
                                            ? lyric.getFontSize() * 1.2
                                            : 10.0);

                            if (lyricBottomRelY > maxLyricRelY) {
                                maxLyricRelY = lyricBottomRelY;
                            }
                        }
                    }
                }
            }
        }

        if (minTopY == Double.POSITIVE_INFINITY) {
            cachedTopOverflow = 0.0;
        } else {

            double relY = minTopY - y;

            cachedTopOverflow =
                    relY < 0.0
                            ? -relY
                            : 0.0;
        }

        cachedNoteBodyOverflow =
                Math.max(0.0, maxNoteBodyRelY - staffHeight);

        cachedLyricOverflow =
                Math.max(0.0, maxLyricRelY - staffHeight);

        overflowCalculated = true;
    }

    public double getTopOverflow() {
        calculateOverflows();
        return cachedTopOverflow;
    }

    public double getNoteBodyBottomOverflow() {
        calculateOverflows();
        return cachedNoteBodyOverflow;
    }

    public double getLyricBottomOverflow() {
        calculateOverflows();
        return cachedLyricOverflow;
    }

//    public double getTopOverflow() {
//        if (parent == null) return 0.0;
//        double minTopY = Double.POSITIVE_INFINITY;
//
//        for (SegmentLayout segment : parent.getSegments()) {
//            for (ElementLayout element : segment.getElements()) {
//                if (element.getStaff() == this) {
//                    double elementTopY = (element instanceof NoteLayout n) ? n.getMinY() : element.getBoxY();
//                    if (elementTopY < minTopY) {
//                        minTopY = elementTopY;
//                    }
//                }
//            }
//        }
//
//        if (minTopY == Double.POSITIVE_INFINITY) return 0.0;
//
//        double relY = minTopY - this.getY();
//        if (relY < 0.0) {
//            return Math.abs(relY);
//        }
//        return 0.0;
//    }
//
//    public double getNoteBodyBottomOverflow() {
//        if (parent == null) return 0.0;
//        double staffHeight = getHeight();
//        double maxRelY = staffHeight;
//
//        for (SegmentLayout segment : parent.getSegments()) {
//            for (ElementLayout element : segment.getElements()) {
//                if (element.getStaff() == this && element instanceof NoteLayout note) {
//                    double noteBodyAbsY = note.getBodyBottomY();
//                    double relY = noteBodyAbsY - this.getY();
//                    if (relY > maxRelY) {
//                        maxRelY = relY;
//                    }
//                }
//            }
//        }
//
//        return Math.max(0.0, maxRelY - staffHeight);
//    }
//
//    public double getLyricBottomOverflow() {
//        if (parent == null) return 0.0;
//        double staffHeight = getHeight();
//        double maxRelY = staffHeight;
//
//        for (SegmentLayout segment : parent.getSegments()) {
//            for (ElementLayout element : segment.getElements()) {
//                if (element.getStaff() == this && element instanceof NoteLayout note) {
//                    List<LyricLayout> lyrics = note.getLyrics();
//                    if (lyrics != null) {
//                        for (LyricLayout lyric : lyrics) {
//                            double lyricRelTop = lyric.getRelY();
//                            double fontSize = lyric.getFontSize();
//                            double lyricBottomRelY = lyricRelTop + (fontSize > 0 ? fontSize * 1.2 : 10.0);
//                            if (lyricBottomRelY > maxRelY) {
//                                maxRelY = lyricBottomRelY;
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//        return Math.max(0.0, maxRelY - staffHeight);
//    }
}