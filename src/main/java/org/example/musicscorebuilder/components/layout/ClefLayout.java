package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout extends ElementLayout {
    private final Leland fontData;
    private double height;
    private double x, y;
    private final double leftMargin = 0.85;

    public ClefLayout(StaffLayout staffLayout) {
        super(false);
        this.height = staffLayout.getHeight();
        ClefType type = staffLayout.getStaff().getDefaultClef().getType();
        fontData = type.getFontData();
        x = leftMargin;
        y = type.getOffsetY() * staffLayout.getLineSpacing() + staffLayout.getY();
    }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
    public double boxY() { return getY() - fontData.getNEy(); }

    @Override public double getWidth() { return getHeight() * fontData.getRatio() + leftMargin; }
    public double getHeight() { return fontData.getHeight(); }
    public double getX() { return x; }
    public double getY() { return y; }
}
