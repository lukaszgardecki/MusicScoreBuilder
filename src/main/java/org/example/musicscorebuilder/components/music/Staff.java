package org.example.musicscorebuilder.components.music;

public class Staff {
    private final int linesNumber;
    private double lineWidth = 0.9;
    //    private List<Voice> voices = new ArrayList<>();
//    private List<ClefChange> clefChanges = new ArrayList<>();
    double lineSpacing = 7;

    public Staff() {
        this.linesNumber = 5;
    }

    public Staff(int linesNumber) {
        this.linesNumber = linesNumber;
    }

    public int getLinesNumber() {
        return linesNumber;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public double getLineSpacing() {
        return lineSpacing;
    }

    public double getHeight() {
        return (linesNumber - 1) * lineSpacing;
    }
}