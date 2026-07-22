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

    public void addEndBarlineSegment(BarlineStyle style) {
        var element = new Barline(style, Barline.Type.END);
        Segment seg = new Segment(SegmentType.BARLINE, staves);

        for (Staff staff : staves) {
            seg.addElement(staff, element);
        }
        segments.add(seg);
    }

    public List<Staff> getStaves() { return staves; }
    public List<Segment> getSegments() { return segments; }
    public int countVoicesByStaff(Staff staff) {
        return segments.stream()
                .mapToInt(s -> s.getVoiceCountByStaff(staff))
                .max()
                .orElse(0);
    }

    public void addChordRestSegmentAtEnd() {
        Segment seg = new Segment(SegmentType.CHORDREST, staves);

        var staff1 = staves.get(0);
        seg.addElement(staff1, new Note(1, PitchStep.C, 0, 4, NoteType.getRandom(), BeamType.NONE));
        seg.addElement(staff1, new Note(1, PitchStep.G, 0, 4, NoteType.getRandom(), BeamType.NONE));

        if (staves.size() == 2) {
            var staff2 = staves.get(1);
            seg.addElement(staff2, new Note(1, PitchStep.A, 0, 3, NoteType.getRandom(), BeamType.NONE));
            seg.addElement(staff2, new Note(1, PitchStep.D, 0, 3, NoteType.getRandom(), BeamType.NONE));
        }

        if (segments.isEmpty()) {
            segments.add(seg);
        } else {
            segments.add(segments.size() - 1, seg);
        }
    }
}