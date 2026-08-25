package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Leland;

public class DotLayout implements Selectable {
    private final Leland fontData = Leland.AUGMENTATION_DOT;
    private final ScoreStyle style;
    private final NoteLayout parentNote;
    private double x, y;
    private boolean selected;

    public DotLayout(NoteLayout parent, double x,  double y) {
        this.parentNote = parent;
        this.x = x;
        this.y = y;
        this.style = parent.getScoreStyle();
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return parentNote.getVoice(); }

    @Override
    public boolean contains(double px, double py) {
        double height = fontData.getHeight() * style.getStaffLineSpacing();
        double minX = parentNote.getX() + this.x;
        double maxX = minX + getWidth();
        double absCenterY = parentNote.getY() + this.y;
        double minY = absCenterY - (height / 2.0);
        double maxY = absCenterY + (height / 2.0);
        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public SegmentLayout getSegment() { return parentNote.getSegment(); }
    @Override public StaffLayout getStaff() { return parentNote.getStaff(); }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public ScoreStyle getScoreStyle() { return style; }
    public NoteLayout getParent()  { return parentNote; }

    public double getFontSize() { return 4 * style.getStaffLineSpacing(); }
    public String getCode() { return fontData.getCode(); }
}