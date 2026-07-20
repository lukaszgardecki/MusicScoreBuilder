package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Clef;
import org.example.musicscorebuilder.components.music.ClefType;
import org.example.musicscorebuilder.components.music.Leland;

public class ClefLayout extends ElementLayout {
    private final Leland fontData;
    private final double height;
    private final double scale;
    private double y;

    public ClefLayout(Clef clef, StaffLayout staffLayout, SegmentLayout parent) {
        super(false, parent);
        this.height = staffLayout.getHeight();
        this.scale = staffLayout.getLineSpacing();
        ClefType type = clef.getType();
        fontData = type.getFontData();
        y = type.getOffsetY() * staffLayout.getLineSpacing() + staffLayout.getY();
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * scale; }
    @Override public double getHeight() { return fontData.getHeight() * scale; }
    @Override public double getBoxY() { return getY() - (fontData.getNEy() * scale); }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
}