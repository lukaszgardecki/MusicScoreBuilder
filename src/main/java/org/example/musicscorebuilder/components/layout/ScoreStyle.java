package org.example.musicscorebuilder.components.layout;

public class ScoreStyle {
    private static final double FACTORY_PAGE_SPACING = 4.0;
    private static final double FACTORY_STAFF_SPACING = 4.0;
    private static final double FACTORY_PART_SPACING = 5.0;
    private static final double FACTORY_SYSTEM_SPACING = 6.0;
    private static final double FACTORY_SPATIUM_MM = 1.75;

    private double pageSpacing = FACTORY_PAGE_SPACING;
    private double staffSpacing = FACTORY_STAFF_SPACING;
    private double partSpacing = FACTORY_PART_SPACING;
    private double systemSpacing = FACTORY_SYSTEM_SPACING;
    private double spatiumMm = FACTORY_SPATIUM_MM;

    public double toSp(double valueInMm) {
        if (spatiumMm <= 0) return 0;
        return valueInMm / spatiumMm;
    }

    public double getPageSpacing() { return pageSpacing; }
    public double getStaffSpacing() { return staffSpacing; }
    public double getPartSpacing() { return partSpacing; }
    public double getSystemSpacing() { return systemSpacing; }
}