package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.JumpMark;

public class JumpTextLayout extends JumpMarkLayout {
    private double width = 6.0;
    private double y;

    public JumpTextLayout(JumpMark jumpMark, MeasureLayout parentMeasure, StaffLayout parentStaff, double y) {
        super(jumpMark, parentMeasure, parentStaff);
        this.y = y;
    }

    @Override
    public boolean contains(double px, double py) {
        double minX = getX();
        double maxX = getX() + getWidth();
        double minY = getBoxY();
        double maxY = minY + getHeight();
        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return width; }
    @Override public double getHeight() { return getFontSize() + 0.2; }
    @Override public double getBoxY() {return getY() - getHeight();}

    public String getText() {
        return jumpMark.getText() != null && !jumpMark.getText().trim().isEmpty()
                ? jumpMark.getText()
                : jumpMark.getType().getDefaultText();
    }

    public double getFontSize() { return style.getJumpMarkFontSize(); }
}