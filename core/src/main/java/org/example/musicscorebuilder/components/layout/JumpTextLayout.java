package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.JumpMark;
import org.example.musicscorebuilder.components.music.JumpType;

public class JumpTextLayout extends JumpMarkLayout {
    private double y;

    public JumpTextLayout(JumpMark jumpMark, MeasureLayout parentMeasure, StaffLayout parentStaff, double y) {
        super(jumpMark, parentMeasure, parentStaff);
        this.y = y;
    }

    @Override
    public boolean contains(double px, double py) {
        double sp = style.getStaffLineSpacing();
        double textX = getX() * sp;
        double w = getWidth();

        double minX = (getPosition() == JumpType.Position.END_OF_MEASURE) ? textX - w : textX;
        double maxX = minX + w;

        double minY = getBoxY();
        double maxY = minY + getHeight();

        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return getText().length() * 0.55 * getFontSize() * style.getStaffLineSpacing(); }
    @Override public double getHeight() { return getFontSize() * style.getStaffLineSpacing(); }
    @Override public double getBoxY() { return (getY() - getFontSize()) * style.getStaffLineSpacing(); }

    public String getText() {
        return jumpMark.getText() != null && !jumpMark.getText().trim().isEmpty()
                ? jumpMark.getText()
                : jumpMark.getType().getDefaultText();
    }

    public double getFontSize() { return style.getJumpMarkFontSize(); }
}