package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.NoteType;
import org.example.musicscorebuilder.components.music.Rest;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.List;

public class RestLayout extends ElementLayout {
    private final Leland fontData;
    private final Rest rest;
    private final double height;
    private double y;

    public RestLayout(Rest rest, StaffLayout staff, SegmentLayout parent) {
        super(true, parent, staff);
        this.rest = rest;
        this.height = staff.getHeight();
        double yOffset = calculateYOffset();
        this.y = yOffset * staff.getLineSpacing() + staff.getY();
        this.fontData = resolveFontData(rest.getType(), yOffset);
    }

    @Override
    public double getX() {
        SegmentLayout segment = getParent();
        MeasureLayout measureLayout = segment.getParent();

        if (rest.getType() == NoteType.WHOLE && isOnlyElementInVoice(measureLayout)) {
            List<SegmentLayout> noteRestSegments = measureLayout.getSegments().stream()
                    .filter(seg -> seg.getType() == SegmentType.NOTEREST)
                    .toList();

            if (!noteRestSegments.isEmpty()) {
                SegmentLayout firstNoteRestSeg = noteRestSegments.getFirst();
                double totalNoteRestWidth = noteRestSegments.stream()
                        .mapToDouble(SegmentLayout::getWidth)
                        .sum();

                double restWidth = getFontWidth();
                double offsetFromFirst = segment.getX() - firstNoteRestSeg.getX();
                double barlineRMargin = segment.getScoreStyle().getSegmentBarlineRightMargin();
                return Math.max(0, (totalNoteRestWidth - restWidth - barlineRMargin) / 2.0 - offsetFromFirst);
            }
        }
        return 0.0;
    }

    private boolean isOnlyElementInVoice(MeasureLayout measureLayout) {
        long count = measureLayout.getSegments().stream()
                .filter(seg -> seg.getType() == SegmentType.NOTEREST)
                .flatMap(seg -> seg.getElementsByStaff(getStaff()).stream())
                .filter(elem -> elem.getVoice() == getVoice())
                .count();

        return count <= 1;
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    @Override public double getHeight() { return fontData.getHeight() * style.getStaffLineSpacing(); }
    @Override public double getBoxY() { return getY() - (fontData.getNEy() * style.getStaffLineSpacing()); }
    @Override public int getVoice() { return rest.getVoice(); }

    public Rest getRest() { return rest; }
    public double getBoxX() { return getX(); }
    public double getFontSize() { return height; }
    public double getFontWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getBoxWidth() { return getFontWidth(); }
    public String getCode() { return fontData.getCode(); }

    private Leland resolveFontData(NoteType type, double yOffset) {
        boolean isOutsideStaff = yOffset < 0.0 || yOffset > 4.0;

        return switch (type) {
            case WHOLE -> isOutsideStaff ? Leland.REST_WHOLE_LEDGER_LINE : Leland.REST_WHOLE;
            case HALF -> isOutsideStaff ? Leland.REST_HALF_LEDGER_LINE : Leland.REST_HALF;
            case QUARTER -> Leland.REST_QUARTER;
            case EIGHTH -> Leland.REST_8TH;
            case SIXTEENTH -> Leland.REST_16TH;
            case THIRTY_SECOND -> Leland.REST_32ND;
        };
    }

    private double calculateYOffset() {
        var restType = rest.getType();
        double baseOffset = restType == NoteType.WHOLE ? 1.0 : 2.0;

        SegmentLayout segment = getParent();
        if (segment == null) return baseOffset;
        MeasureLayout measureLayout = segment.getParent();
        if (measureLayout == null) return baseOffset;

        int totalVoices = measureLayout.getVoiceCountForStaff(getStaff());
        if (totalVoices <= 1) return baseOffset;

        int v = getVoice();

        return baseOffset + switch (totalVoices) {
            case 2 -> {
                if (v <= 1) yield switch(restType) {
                    case NoteType.WHOLE, NoteType.HALF, NoteType.EIGHTH -> -1.0;
                    default -> -2.0;
                };
                yield switch(restType) {
                    case NoteType.HALF -> 1.0;
                    case NoteType.THIRTY_SECOND -> 3.0;
                    default -> 2.0;
                };
            }
            case 3 -> {
                if (v <= 1) yield switch(restType) {
                    case NoteType.WHOLE, NoteType.HALF, NoteType.EIGHTH -> -3.0;
                    default -> -4.0;
                };
                if (v == 2) yield baseOffset;
                yield restType == NoteType.HALF ? 3.0 : 4.0;
            }
            default -> {
                if (v <= 1) yield switch(restType) {
                    case NoteType.WHOLE, NoteType.HALF -> -4.0;
                    case NoteType.EIGHTH -> -5.0;
                    default -> -6.0;
                };
                if (v == 2) yield switch(restType) {
                    case NoteType.WHOLE, NoteType.HALF, NoteType.EIGHTH -> -1.0;
                    default -> -2.0;
                };
                if (v == 3) yield switch(restType) {
                    case NoteType.HALF -> 1.0;
                    case NoteType.THIRTY_SECOND -> 3.0;
                    default -> 2.0;
                };
                yield switch(restType) {
                    case NoteType.HALF -> 5.0;
                    case NoteType.THIRTY_SECOND -> 7.0;
                    default -> 6.0;
                };
            }
        };
    }
}