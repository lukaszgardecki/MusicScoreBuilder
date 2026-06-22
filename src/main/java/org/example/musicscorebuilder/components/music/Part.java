package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Part {
    private String name;
    private double staffSpacing = 40;
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

    public double getStaffSpacing() {
        return staffSpacing;
    }

    public String getName() {
        return name;
    }
}