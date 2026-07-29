package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Leland;

public class BeamSingleLayout implements Selectable {
    private final ScoreStyle style;
    private final NoteLayout parentNote;
    private boolean selected;

    public BeamSingleLayout(NoteLayout parentNote) {
        this.parentNote = parentNote;
        this.style = parentNote.getScoreStyle();
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
    public double getBoxY() { return parentNote.getStem().getDirection() == StemDirection.UP ? getY() : getY() - getHeight(); }
    public double getHeight() {
        double heightDiff = parentNote.getStem().getDirection() == StemDirection.UP
                ? 0.75 * style.getStaffLineSpacing()
                : style.getStaffLineSpacing();
        return getFontSize() - heightDiff;
    }
    private Leland getFontData() {
        boolean isDown = parentNote.getStem() != null
                && parentNote.getStem().getDirection() == StemDirection.DOWN;

        return switch (parentNote.getNote().getType()) {
            case EIGHTH -> isDown ? Leland.NOTE_FLAG_8TH_DOWN : Leland.NOTE_FLAG_8TH_UP;
            case SIXTEENTH -> isDown ? Leland.NOTE_FLAG_16TH_DOWN : Leland.NOTE_FLAG_16TH_UP;
            case THIRTY_SECOND -> isDown ? Leland.NOTE_FLAG_32ND_DOWN : Leland.NOTE_FLAG_32ND_UP;
            default -> throw new IllegalArgumentException("Brak flagi dla wartości: " + parentNote.getNote().getType());
        };
    }
    public double getFontWidth() { return (getFontData().getHeight() * getFontData().getRatio()) * style.getStaffLineSpacing(); }
    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return getFontData().getCode(); }
    public ScoreStyle getScoreStyle() { return style; }
    public NoteLayout getParent() { return parentNote; }
}