package org.example.musicscorebuilder.components.layout;

public class ScoreStyle {

    private static final double PAGE_SPACING = 4.0;
    private static final double STAFF_SPACING = 4.0;
    private static final double SYSTEM_SPACING = 6.0;
    private static final double SPATIUM_MM = 1.564;
    private static final double SYSTEM_MIN_FULLNESS_RATIO = 0.5;

    private static final double STAFF_SPACING_SCALE = 1.3;
    private static final double STAFF_LINE_SPACING = 1;
    private static final double STAFF_LINE_WIDTH_SP = 0.11;

    private static final double BARLINE_LIGHT_WIDTH = 0.18;
    private static final double BARLINE_HEAVY_WIDTH = 0.55;
    private static final double BARLINE_GAP = 0.37;
    private static final double BARLINE_DOT_SPACE = 0.37;
    private static final double BARLINE_DOT_RADIUS = 0.15;

    private static final double SEGMENT_LEFT_MARGIN = 1.0;

    private static final double KEY_SIGNATURE_SIGN_SPACE = 0.12;

    private double pageSpacing = PAGE_SPACING;
    private double staffSpacing = STAFF_SPACING;
    private double systemSpacing = SYSTEM_SPACING;
    private double spatiumMm = SPATIUM_MM;
    private double systemMinFullnessRatio = SYSTEM_MIN_FULLNESS_RATIO;

    private double staffSpacingScale = STAFF_SPACING_SCALE;
    private double staffLineSpacing = staffSpacingScale * STAFF_LINE_SPACING;
    private double staffLineWidth = STAFF_LINE_WIDTH_SP;

    private double barlineLightWidth = BARLINE_LIGHT_WIDTH;
    private double barlineHeavyWidth = BARLINE_HEAVY_WIDTH;
    private double barlineGap = BARLINE_GAP;
    private double barlineDotSpace = BARLINE_DOT_SPACE;
    private double barlineDotRadius = BARLINE_DOT_RADIUS;

    private double segmentLeftMargin = SEGMENT_LEFT_MARGIN;

    private double keySignatureSignSpace = KEY_SIGNATURE_SIGN_SPACE;


    public double getPageSpacing() { return pageSpacing; }
    public double getStaffSpacing() { return staffSpacing; }
    public double getSystemSpacing() { return systemSpacing; }
    public double getSpatiumMm() { return spatiumMm; }
    public double getSystemMinFullnessRatio() { return systemMinFullnessRatio; }

    public double getStaffSpacingScale() { return staffSpacingScale; }
    public double getStaffLineSpacing() { return staffLineSpacing; }
    public double getStaffLineWidth() { return staffLineWidth; }

    public double getBarlineLightWidth() { return barlineLightWidth; }
    public double getBarlineHeavyWidth() { return barlineHeavyWidth; }
    public double getBarlineGap() { return barlineGap; }
    public double getBarlineDotSpace() { return barlineDotSpace; }
    public double getBarlineDotRadius() { return barlineDotRadius; }

    public double getSegmentLeftMargin() { return segmentLeftMargin; }

    public double getKeySignatureSignSpace() { return keySignatureSignSpace; }



    public double toSp(double valueInMm) {
        if (spatiumMm <= 0) return 0;
        return valueInMm / spatiumMm;
    }
}