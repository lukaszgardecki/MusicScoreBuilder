package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.KeySignature;
import org.example.musicscorebuilder.components.music.KeySigType;
import org.example.musicscorebuilder.components.music.Leland;

public class KeySigLayout extends ElementLayout {
    private final Leland fontData;
    private double height;
    private KeySign[] keySigns;
    private final double leftMargin = 0.85;

    public record KeySign(double x, double y, double boxY) {}

    public KeySigLayout(KeySignature keySignature, StaffLayout staffLayout) {
        super(false);
        this.height = staffLayout.getHeight();
        KeySigType type = keySignature.getType();
        fontData = type.getFontData();
        double[] rawOffsets = type.getOffsetsY(staffLayout.getStaff().getDefaultClef().getType());

        this.keySigns = new KeySign[rawOffsets.length];
        for (int i = 0; i < keySigns.length; i++) {
            double x = getSignWidth() * i + leftMargin;
            double y = rawOffsets[i] * staffLayout.getLineSpacing() + staffLayout.getY();
            keySigns[i] = new KeySign(x, y, y - fontData.getNEy());
        }
    }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
    public KeySign[] getKeySigns() { return keySigns; }
    @Override public double getWidth() { return getSignWidth() * keySigns.length + leftMargin; }
    public double getSignWidth() { return getHeight() * fontData.getRatio(); }
    public double getHeight() { return fontData.getHeight(); }
}