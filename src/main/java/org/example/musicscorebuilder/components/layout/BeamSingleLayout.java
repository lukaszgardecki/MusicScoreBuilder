package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Leland;

public class BeamSingleLayout implements Selectable {
    private final Leland fontData;
    private final ScoreStyle style;
    private final NoteLayout parentNote;
    private boolean selected;

    public BeamSingleLayout(NoteLayout parentNote) {
        this.parentNote = parentNote;
        this.style = parentNote.getScoreStyle();
        this.fontData = parentNote.getStem().getDirection() == StemDirection.UP
                ? Leland.NOTE_FLAG_8TH_UP
                : Leland.NOTE_FLAG_8TH_DOWN;
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return parentNote.getVoice(); }
    @Override
    public boolean contains(double segmentX, double segmentY) {
        double stemWidth = parentNote.getStem().getWidth();
        double x = getX();
        double y = getY();

        double totalWidth = getFontWidth();
        double height = getHeight();

        double startX = x + stemWidth;
        double width = totalWidth - stemWidth;

        boolean hitX = segmentX >= startX && segmentX <= startX + width;
        boolean hitY;

        if (parentNote.getStem().getDirection() == StemDirection.UP) {
            hitY = segmentY >= y && segmentY <= y + height;
        } else {
            hitY = segmentY >= y - height && segmentY <= y;
        }

        return hitX && hitY;
    }
    @Override public SegmentLayout getSegment() { return parentNote.getSegment(); }
    @Override public StaffLayout getStaff() { return parentNote.getStaff(); }

    public double getX() { return parentNote.getStem().getX(); }
    public double getY() { return parentNote.getStem().getEndY(); }
    public double getBoxY() {return parentNote.getStem().getDirection() == StemDirection.UP ? getY() : getY() - getHeight(); }
    public double getHeight() {
        double heightDiff = parentNote.getStem().getDirection() == StemDirection.UP
                ? 0.75 * style.getStaffLineSpacing()
                : style.getStaffLineSpacing();
        return getFontSize() - heightDiff;
    }

    public double getFontWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return fontData.getCode(); }
    public ScoreStyle getScoreStyle() { return style; }
}