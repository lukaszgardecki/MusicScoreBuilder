package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.KeySignature;
import org.example.musicscorebuilder.components.music.KeySigType;
import org.example.musicscorebuilder.components.music.Leland;

import java.util.Optional;

public class KeySigLayout extends ElementLayout {
    private Leland fontData = null;
    private double height;
    private double[] xPositions;
    private double[] relativeY;
    private double[] relativeBoxY;

    public record KeySign(double x, double y, double boxY) {}

    public KeySigLayout(KeySignature keySignature, StaffLayout staff, SegmentLayout parent) {
        super(false, parent, staff);
        this.height = staff.getHeight();

        KeySigType type = keySignature.getType();
        if (type == null) {
            this.xPositions = new double[0];
            this.relativeY = new double[0];
            this.relativeBoxY = new double[0];
            return;
        }

        fontData = type.getFontData();
        double[] rawOffsets = type.getOffsetsY(staff.getStaff().getDefaultClef().getType());

        int count = rawOffsets.length;
        this.xPositions = new double[count];
        this.relativeY = new double[count];
        this.relativeBoxY = new double[count];

        double startX = this.getX();
        double stepX = staff.getLineSpacing() + style.getKeySignatureSignSpace();
        for (int i = 0; i < count; i++) {
            this.xPositions[i] = startX + (stepX * i);
            this.relativeY[i] = rawOffsets[i] * staff.getLineSpacing();
            this.relativeBoxY[i] = this.relativeY[i] - (fontData.getNEy() * staff.getLineSpacing());
        }
    }

    @Override
    public double getY() {
        return staff != null ? staff.getY() : super.getY();
    }

    @Override
    public double getWidth() {
        if (xPositions == null || xPositions.length == 0) return 0.0;
        double scaledSignWidth = getSignWidth();
        double totalSpacing = (xPositions.length - 1) * (staff.getLineSpacing() + style.getKeySignatureSignSpace());
        return totalSpacing + scaledSignWidth;
    }

    @Override
    public double getHeight() {
        if (relativeBoxY == null || relativeBoxY.length == 0 || fontData == null) return 0.0;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        boolean hasValidSigns = false;

        for (double relBoxY : relativeBoxY) {
            hasValidSigns = true;
            double top = relBoxY;
            double bottom = relBoxY + (fontData.getHeight() * staff.getLineSpacing());
            if (top < minY) minY = top;
            if (bottom > maxY) maxY = bottom;
        }

        return hasValidSigns ? (maxY - minY) : 0.0;
    }

    @Override
    public double getBoxY() {
        if (relativeBoxY == null || relativeBoxY.length == 0 || fontData == null) return getY();
        double minY = Double.MAX_VALUE;

        for (double relBoxY : relativeBoxY) {
            if (relBoxY < minY) {
                minY = relBoxY;
            }
        }
        return getY() + minY;
    }

    @Override
    public int getVoice() { return 1; }

    public double getFontSize() {
        return staff != null ? staff.getHeight() : height;
    }

    public String getCode() {
        return Optional.ofNullable(fontData).map(Leland::getCode).orElse("");
    }

    public double getSignWidth() {
        double scale = staff.getLineSpacing();
        double singleGlyphHeight = Optional.ofNullable(fontData).map(Leland::getHeight).orElse(0d);
        return (singleGlyphHeight * Optional.ofNullable(fontData).map(Leland::getRatio).orElse(0d)) * scale;
    }

    public KeySign[] getKeySigns() {
        if (xPositions == null || xPositions.length == 0) return new KeySign[0];

        double currentY = getY();
        KeySign[] signs = new KeySign[xPositions.length];
        for (int i = 0; i < xPositions.length; i++) {
            signs[i] = new KeySign(
                    xPositions[i],
                    currentY + relativeY[i],
                    currentY + relativeBoxY[i]
            );
        }
        return signs;
    }

    @Override
    public void setX(double newX) {
        double oldX = getX();
        super.setX(newX);

        double deltaX = newX - oldX;
        if (deltaX == 0 || xPositions == null) return;

        for (int i = 0; i < xPositions.length; i++) {
            xPositions[i] += deltaX;
        }
    }

    @Override
    public void setY(double newY) {
        super.setY(newY);
    }
}