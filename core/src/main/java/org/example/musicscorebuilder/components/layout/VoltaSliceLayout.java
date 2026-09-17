package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Volta;

public class VoltaSliceLayout implements Selectable {
    private final ScoreStyle style;
    private final Volta volta;
    private final MeasureLayout measureLayout;
    private final String text;
    private boolean selected;
    private final boolean drawLeftHook;
    private final boolean drawRightHook;

    private final double startXOffset;
    private final double endXOffset;

    public VoltaSliceLayout(Volta volta, MeasureLayout measureLayout, String text,
                            boolean drawLeftHook, boolean drawRightHook,
                            double startXOffset, double endXOffset) {
        this.volta = volta;
        this.measureLayout = measureLayout;
        this.style = (measureLayout != null) ? measureLayout.getScoreStyle() : new ScoreStyle();
        this.text = text;
        this.drawLeftHook = drawLeftHook;
        this.drawRightHook = drawRightHook;
        var halfLineWidth = getLineWidth() * 0.5;
        this.startXOffset = startXOffset + halfLineWidth;
        this.endXOffset = endXOffset - halfLineWidth;
    }

    public VoltaSliceLayout(Volta volta, String text, boolean drawLeftHook, boolean drawRightHook) {
        this(volta, null, text, drawLeftHook, drawRightHook, 0.0, 12.0);
    }

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }

    @Override
    public boolean contains(double px, double py) {
        double startX = startXOffset;
        double endX = endXOffset;
        double lineY = -getYOffset();

        double maxHook = Math.max(
                drawLeftHook ? getStartHookHeight() : 0.0,
                drawRightHook ? getEndHookHeight() : 0.0
        );
        if (maxHook == 0.0) {
            maxHook = getLineWidth();
        }

        double hookBottomY = lineY + maxHook;
        double minX = Math.min(startX, endX);
        double maxX = Math.max(startX, endX);
        double minY = Math.min(lineY, hookBottomY);
        double maxY = Math.max(lineY, hookBottomY);

        return px >= minX && px <= maxX && py >= minY && py <= maxY;
    }

    @Override public SegmentLayout getSegment() { return measureLayout != null ? measureLayout.getSegments().get(0) : null; }
    @Override public StaffLayout getStaff() { return measureLayout != null ? measureLayout.getStaffs().get(0) : null; }
    @Override public int getVoice() { return 1; }

    public ScoreStyle getStyle() { return style; }
    public Volta getVolta() { return volta; }
    public MeasureLayout getMeasureLayout() { return measureLayout; }
    public String getText() { return text; }
    public boolean isDrawLeftHook() { return drawLeftHook; }
    public boolean isDrawRightHook() { return drawRightHook; }

    public double getStartXOffset() { return startXOffset; }
    public double getEndXOffset() { return endXOffset; }

    public double getTextXOffset() {
        return (volta != null && volta.getTextXOffset() != null) ? volta.getTextXOffset() : style.getVoltaTextXOffset();
    }

    public double getYOffset() {
        return (volta != null && volta.getYOffset() != null) ? volta.getYOffset() : style.getVoltaYOffset();
    }

    public double getStartHookHeight() {
        return (volta != null && volta.getStartHookHeight() != null) ? volta.getStartHookHeight() : style.getVoltaLeftHookHeight();
    }

    public double getEndHookHeight() {
        return (volta != null && volta.getEndHookHeight() != null) ? volta.getEndHookHeight() : style.getVoltaRightHookHeight();
    }

    public double getLineWidth() {
        return (volta != null && volta.getLineWidth() != null) ? volta.getLineWidth() : style.getVoltaLineWidth();
    }

    public Volta.LineStyle getLineStyle() {
        return (volta != null && volta.getLineStyle() != null) ? volta.getLineStyle() : Volta.LineStyle.SOLID;
    }

    public double getDashLength() {
        return (volta != null && volta.getDashLength() != null) ? volta.getDashLength() : style.getVoltaLineDashLength();
    }

    public double getDashGap() {
        return (volta != null && volta.getDashGap() != null) ? volta.getDashGap() : style.getVoltaLineDashGapLength();
    }

    public double getFontSize() {
        return (volta != null && volta.getFontSize() != null) ? volta.getFontSize() : style.getVoltaFontSize();
    }
}