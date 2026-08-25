package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.KeySignature;
import org.example.musicscorebuilder.components.music.KeySigType;
import org.example.musicscorebuilder.components.music.Leland;

import java.util.Optional;

public class KeySigLayout extends ElementLayout {
    private Leland fontData = null;
    private double height;
    private KeySign[] keySigns;

    public record KeySign(double x, double y, double boxY) {}

    public KeySigLayout(KeySignature keySignature, StaffLayout staff, SegmentLayout parent) {
        super(false, parent, staff);
        this.height = staff.getHeight();
        this.setY(staff.getY());
        KeySigType type = keySignature.getType();
        if (type == null) {
            this.keySigns = new KeySign[0];
            return;
        }

        fontData = type.getFontData();
        double[] rawOffsets = type.getOffsetsY(staff.getStaff().getDefaultClef().getType());
        this.keySigns = new KeySign[rawOffsets.length];

        double startX = this.getX();
        double stepX = staff.getLineSpacing() + style.getKeySignatureSignSpace();
        for (int i = 0; i < keySigns.length; i++) {
            double x = startX + (stepX * i);
            double relativeY = (rawOffsets[i] * staff.getLineSpacing());
            double absoluteY = this.getY() + relativeY;

            keySigns[i] = new KeySign(x, absoluteY, absoluteY - (fontData.getNEy() * staff.getLineSpacing()));
        }
    }

    @Override
    public double getWidth() {
        if (keySigns == null || keySigns.length == 0) return 0.0;
        double scaledSignWidth = getSignWidth();
        double totalSpacing = (keySigns.length - 1) * (staff.getLineSpacing() + style.getKeySignatureSignSpace());
        return totalSpacing + scaledSignWidth;
    }

    @Override
    public double getHeight() {
        if (keySigns == null || keySigns.length == 0 || fontData == null) return 0.0;

        double minY = Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        boolean hasValidSigns = false;

        for (KeySign sign : keySigns) {
            if (sign != null) {
                hasValidSigns = true;
                double top = sign.boxY();
                double bottom = sign.boxY() + (fontData.getHeight() * staff.getLineSpacing());
                if (top < minY) minY = top;
                if (bottom > maxY) maxY = bottom;
            }
        }

        return hasValidSigns ? (maxY - minY) : 0.0;
    }

    @Override public double getBoxY() {
        if (keySigns == null || keySigns.length == 0 || fontData == null) return getY();

        double minY = Double.MAX_VALUE;
        boolean hasValidSigns = false;

        for (KeySign sign : keySigns) {
            if (sign != null) {
                hasValidSigns = true;
                if (sign.boxY() < minY) {
                    minY = sign.boxY();
                }
            }
        }
        return hasValidSigns ? minY : getY();
    }
    @Override public int getVoice() { return 1; }

    public double getFontSize() { return height; }
    public String getCode() { return Optional.ofNullable(fontData).map(Leland::getCode).orElse(""); }
    public double getSignWidth() {
        double scale = staff.getLineSpacing();
        double singleGlyphHeight = Optional.ofNullable(fontData).map(Leland::getHeight).orElse(0d);
        return (singleGlyphHeight * Optional.ofNullable(fontData).map(Leland::getRatio).orElse(0d)) * scale;
    }
    public KeySign[] getKeySigns() { return keySigns; }

    @Override
    public void setX(double newX) {
        double oldX = getX();
        super.setX(newX);

        double deltaX = newX - oldX;
        if (deltaX == 0 || keySigns == null) return;

        for (int i = 0; i < keySigns.length; i++) {
            KeySign oldSign = keySigns[i];
            if (oldSign == null) continue;
            keySigns[i] = new KeySign(
                    oldSign.x() + deltaX,
                    oldSign.y(),
                    oldSign.boxY()
            );
        }
    }

    @Override
    public void setY(double newY) {
        double oldY = getY();
        super.setY(newY);

        double deltaY = newY - oldY;
        if (deltaY == 0 || keySigns == null) return;

        for (int i = 0; i < keySigns.length; i++) {
            KeySign oldSign = keySigns[i];
            if (oldSign == null) continue;
            keySigns[i] = new KeySign(
                    oldSign.x(),
                    oldSign.y() + deltaY,
                    oldSign.boxY() + deltaY
            );
        }
    }
}