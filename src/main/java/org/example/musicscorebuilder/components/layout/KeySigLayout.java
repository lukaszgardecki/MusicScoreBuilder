package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.KeySigType;
import org.example.musicscorebuilder.components.music.Leland;

public class KeySigLayout extends ElementLayout {
    private final Leland fontData;
    private double height;
    private double[] offsetsY;
    private double[] offsetsX;

    public KeySigLayout(StaffLayout staffLayout) {
        super(false);
        this.height = staffLayout.getHeight();
        KeySigType type = staffLayout.getStaff().getDefaultKeySig().getType();
        fontData = type.getFontData();
        double[] rawOffsets = type.getOffsetsY(staffLayout.getStaff().getDefaultClef().getType());

        this.offsetsY = new double[rawOffsets.length];
        for (int i = 0; i < rawOffsets.length; i++) {
            this.offsetsY[i] = rawOffsets[i] * staffLayout.getLineSpacing() + staffLayout.getY();
        }

        this.offsetsX = new double[offsetsY.length];
        for (int i = 0; i < offsetsX.length; i++) {
            offsetsX[i] = getSignWidth() * i;
        }
    }

    public int count() { return offsetsY.length; }

    public double getFontSize() { return height; }
    public String getCode() { return fontData.getCode(); }
    public double getX(int i) { return offsetsX[i]; }
    public double getY(int i) { return offsetsY[i]; }
    public double getBoxY(int i) { return offsetsY[i] - fontData.getNEy(); }

    @Override public double getWidth() { return getSignWidth() * offsetsY.length; }
    public double getSignWidth() { return getHeight() * fontData.getRatio(); }
    public double getHeight() { return fontData.getHeight(); }
}