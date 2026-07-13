package org.example.musicscorebuilder.components.layout;

public class ScoreStyle {

    private static final double PAGE_SPACING = 4.0;
    private static final double STAFF_SPACING = 4.0;
    private static final double PART_SPACING = 5.0;
    private static final double SYSTEM_SPACING = 6.0;
    private static final double SPATIUM_MM = 1.75;
    private static final double SYSTEM_MIN_FULLNESS_RATIO = 0.5;

    private double pageSpacing = PAGE_SPACING;
    private double staffSpacing = STAFF_SPACING;
    private double partSpacing = PART_SPACING;
    private double systemSpacing = SYSTEM_SPACING;
    private double spatiumMm = SPATIUM_MM;
    private double systemMinFullnessRatio = SYSTEM_MIN_FULLNESS_RATIO;

    public double toSp(double valueInMm) {
        if (spatiumMm <= 0) return 0;
        return valueInMm / spatiumMm;
    }

    public double getPageSpacing() { return pageSpacing; }
    public double getStaffSpacing() { return staffSpacing; }
    public double getPartSpacing() { return partSpacing; }
    public double getSystemSpacing() { return systemSpacing; }
    public double getSystemMinFullnessRatio() { return systemMinFullnessRatio; }
}