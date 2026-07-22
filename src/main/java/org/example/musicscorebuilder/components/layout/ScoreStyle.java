package org.example.musicscorebuilder.components.layout;

public class ScoreStyle {

    private static final double PAGE_SPACING = 4.0;
    private static final double STAFF_SPACING = 7.0;
    private static final double SYSTEM_SPACING = 8.0;
    private static final double SPATIUM_MM = 1.564;
    private static final double SYSTEM_MIN_FULLNESS_RATIO = 0.5;

    private static final double STAFF_SPACING_SCALE = 1.3;
    private static final double STAFF_LINE_SPACING = 1.0;
    private static final double STAFF_LINE_WIDTH = 0.11;

    private static final double BARLINE_LIGHT_WIDTH = 0.18;
    private static final double BARLINE_HEAVY_WIDTH = 0.55;
    private static final double BARLINE_GAP = 0.37;
    private static final double BARLINE_DOT_SPACE = 0.37;
    private static final double BARLINE_DOT_RADIUS = 0.2;

    private static final double SEGMENT_RIGHT_MARGIN = 0.7;

    private static final double KEY_SIGNATURE_SIGN_SPACE = 0.12;

    private static final double NOTE_LEDGER_LINE_LENGTH_FACTOR = 1.35;
    private static final double NOTE_LEDGER_LINE_THICKNESS = 0.16;
    private static final int NOTE_MAX_LEDGER_LINES = 3;
    private static final double NOTE_STEM_WIDTH = 0.1;
    private static final double NOTE_STEM_SINGLE_DEFAULT_HEIGHT = 3.5;
    private static final double NOTE_STEM_BEAMED_DEFAULT_HEIGHT = 3.0;
    private static final double NOTE_STEM_MIN_HEIGHT = 2.25;
    private static final double NOTE_STEM_HEIGHT_DIFF_FACTOR = 0.2;
    private static final double NOTE_BEAM_THICKNESS = 0.5;

    private static final String VOICE_1_COLOR = "#0066cc";
    private static final String VOICE_2_COLOR = "#007a1a";
    private static final double SELECTION_FRAME_WIDTH = 2 * STAFF_LINE_WIDTH;
    private static final double SELECTION_FRAME_EXTRA_HEIGHT = STAFF_LINE_SPACING;
    private static final double SELECTION_FRAME_RADIUS = 0.4;

    private double pageSpacing = PAGE_SPACING;
    private double staffSpacing = STAFF_SPACING;
    private double systemSpacing = SYSTEM_SPACING;
    private double spatiumMm = SPATIUM_MM;
    private double systemMinFullnessRatio = SYSTEM_MIN_FULLNESS_RATIO;

    private double staffSpacingScale = STAFF_SPACING_SCALE;
    private double staffLineSpacing = STAFF_LINE_SPACING;
    private double staffLineWidth = STAFF_LINE_WIDTH;

    private double barlineLightWidth = BARLINE_LIGHT_WIDTH;
    private double barlineHeavyWidth = BARLINE_HEAVY_WIDTH;
    private double barlineGap = BARLINE_GAP;
    private double barlineDotSpace = BARLINE_DOT_SPACE;
    private double barlineDotRadius = BARLINE_DOT_RADIUS;

    private double segmentRightMargin = SEGMENT_RIGHT_MARGIN;

    private double keySignatureSignSpace = KEY_SIGNATURE_SIGN_SPACE;

    private double noteLedgerLengthFactor = NOTE_LEDGER_LINE_LENGTH_FACTOR;
    private double noteLedgerLineThickness = NOTE_LEDGER_LINE_THICKNESS;
    private int noteMaxLedgerLines = NOTE_MAX_LEDGER_LINES;
    private double noteStemWidth = NOTE_STEM_WIDTH;
    private double noteStemSingleDefaultHeight = NOTE_STEM_SINGLE_DEFAULT_HEIGHT;
    private double noteStemBeamedDefaultHeight = NOTE_STEM_BEAMED_DEFAULT_HEIGHT;
    private double noteStemMinHeight = NOTE_STEM_MIN_HEIGHT;
    private double noteStemHeightDiffFactor = NOTE_STEM_HEIGHT_DIFF_FACTOR;
    private double noteBeamThickness = NOTE_BEAM_THICKNESS;

    private String voice1Color = VOICE_1_COLOR;
    private String voice2Color = VOICE_2_COLOR;
    private double selectionFrameWidth =  SELECTION_FRAME_WIDTH;
    private double selectionFrameExtraHeight = SELECTION_FRAME_EXTRA_HEIGHT;
    private double selectionFrameRadius = SELECTION_FRAME_RADIUS;


    public double getPageSpacing() { return staffSpacingScale * pageSpacing; }
    public double getStaffSpacing() { return staffSpacingScale * staffSpacing; }
    public double getSystemSpacing() { return staffSpacingScale * systemSpacing; }
    public double getSpatiumMm() { return spatiumMm; }
    public double getSystemMinFullnessRatio() { return systemMinFullnessRatio; }

    public double getStaffLineSpacing() { return staffSpacingScale * staffLineSpacing; }
    public double getStaffLineWidth() { return staffSpacingScale * staffLineWidth; }

    public double getBarlineLightWidth() { return staffSpacingScale * barlineLightWidth; }
    public double getBarlineHeavyWidth() { return staffSpacingScale * barlineHeavyWidth; }
    public double getBarlineGap() { return staffSpacingScale * barlineGap; }
    public double getBarlineDotSpace() { return staffSpacingScale * barlineDotSpace; }
    public double getBarlineDotRadius() { return staffSpacingScale * barlineDotRadius; }

    public double getSegmentRightMargin() { return staffSpacingScale * segmentRightMargin; }

    public double getKeySignatureSignSpace() { return staffSpacingScale * keySignatureSignSpace; }

    public double getNoteLedgerLengthFactor() { return noteLedgerLengthFactor; }
    public double getNoteLedgerLineThickness() { return staffSpacingScale * noteLedgerLineThickness; }
    public int getNoteMaxLedgerLines() { return noteMaxLedgerLines; }
    public double getNoteStemWidth() { return staffSpacingScale * noteStemWidth; }
    public double getNoteStemSingleDefaultHeight() { return staffSpacingScale * noteStemSingleDefaultHeight; }
    public double getNoteStemBeamedDefaultHeight() { return staffSpacingScale * noteStemBeamedDefaultHeight; }
    public double getNoteStemMinHeight() { return staffSpacingScale * noteStemMinHeight; }
    public double getNoteStemHeightDiffFactor() { return noteStemHeightDiffFactor * getStaffLineSpacing(); }
    public double getNoteBeamThickness() { return staffSpacingScale * noteBeamThickness; }

    public double getSelectionFrameWidth() { return staffSpacingScale * selectionFrameWidth; }
    public double getSelectionFrameExtraHeight() { return staffSpacingScale * selectionFrameExtraHeight; }
    public double getSelectionFrameRadius() { return staffSpacingScale * selectionFrameRadius; }

    public double toSp(double valueInMm) {
        if (spatiumMm <= 0) return 0;
        return valueInMm / spatiumMm;
    }

    public String getSelectColor(Selectable element) {
        if (element.isSelected()) {
            int voice =  element.getVoice();
            return switch (voice) {
                case 1 -> voice1Color;
                default -> voice2Color;
            };
        }
        return "#000000";
    }
}