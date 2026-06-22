package org.example.musicscorebuilder.components.music;

public class Page {
    double width = 794;
    double height = 1123;

    double marginTop = 94;
    double marginBottom = 94;
    double marginLeft = 94;
    double marginRight = 94;

    public double getWidth() {
        return width;
    }

    public double getEffectiveWidth() {
        return width - marginLeft - marginRight;
    }

    public double getHeight() {
        return height;
    }

    public double getEffectiveHeight() {
        return height - marginTop - marginBottom;
    }

    public double getMarginTop() {
        return marginTop;
    }

    public double getMarginBottom() {
        return marginBottom;
    }

    public double getMarginLeft() {
        return marginLeft;
    }

    public double getMarginRight() {
        return marginRight;
    }
}
