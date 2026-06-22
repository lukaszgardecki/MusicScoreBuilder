package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Score {
    private String title;
    private String composer;
    private List<Staff> staves = new ArrayList<>();
//    private List<Part> parts = new ArrayList<>();

    public Score() {
        staves.add(new Staff());
    }

    public List<Staff> getStaves() {
        return staves;
    }

    public void add(Staff staff) {
        staves.add(staff);
    }
}

