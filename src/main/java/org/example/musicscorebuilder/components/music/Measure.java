package org.example.musicscorebuilder.components.music;

import java.util.ArrayList;
import java.util.List;

public class Measure {
    private Barline rightBarline;
    private final List<Staff> staves;
    private final List<Segment> segments = new ArrayList<>();

    public Measure(BarlineStyle barlineStyle, List<Staff> staves) {
        this.staves = staves;
        this.rightBarline = new Barline(barlineStyle, Barline.Type.END);
    }

    public void addChordRestSegmentAtEnd() {
        if (segments.isEmpty()) {
            segments.add(new Segment(SegmentType.CHORDREST));
        } else {
            segments.add(segments.size() - 1, new Segment(SegmentType.CHORDREST));
        }
    }

    public void addEndBarlineSegment(BarlineStyle style) {
        var element = new Barline(style, Barline.Type.END);
        Segment seg = new Segment(SegmentType.BARLINE);
        seg.addElement(element);
        segments.add(seg);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
}