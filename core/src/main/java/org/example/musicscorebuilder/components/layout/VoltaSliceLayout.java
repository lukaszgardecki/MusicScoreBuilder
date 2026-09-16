package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Volta;

public class VoltaSliceLayout {
    private final ScoreStyle scoreStyle;
    private final Volta volta;
    private final MeasureLayout measureLayout;
    private final String text;
    private final boolean drawLeftHook;
    private final boolean drawRightHook;

    private final double startXOffset;
    private final double endXOffset;

    private double yOffset = 2.5;
    private double hookHeight = 1.9;
    private double fontSize = hookHeight + 0.2;

    public VoltaSliceLayout(Volta volta, MeasureLayout measureLayout, String text,
                            boolean drawLeftHook, boolean drawRightHook,
                            double startXOffset, double endXOffset) {
        this.volta = volta;
        this.measureLayout = measureLayout;
        this.scoreStyle = (measureLayout != null) ? measureLayout.getScoreStyle() : new ScoreStyle();
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

    public ScoreStyle getScoreStyle() { return scoreStyle; }
    public Volta getVolta() { return volta; }
    public MeasureLayout getMeasureLayout() { return measureLayout; }
    public String getText() { return text; }
    public boolean isDrawLeftHook() { return drawLeftHook; }
    public boolean isDrawRightHook() { return drawRightHook; }

    public double getStartXOffset() { return startXOffset; }
    public double getEndXOffset() { return endXOffset; }

    public double getYOffset() { return yOffset; }
    public void setYOffset(double yOffset) { this.yOffset = yOffset; }

    public double getHookHeight() { return hookHeight; }
    public void setHookHeight(double hookHeight) { this.hookHeight = hookHeight; }

    public double getFontSize() { return fontSize; }
    public void setFontSize(double fontSize) { this.fontSize = fontSize; }

    public double getLineWidth() {
        return scoreStyle.getStaffLineWidth();
    }
}