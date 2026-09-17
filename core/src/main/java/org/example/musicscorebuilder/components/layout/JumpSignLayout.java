package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.JumpMark;
import org.example.musicscorebuilder.components.music.JumpType;
import org.example.musicscorebuilder.components.music.Leland;

public class JumpSignLayout extends JumpMarkLayout {
    private final Leland fontData;
    private final double height;
    private double y;

    public JumpSignLayout(JumpMark jumpMark, MeasureLayout parentMeasure, StaffLayout parentStaff, double y) {
        super(jumpMark, parentMeasure, parentStaff);
        this.fontData = jumpMark.getType().getFontData();
        this.height = staff.getHeight() * jumpMark.getType().getHeightFactor();
        this.y = y;
    }

    @Override
    public boolean contains(double px, double py) {
        double minX = (getPosition() == JumpType.Position.END_OF_MEASURE)
                ? getX() - getWidth()
                : getX();
        double maxX = minX + getWidth();

        double minY = getBoxY();
        double maxY = minY + getHeight();

        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    @Override public double getHeight() { return fontData.getHeight() * style.getStaffLineSpacing(); }
    @Override public double getBoxY() { return getY() - (fontData.getNEy() * style.getStaffLineSpacing()); }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
}