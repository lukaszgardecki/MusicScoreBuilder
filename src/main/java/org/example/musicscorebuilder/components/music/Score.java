package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String title;
    private String composer;
    private final List<Part> parts = new ArrayList<>();
    private double partSpacing = 60;

    public Score(String title, String composer) {
        this.title = title;
        this.composer = composer;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void add(Part part) {
        parts.add(part);
    }

    public void removeLast() {
        if (parts.isEmpty()) return;
        parts.removeLast();
    }

    public double getPartSpacing() {
        return partSpacing;
    }

    public String getTitle() {
        return title;
    }

    public String getComposer() {
        return composer;
    }
}

