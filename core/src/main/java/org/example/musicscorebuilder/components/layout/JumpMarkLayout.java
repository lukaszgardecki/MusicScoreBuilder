package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.util.MeasureOffsetCalculator;
import org.example.musicscorebuilder.components.music.JumpMark;
import org.example.musicscorebuilder.components.music.JumpType;

public abstract class JumpMarkLayout  extends ElementLayout {
    protected final JumpMark jumpMark;
    private boolean selected;

    public JumpMarkLayout(JumpMark jumpMark, MeasureLayout parentMeasure, StaffLayout parentStaff) {
        super(false, parentMeasure.getSegments().get(0), parentStaff);
        this.jumpMark = jumpMark;
        double x = (getPosition() == JumpType.Position.START_OF_MEASURE)
                ? MeasureOffsetCalculator.calculateStartXOffset(parentMeasure)
                : MeasureOffsetCalculator.calculateEndXOffset(parentMeasure);
        setX(x);
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return 1; }

    public JumpType.Position getPosition() { return jumpMark.getType().getDefaultPosition(); }
}