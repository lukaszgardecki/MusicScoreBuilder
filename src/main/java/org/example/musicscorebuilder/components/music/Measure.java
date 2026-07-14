package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Measure {
    private TimeSignature timeSignature = new TimeSignature(4);
    private Barline rightBarline;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();

    public Measure(BarlineStyle barlineStyle, List<Staff> staves) {
        this.staves = staves;
        this.rightBarline = new Barline(barlineStyle, Barline.Type.END);
    }

    public void add(Element element) {
        Segment seg = new Segment();
        seg.addElement(element);
        segments.add(seg);
    }

    public TimeSignature getTimeSignature() { return timeSignature; }
    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
}