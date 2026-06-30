package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Part {
    private String name;
    private final List<Staff> staves = new ArrayList<>();

    public Part(String name) {
        this.name = name;
    }

    public List<Staff> getStaves() {
        return staves;
    }

    public void add(Staff staff) {
        staves.add(staff);
    }

    public String getName() {
        return name;
    }
}