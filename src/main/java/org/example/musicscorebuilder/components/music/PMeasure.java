package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class PMeasure {
    private final List<SMeasure> stavesMeasures = new ArrayList<>();

    public void add(SMeasure sMeasure) { stavesMeasures.add(sMeasure); }

    public List<SMeasure> getSMeasures() { return stavesMeasures; }
}
