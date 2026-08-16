package org.example.musicscorebuilder.components.layout.engine;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.musicscorebuilder.components.layout.Selectable;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class ScoreStyle {

    private static final double PAGE_SPACING                                = 4.0;
    private static final double STAFF_SPACING                               = 7.0;
    private static final double SYSTEM_SPACING                              = 7.0;
    private static final double SPATIUM_MM                                  = 1.564;
    private static final double SYSTEM_MIN_FULLNESS_RATIO                   = 0.3;

    private static final double STAFF_SPACING_SCALE                         = 1.0;
    private static final double STAFF_LINE_SPACING                          = 1.0;
    private static final double STAFF_LINE_WIDTH                            = 0.11;

    private static final double BARLINE_LIGHT_WIDTH                         = 0.18;
    private static final double BARLINE_HEAVY_WIDTH                         = 0.55;
    private static final double BARLINE_GAP                                 = 0.37;
    private static final double BARLINE_DOT_SPACE                           = 0.37;
    private static final double BARLINE_DOT_RADIUS                          = 0.2;

    private static final double KEY_SIGNATURE_SIGN_SPACE                    = 0.12;

    private static final double NOTE_LEDGER_LINE_LENGTH_FACTOR              = 1.35;
    private static final double NOTE_LEDGER_LINE_THICKNESS                  = 0.16;
    private static final int NOTE_MAX_LEDGER_LINES                          = 3;
    private static final double NOTE_STEM_WIDTH                             = 0.1;
    private static final double NOTE_STEM_SINGLE_DEFAULT_HEIGHT             = 3.5;
    private static final double NOTE_STEM_BEAMED_DEFAULT_HEIGHT             = 3.25;
    private static final double NOTE_STEM_MIN_HEIGHT                        = 2.25;
    private static final double NOTE_STEM_HEIGHT_DIFF_FACTOR                = 0.2;
    private static final double NOTE_BEAM_THICKNESS                         = 0.5;
    private static final double NOTE_BEAM_STUB_LENGTH                       = 1.0;
    private static final double NOTE_BEAM_GAP                               = 0.3;
    private static final double NOTE_DOT_MARGIN                             = 0.55;
    private static final double NOTE_DOT_SPACING                            = 0.3;
    private static final double NOTE_ACC_SPACING                            = 0.15;
    private static final double NOTE_LYRIC_FONT_SIZE                        = 2.3;

    private static final double BOW_MID_THICKNESS                           = 0.18;
    private static final double BOW_TIP_ROUNDING_FACTOR                     = 0.05;
    private static final double BOW_MAX_DX_RATIO                            = 0.20;
    private static final double BOW_HEIGHT_FACTOR                           = 1.0;
    private static final double BOW_Y_NOTE_SPACE                            = 0.2;
    private static final double BOW_X_NOTE_SPACE                            = 0.2;
    private static final double BOW_SYSTEM_BREAK_END_X_MARGIN               = 0.5;

    private static final String VOICE_1_COLOR                               = "#0066cc";
    private static final String VOICE_2_COLOR                               = "#007a1a";
    private static final String VOICE_3_COLOR                               = "#c53f00";
    private static final String VOICE_4_COLOR                               = "#c31989";
    private static final String VOICE_1_INSERT_COLOR                        = "#3399ff";
    private static final String VOICE_2_INSERT_COLOR                        = "#00b32d";
    private static final String VOICE_3_INSERT_COLOR                        = "#f45409";
    private static final String VOICE_4_INSERT_COLOR                        = "#f326ad";
    private static final double SELECTION_FRAME_WIDTH                       = 2 * STAFF_LINE_WIDTH;
    private static final double SELECTION_FRAME_EXTRA_HEIGHT                = STAFF_LINE_SPACING;
    private static final double SELECTION_FRAME_RADIUS                      = 0.4;

    private static final double SEG_DEF_NOTEREST_L_MARGIN                   = 0.9 + NOTE_ACC_SPACING;
    private static final double SEG_DEF_NOTEREST_R_MARGIN                   = 2;
    private static final double SEG_DEF_CLEF_R_MARGIN                       = 0.5;
    private static final double SEG_DEF_TIME_SIG_R_MARGIN                   = 1.0;
    private static final double SEG_DEF_KEY_SIG_R_MARGIN                    = 0.7;
    private static final double SEG_DEF_START_BARLINE_R_MARGIN              = 0.5;
    private static final double SEG_DEF_BARLINE_R_MARGIN                    = 0.5;
    private static final double SEG_DEF_END_BARLINE_R_MARGIN                = 0;

    private static final double EDIT_CURSOR_LINE_THICKNESS                  = 0.2;
    private static final double EDIT_CURSOR_BOX_WIDTH                       = 2.0;
    private static final double EDIT_CURSOR_PADDING                         = 0.8;

    private static final double HEADER_DEF_MARGIN_TOP                       = 0.0;
    private static final double HEADER_DEF_MARGIN_BOTTOM                    = 4.0;
    private static final double HEADER_DEF_NUMBER_NEW_FONT_SIZE             = 3.3;
    private static final double HEADER_DEF_NUMBER_OLD_FONT_SIZE             = 2.5;
    private static final double HEADER_DEF_TITLE_FONT_SIZE                  = 3.3;
    private static final double HEADER_DEF_SUBTITLE_FONT_SIZE               = 2.5;
    private static final double HEADER_DEF_COMPOSER_FONT_SIZE               = 2.0;
    private static final double HEADER_DEF_NUM_BOX_MIN_WIDTH                = 9.0;
    private static final double HEADER_DEF_NUM_BOX_MIN_HEIGHT               = 7.0;
    private static final double HEADER_DEF_NUM_BOX_RADIUS                   = 1.0;
    private static final double HEADER_DEF_NUM_BOX_STROKE_WIDTH             = 0.05;
    private static final double HEADER_DEF_NUM_BOX_SPACING                  = 0.8;
    private static final double HEADER_DEF_NUM_BOX_PADDING_X                = 0.5;
    private static final double HEADER_DEF_NUM_BOX_PADDING_Y                = 0.5;

    private static final double FOOTER_DEF_PAGE_NUM_FONT_SIZE               = 2.0;

    private static final double FRAME_STROKE_THICKNESS                      = 0.15;
    private static final double FRAME_STROKE_DASH_LENGTH                    = 0.4;
    private static final double FRAME_STROKE_SPACE_LENGTH                   = 0.3;
    private static final String FRAME_STROKE_COLOR                          = "#a0a0a4";

    private static final double TIE_MAX_THICKNESS                           = 0.2;
    private static final double SLUR_MAX_THICKNESS                          = 0.3;

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

    private double segmentNoteRestLeftMargin = SEG_DEF_NOTEREST_L_MARGIN;
    private double segmentNoteRestRightMargin = SEG_DEF_NOTEREST_R_MARGIN;
    private double segmentClefRightMargin = SEG_DEF_CLEF_R_MARGIN;
    private double segmentTimeSigRightMargin = SEG_DEF_TIME_SIG_R_MARGIN;
    private double segmentKeySigRightMargin = SEG_DEF_KEY_SIG_R_MARGIN;
    private double segmentStartBarlineRightMargin = SEG_DEF_START_BARLINE_R_MARGIN;
    private double segmentBarlineRightMargin = SEG_DEF_BARLINE_R_MARGIN;
    private double segmentEndBarlineRightMargin = SEG_DEF_END_BARLINE_R_MARGIN;

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
    private double noteBeamStubLength = NOTE_BEAM_STUB_LENGTH;
    private double noteBeamGap = NOTE_BEAM_GAP;
    private double noteDotMargin = NOTE_DOT_MARGIN;
    private double noteDotSpacing = NOTE_DOT_SPACING;
    private double noteAccSpacing = NOTE_ACC_SPACING;
    private double noteLyricFontSize = NOTE_LYRIC_FONT_SIZE;

    private double bowMidThickness = BOW_MID_THICKNESS;
    private double bowTipRoundingFactor = BOW_TIP_ROUNDING_FACTOR;
    private double bowMaxDxRatio = BOW_MAX_DX_RATIO;
    private double bowHeightFactor = BOW_HEIGHT_FACTOR;
    private double bowYNoteSpace = BOW_Y_NOTE_SPACE;
    private double bowXNoteSpace = BOW_X_NOTE_SPACE;
    private double bowSystemBreakEndXMargin = BOW_SYSTEM_BREAK_END_X_MARGIN;

    private String voice1Color = VOICE_1_COLOR;
    private String voice2Color = VOICE_2_COLOR;
    private String voice3Color = VOICE_3_COLOR;
    private String voice4Color = VOICE_4_COLOR;
    private String voice1InsertColor = VOICE_1_INSERT_COLOR;
    private String voice2InsertColor = VOICE_2_INSERT_COLOR;
    private String voice3InsertColor = VOICE_3_INSERT_COLOR;
    private String voice4InsertColor = VOICE_4_INSERT_COLOR;
    private double selectionFrameWidth = SELECTION_FRAME_WIDTH;
    private double selectionFrameExtraHeight = SELECTION_FRAME_EXTRA_HEIGHT;
    private double selectionFrameRadius = SELECTION_FRAME_RADIUS;

    private double headerDefMarginTop = HEADER_DEF_MARGIN_TOP;
    private double headerDefMarginBottom = HEADER_DEF_MARGIN_BOTTOM;
    private double headerDefNumberNewFontSize = HEADER_DEF_NUMBER_NEW_FONT_SIZE;
    private double headerDefNumberOldFontSize = HEADER_DEF_NUMBER_OLD_FONT_SIZE;
    private double headerDefTitleFontSize = HEADER_DEF_TITLE_FONT_SIZE;
    private double headerDefSubtitleFontSize = HEADER_DEF_SUBTITLE_FONT_SIZE;
    private double headerDefComposerFontSize = HEADER_DEF_COMPOSER_FONT_SIZE;
    private double headerDefNumBoxMinWidth = HEADER_DEF_NUM_BOX_MIN_WIDTH;
    private double headerDefNumBoxMinHeight = HEADER_DEF_NUM_BOX_MIN_HEIGHT;
    private double headerDefNumBoxRadius = HEADER_DEF_NUM_BOX_RADIUS;
    private double headerDefNumBoxStrokeWidth = HEADER_DEF_NUM_BOX_STROKE_WIDTH;
    private double headerDefNumBoxSpacing = HEADER_DEF_NUM_BOX_SPACING;
    private double headerDefNumBoxPaddingX = HEADER_DEF_NUM_BOX_PADDING_X;
    private double headerDefNumBoxPaddingY = HEADER_DEF_NUM_BOX_PADDING_Y;

    private double footerDefPageNumFontSize = FOOTER_DEF_PAGE_NUM_FONT_SIZE;

    private double frameStrokeThickness = FRAME_STROKE_THICKNESS;
    private double frameStrokeDashLength = FRAME_STROKE_DASH_LENGTH;
    private double frameStrokeSpaceLength = FRAME_STROKE_SPACE_LENGTH;
    private String frameStrokeColor = FRAME_STROKE_COLOR;

    public double getPageSpacing() { return staffSpacingScale * pageSpacing; }
    public double getStaffSpacing() { return staffSpacingScale * staffSpacing; }
    public double getSystemSpacing() { return staffSpacingScale * systemSpacing; }
    public double getSpatiumMm() { return spatiumMm; }
    public double getSystemMinFullnessRatio() { return systemMinFullnessRatio; }

    public double getStaffSpacingScale() { return staffSpacingScale; }
    public double getStaffLineSpacing() { return staffSpacingScale * staffLineSpacing; }
    public double getStaffLineWidth() { return staffSpacingScale * staffLineWidth; }

    public double getBarlineLightWidth() { return staffSpacingScale * barlineLightWidth; }
    public double getBarlineHeavyWidth() { return staffSpacingScale * barlineHeavyWidth; }
    public double getBarlineGap() { return staffSpacingScale * barlineGap; }
    public double getBarlineDotSpace() { return staffSpacingScale * barlineDotSpace; }
    public double getBarlineDotRadius() { return staffSpacingScale * barlineDotRadius; }

    public double getSegmentNoteRestLeftMargin() { return staffSpacingScale * segmentNoteRestLeftMargin; }
    public double getSegmentNoteRestRightMargin() { return staffSpacingScale * segmentNoteRestRightMargin; }
    public double getSegmentClefRightMargin() { return staffSpacingScale * segmentClefRightMargin; }
    public double getSegmentTimeSigRightMargin() { return staffSpacingScale * segmentTimeSigRightMargin; }
    public double getSegmentKeySigRightMargin() { return staffSpacingScale * segmentKeySigRightMargin; }
    public double getSegmentStartBarlineRightMargin() { return staffSpacingScale * segmentStartBarlineRightMargin; }
    public double getSegmentBarlineRightMargin() { return staffSpacingScale * segmentBarlineRightMargin; }
    public double getSegmentEndBarlineRightMargin() { return staffSpacingScale * segmentEndBarlineRightMargin; }

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
    public double getNoteBeamStubLength() { return staffSpacingScale * noteBeamStubLength; }
    public double getNoteBeamGap() { return staffSpacingScale * noteBeamGap; }
    public double getNoteDotMargin() { return staffSpacingScale * noteDotMargin; }
    public double getNoteDotSpacing() { return staffSpacingScale * noteDotSpacing; }
    public double getNoteAccSpacing() { return staffSpacingScale * noteAccSpacing; }
    public double getNoteLyricFontSize() { return staffSpacingScale * noteLyricFontSize; }

    public double getBowMidThickness() { return staffSpacingScale * bowMidThickness; }
    public double getBowTipRoundingFactor() { return staffSpacingScale * bowTipRoundingFactor; }
    public double getBowMaxDxRatio() { return bowMaxDxRatio; }
    public double getBowHeightFactor() { return staffSpacingScale * bowHeightFactor; }
    public double getBowYNoteSpace() { return staffSpacingScale * bowYNoteSpace; }
    public double getBowXNoteSpace() { return staffSpacingScale * bowXNoteSpace; }
    public double getBowSystemBreakEndXMargin() { return staffSpacingScale * bowSystemBreakEndXMargin; }

    public double getSelectionFrameWidth() { return staffSpacingScale * selectionFrameWidth; }
    public double getSelectionFrameExtraHeight() { return staffSpacingScale * selectionFrameExtraHeight; }
    public double getSelectionFrameRadius() { return staffSpacingScale * selectionFrameRadius; }

    public double getEditCursorLineThickness() { return staffSpacingScale * EDIT_CURSOR_LINE_THICKNESS; }
    public double getEditCursorBoxWidth() { return staffSpacingScale * EDIT_CURSOR_BOX_WIDTH; }
    public double getEditCursorPadding() { return staffSpacingScale * EDIT_CURSOR_PADDING * staffLineSpacing; }

    public double getHeaderDefMarginTop() { return headerDefMarginTop; }
    public double getHeaderDefMarginBottom() { return headerDefMarginBottom; }
    public double getHeaderDefNumberNewFontSize() { return headerDefNumberNewFontSize; }
    public double getHeaderDefNumberOldFontSize() { return headerDefNumberOldFontSize; }
    public double getHeaderDefTitleFontSize() { return headerDefTitleFontSize; }
    public double getHeaderDefSubtitleFontSize() { return headerDefSubtitleFontSize; }
    public double getHeaderDefComposerFontSize() { return headerDefComposerFontSize; }
    public double getHeaderDefNumBoxMinWidth() { return headerDefNumBoxMinWidth; }
    public double getHeaderDefNumBoxMinHeight() { return headerDefNumBoxMinHeight; }
    public double getHeaderDefNumBoxRadius() { return headerDefNumBoxRadius; }
    public double getHeaderDefNumBoxStrokeWidth() { return headerDefNumBoxStrokeWidth; }
    public double getHeaderDefNumBoxSpacing() { return headerDefNumBoxSpacing; }
    public double getHeaderDefNumBoxPaddingX() { return headerDefNumBoxPaddingX; }
    public double getHeaderDefNumBoxPaddingY() { return headerDefNumBoxPaddingY; }

    public double getFooterDefPageNumFontSize() { return footerDefPageNumFontSize; }

    public double getFrameStrokeThickness() { return frameStrokeThickness; }
    public double getFrameStrokeDashLength() { return frameStrokeDashLength; }
    public double getFrameStrokeSpaceLength() { return frameStrokeSpaceLength; }
    public String getFrameStrokeColor() { return frameStrokeColor; }

    public double getTieMaxThickness() { return staffSpacingScale * TIE_MAX_THICKNESS; }
    public double getSlurMaxThickness() { return staffSpacingScale * SLUR_MAX_THICKNESS; }

    public double toSp(double valueInMm) {
        if (spatiumMm <= 0) return 0;
        return valueInMm / spatiumMm;
    }

    public String getSelectColor(Selectable element) {
        if (!element.isSelected()) return "#000000";

        return switch (element.getVoice()) {
            case 1 -> voice1Color;
            case 2 -> voice2Color;
            case 3 -> voice3Color;
            default -> voice4Color;
        };
    }

    public String getEditInsertColor(int voice) {
        return switch (voice) {
            case 1 -> voice1InsertColor;
            case 2 -> voice2InsertColor;
            case 3 -> voice3InsertColor;
            case 4 -> voice4InsertColor;
            default -> "#000000";
        };
    }


    public void setStaffSpacingScale(double staffSpacingScale) { this.staffSpacingScale = staffSpacingScale; }
}