package org.example.musicscorebuilder.components.layout;

public class BraceLayout {
    private final double ICON_HEIGHT = 367.0;
    private final double ICON_WIDTH = 28.0;
    private final String PATH_DATA = "M22.4,3.2c-1.5,1.8 -5.1,8.2 -8,14.3 -10.1,20.8 -11.4,41.4 -5.4,83.5 1,7.4 2.4,20.3 3.1,28.5 1.1,13.1 1,16.3 -0.4,24.8 -1.9,10.7 -5.3,20.2 -9.3,25.5 -2.6,3.4 -2.7,3.5 -1,6.1 5,7.6 8.6,17.1 10.2,26.6 1.5,8.7 1.6,11.8 0.5,25 -0.7,8.2 -2.1,21.1 -3.1,28.5 -4.5,31.7 -4.6,50.8 -0.5,68.8 2.7,11.4 13.6,32.2 17,32.2 2.8,-0 2.5,-2.2 -1.4,-8.7 -7.6,-13 -8.6,-17.3 -8.5,-38.8 0,-15 0.6,-23.5 2.8,-40.5 2.4,-18.6 2.7,-23.8 2.3,-38.5 -0.4,-13.9 -0.9,-18.8 -3,-26.6 -2.4,-9.1 -7.7,-21.2 -11.9,-27.1l-1.9,-2.8 4,-6.7c13.4,-22.6 15.9,-46.1 10,-94.4 -5,-41.7 -3.6,-57.4 6.6,-74.7 3.3,-5.5 3.8,-8.2 1.5,-8.2 -0.4,-0 -2.1,1.5 -3.6,3.2z";
    private double height;

    public String getPath() {
        return PATH_DATA;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return ICON_WIDTH * getScale() + 1;
    }

    public double getScale() {
        return height / ICON_HEIGHT;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}