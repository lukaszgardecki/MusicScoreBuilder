package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout extends SegmentLayout {
    private final Leland fontData;
    private double height;
    private double offsetY;

    public ClefLayout(StaffLayout staffLayout, double x) {
        super(SegmentType.CLEF, x, 0);
        this.height = staffLayout.getHeight();
        ClefType type = staffLayout.getStaff().getDefaultClef().getType();
        fontData = type.getFontData();
        offsetY = type.getOffsetY() * staffLayout.getLineSpacing();
    }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
    public double boxY() { return getY() - fontData.getNEy(); }

    @Override
    public double getHeight() { return fontData.getHeight(); }

    @Override
    public double getWidth() { return getHeight() * fontData.getRatio(); }

    @Override
    public double getY() { return offsetY ; }
}
