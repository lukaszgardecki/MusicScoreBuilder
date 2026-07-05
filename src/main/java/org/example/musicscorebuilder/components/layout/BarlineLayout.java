package org.example.musicscorebuilder.components.layout;

public class BarlineLayout {
    private double x;
    private double height;

    public BarlineLayout() {}
    public BarlineLayout(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getHeight() {
        return height;
    }

    public void addHeight(double height) {
        this.height += height;
    }
}
