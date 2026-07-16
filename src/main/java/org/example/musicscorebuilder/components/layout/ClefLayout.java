package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout extends ElementLayout {
    private final Leland fontData;
    private double height;
    private double x, y;

    public ClefLayout(StaffLayout staffLayout) {
        super(false);
        this.height = staffLayout.getHeight();
        ClefType type = staffLayout.getStaff().getDefaultClef().getType();
        fontData = type.getFontData();
        x = 0.0;
        y = type.getOffsetY() * staffLayout.getLineSpacing() + staffLayout.getY();
    }

    @Override public double getX() { return x; }
    @Override public double getY() { return y; }
    @Override public double getWidth() { return getHeight() * fontData.getRatio(); }
    @Override public double getHeight() { return fontData.getHeight(); }
    @Override public double getBoxY() { return getY() - fontData.getNEy(); }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
}
