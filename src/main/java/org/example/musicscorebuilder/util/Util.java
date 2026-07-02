package org.example.musicscorebuilder.util;

import javafx.scene.paint.Color;

import java.util.Random;

public class Util {

    public static Color generateRandomColor() {
        Random rand = new Random();
        return Color.rgb(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256), 0.2);
    }
}
