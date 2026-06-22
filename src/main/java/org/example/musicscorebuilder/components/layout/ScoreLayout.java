package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class ScoreLayout {
    private List<StaffLayout> staves = new ArrayList<>();

    public void add(StaffLayout stave) {
        staves.add(stave);
    }

    public List<StaffLayout> getStaves() {
        return staves;
    }
}