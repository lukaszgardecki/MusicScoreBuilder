package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

public class BowCurveGeometry {
    private final double startX, startY;
    private final double endX, endY;
    private final double dx;
    private final double height;
    private final double thickness;

    private final double cp1x, cp2x;
    private final double cp1yOuter, cp2yOuter;
    private final double cp1yInner, cp2yInner;

    public BowCurveGeometry(double startX, double startY, double endX, double endY, boolean curveUp, ScoreStyle style) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        this.dx = endX - startX;

        if (this.dx <= 0) {
            this.height = 0;
            this.thickness = 0;
            this.cp1x = 0; this.cp2x = 0;
            this.cp1yOuter = 0; this.cp2yOuter = 0;
            this.cp1yInner = 0; this.cp2yInner = 0;
            return;
        }

        this.height = Math.min(style.getBowHeightFactor(), dx * style.getBowMaxDxRatio());
        this.thickness = Math.min(style.getBowMidThickness(), this.height * 0.55);

        double dir = curveUp ? -1.0 : 1.0;

        this.cp1x = startX + (dx * 0.25);
        this.cp2x = startX + (dx * 0.75);

        this.cp1yOuter = startY + (dir * this.height);
        this.cp2yOuter = endY + (dir * this.height);

        this.cp1yInner = startY + (dir * (this.height - this.thickness));
        this.cp2yInner = endY + (dir * (this.height - this.thickness));
    }

    public double findTForX(double targetX) {
        double low = 0.0;
        double high = 1.0;
        double t = 0.5;

        for (int i = 0; i < 24; i++) {
            t = (low + high) / 2.0;
            double currentX = calculateBezierCoordinate(t, startX, cp1x, cp2x, endX);

            if (currentX < targetX) {
                low = t;
            } else {
                high = t;
            }
        }
        return t;
    }

    public double calculateBezierCoordinate(double t, double p0, double p1, double p2, double p3) {
        double u = 1 - t;
        double tt = t * t;
        double uu = u * u;
        double uuu = uu * u;
        double ttt = tt * t;

        return (uuu * p0) + (3 * uu * t * p1) + (3 * u * tt * p2) + (ttt * p3);
    }

    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getEndX() { return endX; }
    public double getEndY() { return endY; }

    public double getDx() { return dx; }
    public double getHeight() { return height; }
    public double getThickness() { return thickness; }

    public double getCp1x() { return cp1x; }
    public double getCp2x() { return cp2x; }

    public double getCp1yOuter() { return cp1yOuter; }
    public double getCp2yOuter() { return cp2yOuter; }

    public double getCp1yInner() { return cp1yInner; }
    public double getCp2yInner() { return cp2yInner; }
}