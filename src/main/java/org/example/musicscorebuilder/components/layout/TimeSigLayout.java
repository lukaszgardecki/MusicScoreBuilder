package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.TimeSignature;

public class TimeSigLayout extends ElementLayout {
    private final DigitSign[][] digitSigns;
    private final double width, height;
    private double y;
    private final double scale;

    public record DigitSign(Leland fontData, double x, double y, double scale) {
        public double getSignWidth() { return getHeight() * fontData.getRatio(); }
        public double getHeight() { return fontData.getHeight() * scale; }
        public double getBoxY() { return y - (fontData().getNEy() * scale); }
    }

    public TimeSigLayout(TimeSignature timeSignature, StaffLayout staff, SegmentLayout parent) {
        super(false, parent, staff);
        this.height = staff.getHeight();
        this.y = staff.getY();
        this.scale = staff.getLineSpacing();
        double signOffsetY = 1 * staff.getLineSpacing();

        if (timeSignature.isFractional()) {
            int beat = timeSignature.getBeat();
            int beatType = timeSignature.getBeatType();

            int[] beatDigits = getDigitsMath(beat);
            int[] beatTypeDigits = getDigitsMath(beatType);

            this.digitSigns = new DigitSign[2][];
            double y = staff.getY() + signOffsetY;
            this.digitSigns[0] = createDigitRow(beatDigits, y);
            double bottomY = y + 2 * staff.getLineSpacing();
            this.digitSigns[1] = createDigitRow(beatTypeDigits, bottomY);

            double topWidth = getRowWidth(this.digitSigns[0]);
            double bottomWidth = getRowWidth(this.digitSigns[1]);
            this.width = Math.max(topWidth, bottomWidth);

            alignRowsCenter(topWidth, bottomWidth);
        } else {
            Leland symbol = timeSignature.isCommon() ? Leland.TIME_COMMON : Leland.TIME_CUT;
            double y = staff.getY() + signOffsetY + staff.getLineSpacing();
            this.digitSigns = new DigitSign[1][1];
            DigitSign sign = new DigitSign(symbol, this.getX(), y, scale);
            this.digitSigns[0][0] = sign;
            this.width = sign.getSignWidth();
        }
    }

    @Override public double getY() { return y; }
    @Override public double getBoxY() { return getY(); }
    @Override public double getWidth() { return this.width; }
    @Override public double getHeight() { return height; }
    @Override public int getVoice() { return 1; }

    public double getFontSize() { return height; }
    public DigitSign[][] getDigitSigns() { return this.digitSigns; }

    @Override
    public void setX(double newX) {
        double oldX = getX();
        super.setX(newX);

        double deltaX = newX - oldX;
        if (deltaX == 0) return;

        for (int row = 0; row < digitSigns.length; row++) {
            for (int col = 0; col < digitSigns[row].length; col++) {
                DigitSign oldSign = digitSigns[row][col];
                digitSigns[row][col] = new DigitSign(
                        oldSign.fontData(),
                        oldSign.x() + deltaX,
                        oldSign.y(),
                        scale
                );
            }
        }
    }

    private int[] getDigitsMath(int number) {
        if (number == 0) return new int[]{0};

        number = Math.abs(number);
        int length = (int) Math.log10(number) + 1;
        int[] digits = new int[length];

        for (int i = length - 1; i >= 0; i--) {
            digits[i] = number % 10;
            number /= 10;
        }

        return digits;
    }

    private DigitSign[] createDigitRow(int[] digits, double y) {
        DigitSign[] row = new DigitSign[digits.length];
        double currentX = this.getX();

        for (int i = 0; i < digits.length; i++) {
            Leland fontData = getDigitFontData(digits[i]);
            var sign = new DigitSign(fontData, currentX, y, scale);
            row[i] = sign;
            currentX += sign.getSignWidth();
        }
        return row;
    }

    private double getRowWidth(DigitSign[] row) {
        if (row.length == 0) return 0.0;
        DigitSign lastSign = row[row.length - 1];
        return (lastSign.x() - this.getX()) + lastSign.getSignWidth();
    }

    private void alignRowsCenter(double topWidth, double bottomWidth) {
        if (topWidth == bottomWidth) return;

        if (topWidth < bottomWidth) {
            double shift = (bottomWidth - topWidth) / 2.0;
            shiftRow(this.digitSigns[0], shift);
        } else {
            double shift = (topWidth - bottomWidth) / 2.0;
            shiftRow(this.digitSigns[1], shift);
        }
    }

    private void shiftRow(DigitSign[] row, double shift) {
        for (int i = 0; i < row.length; i++) {
            row[i] = new DigitSign(row[i].fontData(), row[i].x() + shift, row[i].y(), scale);
        }
    }

    private Leland getDigitFontData(int digit) {
        return switch (digit) {
            case 0 -> Leland.TIME_0;
            case 1 -> Leland.TIME_1;
            case 2 -> Leland.TIME_2;
            case 3 -> Leland.TIME_3;
            case 4 -> Leland.TIME_4;
            case 5 -> Leland.TIME_5;
            case 6 -> Leland.TIME_6;
            case 7 -> Leland.TIME_7;
            case 8 -> Leland.TIME_8;
            case 9 -> Leland.TIME_9;
            default -> throw new IllegalStateException("Unexpected digit value: " + digit);
        };
    }
}