package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Clef;
import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout extends ElementLayout {
    private final Leland fontData;
    private final double height;
    private double y;

    public ClefLayout(Clef clef, StaffLayout staff, SegmentLayout parent) {
        super(false, parent, staff);
        this.height = staff.getHeight();
        ClefType type = clef.getType();
        fontData = type.getFontData();
        y = type.getOffsetY() * staff.getLineSpacing() + staff.getY();
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    @Override public double getHeight() { return fontData.getHeight() * style.getStaffLineSpacing(); }
    @Override public double getBoxY() { return getY() - (fontData.getNEy() * style.getStaffLineSpacing()); }
    @Override public int getVoice() { return 1; }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
}