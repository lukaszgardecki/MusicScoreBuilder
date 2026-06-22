package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class ScoreLayout {
    private final List<PartLayout> parts = new ArrayList<>();
    private double y = 0;

    public void add(PartLayout stave) {
        parts.add(stave);
    }

    public List<PartLayout> getParts() {
        return parts;
    }

    public double getY() {
        return y;
    }
}