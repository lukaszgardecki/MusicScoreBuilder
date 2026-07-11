package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Part {
    private String name;
    private BraceType braceType = BraceType.BRACE;
    private final List<Staff> staves = new ArrayList<>();

    public Part(String name) {
        this.name = name;
    }

    public void add(Staff staff) { staves.add(staff); }

    public String getName() { return name; }
    public BraceType getBraceType() { return braceType; }
    public List<Staff> getStaves() { return staves; }
}