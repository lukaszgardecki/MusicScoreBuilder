package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Clef;
import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout extends ElementLayout {
    private final Leland fontData;
    private final double height;
    private final double scale;
    private double y;

    public ClefLayout(Clef clef, StaffLayout staff, SegmentLayout parent) {
        super(false, parent, staff);
        this.height = staff.getHeight();
        this.scale = staff.getLineSpacing();
        ClefType type = clef.getType();
        fontData = type.getFontData();
        y = type.getOffsetY() * staff.getLineSpacing() + staff.getY();
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * scale; }
    @Override public double getHeight() { return fontData.getHeight() * scale; }
    @Override public double getBoxY() { return getY() - (fontData.getNEy() * scale); }
    @Override public int getVoice() { return 1; }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
}