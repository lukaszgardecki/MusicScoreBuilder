package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.KeySignature;
import org.example.musicscorebuilder.components.music.KeySigType;
import org.example.musicscorebuilder.components.music.Leland;

import java.util.Optional;

public class KeySigLayout extends ElementLayout {
    private Leland fontData = null;
    private double height;
    private KeySign[] keySigns;
    private final double leftMargin = 0.85;

    public record KeySign(double x, double y, double boxY) {}

    public KeySigLayout(KeySignature keySignature, StaffLayout staffLayout) {
        super(false);
        this.height = staffLayout.getHeight();
        KeySigType type = keySignature.getType();

        if (type == null) {
            this.keySigns = new KeySign[0];
            return;
        }

        fontData = type.getFontData();
        double[] rawOffsets = type.getOffsetsY(staffLayout.getStaff().getDefaultClef().getType());
        this.keySigns = new KeySign[rawOffsets.length];

        double signWidth = getSignWidth();
        for (int i = 0; i < keySigns.length; i++) {
            double x = signWidth * i + leftMargin;
            double y = (rawOffsets[i] * staffLayout.getLineSpacing()) + staffLayout.getY();
            keySigns[i] = new KeySign(x, y, y - fontData.getNEy());
        }
    }

    public double getFontSize() { return height; }
    public String getCode() { return Optional.ofNullable(fontData).map(Leland::getCode).orElse(""); }
    public double getSignWidth() { return getHeight() * Optional.ofNullable(fontData).map(Leland::getRatio).orElse(0d); }
    public double getHeight() { return Optional.ofNullable(fontData).map(Leland::getHeight).orElse(0d); }
    public KeySign[] getKeySigns() { return keySigns; }

    @Override
    public double getWidth() {
        var margin = keySigns.length > 0 ? leftMargin : 0;
        return getSignWidth() * keySigns.length + margin;
    }
}