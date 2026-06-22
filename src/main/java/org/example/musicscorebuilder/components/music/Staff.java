package org.example.musicscorebuilder.components.music;

public class Staff {
    private int linesNumber = 5;
    private double lineWidth = 0.9;
    //    private List<Voice> voices = new ArrayList<>();
//    private List<ClefChange> clefChanges = new ArrayList<>();

    double lineSpacing = 7;

    public int getLinesNumber() {
        return linesNumber;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }
}