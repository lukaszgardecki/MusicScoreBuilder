package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.NoteType;
import org.example.musicscorebuilder.components.music.Rest;

public class RestLayout extends ElementLayout {
    private final Leland fontData;
    private final Rest rest;
    private final double height;
    private double y;

    public RestLayout(Rest rest, StaffLayout staff, SegmentLayout parent) {
        super(true, parent, staff);
        this.rest = rest;
        this.height = staff.getHeight();
        this.fontData = switch (rest.getType()) {
            case WHOLE -> Leland.REST_WHOLE;
            case HALF -> Leland.REST_HALF;
            case QUARTER -> Leland.REST_QUARTER;
            case EIGHTH -> Leland.REST_8TH;
            case SIXTEENTH -> Leland.REST_16TH;
            case THIRTY_SECOND -> Leland.REST_32ND;
        };
        y = (rest.getType() == NoteType.WHOLE ? 1 : 2) * staff.getLineSpacing() + staff.getY();
    }

    @Override public double getY() { return y; }
    @Override public double getWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    @Override public double getHeight() { return fontData.getHeight() * style.getStaffLineSpacing(); }
    @Override public double getBoxY() { return getY() - (fontData.getNEy() * style.getStaffLineSpacing()); }
    @Override public int getVoice() { return rest.getVoice(); }

    public double getBoxX() { return getX(); }
    public double getFontSize() { return height; }
    public double getFontWidth() { return (fontData.getHeight() * fontData.getRatio()) * style.getStaffLineSpacing(); }
    public double getBoxWidth() { return getFontWidth(); }
    public String getCode() { return fontData.getCode(); }
}