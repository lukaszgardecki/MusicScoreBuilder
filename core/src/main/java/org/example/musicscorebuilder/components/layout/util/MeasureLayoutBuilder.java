//package org.example.musicscorebuilder.components.layout.util;
//
//import org.example.musicscorebuilder.components.layout.*;
//import org.example.musicscorebuilder.components.layout.engine.LayoutContext;
//import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
//import org.example.musicscorebuilder.components.music.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class MeasureLayoutBuilder {
//
//    public MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout, ScoreStyle style, LayoutContext layoutContext, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
//        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout, style);
//
//        List<Staff> staves = measure.getStaves();
//        for (int i = 0; i < staves.size(); i++) {
//            measureLayout.add(new StaffLayout(staves.get(i), measureLayout, style));
//        }
//
//        addPermanentAttributesIfNeeded(measure, measureLayout);
//        GroupBeamBuilder groupBeamBuilder = populateMeasureSegments(measure, measureLayout, layoutContext, tieBuilder, slurBuilder);
//        addEndBarline(measure, measureLayout);
//
//        measureLayout.setBeamGroups(groupBeamBuilder.build());
//        return measureLayout;
//    }
//
//    public void add1stMeasureAttributes(Barline startBarline, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
//        boolean isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;
//        Measure measure = measureLayout.getMeasure();
//
//        if (isFirstMeasure && measure.getTimeSignature() != null && measure.getTimeSignature().isVisible()) {
//            measureLayout.addSystemTimeSignature(measure.getTimeSignature());
//        }
//
//        Measure prevMeasure = measure.getPrev();
//        boolean isKeyChange = prevMeasure != null
//                && measure.getKeySignature() != null
//                && prevMeasure.getKeySignature() != null
//                && !measure.getKeySignature().equals(prevMeasure.getKeySignature());
//
//        if (!isKeyChange) {
//            measureLayout.addSystemKeySignature(measure.getKeySignature());
//        }
//
//        measureLayout.addSystemClef();
//        if (startBarline != null) {
//            measureLayout.addSystemStartBarline(startBarline);
//        }
//    }
//
//    public void extractNotesToCacheMaps(MeasureLayout measureLayout, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
//        List<SegmentLayout> segments = measureLayout.getSegments();
//        for (int s = 0; s < segments.size(); s++) {
//            SegmentLayout segment = segments.get(s);
//            List<ElementLayout> elements = segment.getElements();
//            for (int e = 0; e < elements.size(); e++) {
//                ElementLayout element = elements.get(e);
//                if (element instanceof NoteLayout noteLayout) {
//                    slurBuilder.putNote(noteLayout.getNote(), noteLayout);
//                    if (noteLayout.getNote().isTieStart()) {
//                        tieBuilder.addNoteLayout(noteLayout);
//                    }
//                }
//            }
//        }
//    }
//
//    public void addEndBarline(Measure measure, MeasureLayout measureLayout) {
//        Segment endBarlineSegment = new Segment(SegmentType.BARLINE, measure);
//        SegmentLayout endBarlineSegLayout = new SegmentLayout(endBarlineSegment, measureLayout);
//        List<StaffLayout> staffs = measureLayout.getStaffs();
//        for (int i = 0; i < staffs.size(); i++) {
//            StaffLayout staffLayout = staffs.get(i);
//            endBarlineSegment.addElement(staffLayout.getStaffIndex(), measure.getRightBarline());
//            endBarlineSegLayout.addByStaff(staffLayout, new BarlineLayout(measure.getRightBarline(), staffLayout, endBarlineSegLayout));
//        }
//        measureLayout.add(endBarlineSegLayout);
//    }
//
//    private void addPermanentAttributesIfNeeded(Measure measure, MeasureLayout measureLayout) {
//        Measure prevMeasure = measure.getPrev();
//        if (prevMeasure == null) return;
//
//        if (measure.getKeySignature() != null && prevMeasure.getKeySignature() != null
//                && !measure.getKeySignature().equals(prevMeasure.getKeySignature())) {
//            SegmentLayout segment = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
//            segment.addKeySignature(measure.getKeySignature());
//            measureLayout.add(segment);
//        }
//
//        TimeSignature currentTS = measure.getTimeSignature();
//        TimeSignature prevTS = prevMeasure.getTimeSignature();
//
//        if (currentTS != null && currentTS.isVisible() && prevTS != null) {
//            if (!currentTS.equals(prevTS)) {
//                SegmentLayout segment = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
//                segment.addTimeSignature(currentTS);
//                measureLayout.add(segment);
//            }
//        }
//    }
//
//    private GroupBeamBuilder populateMeasureSegments(Measure measure, MeasureLayout measureLayout, LayoutContext layoutContext, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
//        GroupBeamBuilder groupBeamBuilder = new GroupBeamBuilder();
//        List<Segment> segments = measure.getSegments();
//        List<StaffLayout> staffs = measureLayout.getStaffs();
//
//        for (int s = 0; s < segments.size(); s++) {
//            Segment segment = segments.get(s);
//            SegmentLayout segmentLayout = new SegmentLayout(segment, measureLayout);
//
//            for (int st = 0; st < staffs.size(); st++) {
//                StaffLayout staff = staffs.get(st);
//                List<Element> elements = segment.getElementsByStaff(staff.getStaffIndex());
//                for (int e = 0; e < elements.size(); e++) {
//                    Element element = elements.get(e);
//                    if (element instanceof Barline barline) {
//                        segmentLayout.addByStaff(staff, new BarlineLayout(barline, staff, segmentLayout));
//                    } else if (element instanceof Note note) {
//                        NoteLayout noteLayout = new NoteLayout(note, staff, segmentLayout);
//                        List<LyricLayout> lyrics = createSingleLyricLine(noteLayout, layoutContext.activeVerse());
//                        noteLayout.setLyrics(lyrics);
//                        segmentLayout.addByStaff(staff, noteLayout);
//
//                        slurBuilder.putNote(note, noteLayout);
//                        if (note.isTieStart()) {
//                            tieBuilder.addNoteLayout(noteLayout);
//                        }
//
//                        if (note.isBeamed()) groupBeamBuilder.add(noteLayout);
//                    } else if (element instanceof Rest rest) {
//                        RestLayout restLayout = new RestLayout(rest, staff, segmentLayout);
//                        segmentLayout.addByStaff(staff, restLayout);
//                    }
//                }
//            }
//            measureLayout.add(segmentLayout);
//        }
//        return groupBeamBuilder;
//    }
//
//    private List<LyricLayout> createSingleLyricLine(NoteLayout noteLayout, int verseNumber) {
//        List<LyricLayout> result = new ArrayList<>();
//        if (noteLayout == null || noteLayout.getNote() == null) return result;
//
//        Note note = noteLayout.getNote();
//        Lyric lyric = note.getLyric(verseNumber);
//
//        if (lyric != null) {
//            boolean hasText = lyric.getText() != null && !lyric.getText().trim().isEmpty();
//            if (hasText || lyric.isConnected()) {
//                result.add(new LyricLayout(lyric, noteLayout));
//            }
//        }
//
//        return result;
//    }
//}

package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.LayoutContext;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MeasureLayoutBuilder {

    public MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout, ScoreStyle style, LayoutContext layoutContext, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout, style);

        List<Staff> staves = measure.getStaves();
        int staffCount = staves.size();
        for (int i = 0; i < staffCount; i++) {
            measureLayout.add(new StaffLayout(staves.get(i), measureLayout, style));
        }

        addPermanentAttributesIfNeeded(measure, measureLayout);
        GroupBeamBuilder groupBeamBuilder = populateMeasureSegments(measure, measureLayout, layoutContext, tieBuilder, slurBuilder);
        addEndBarline(measure, measureLayout);

        measureLayout.setBeamGroups(groupBeamBuilder.build());
        return measureLayout;
    }

    public void add1stMeasureAttributes(Barline startBarline, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        boolean isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;
        Measure measure = measureLayout.getMeasure();

        if (isFirstMeasure && measure.getTimeSignature() != null && measure.getTimeSignature().isVisible()) {
            measureLayout.addSystemTimeSignature(measure.getTimeSignature());
        }

        Measure prevMeasure = measure.getPrev();
        boolean isKeyChange = prevMeasure != null
                && measure.getKeySignature() != null
                && prevMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(prevMeasure.getKeySignature());

        if (!isKeyChange) {
            measureLayout.addSystemKeySignature(measure.getKeySignature());
        }

        measureLayout.addSystemClef();
        if (startBarline != null) {
            measureLayout.addSystemStartBarline(startBarline);
        }
    }

    public void extractNotesToCacheMaps(MeasureLayout measureLayout, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
        List<SegmentLayout> segments = measureLayout.getSegments();
        int segmentCount = segments.size();
        for (int s = 0; s < segmentCount; s++) {
            SegmentLayout segment = segments.get(s);
            List<ElementLayout> elements = segment.getElements();
            int elementCount = elements.size();
            for (int e = 0; e < elementCount; e++) {
                ElementLayout element = elements.get(e);
                if (element instanceof NoteLayout noteLayout) {
                    slurBuilder.putNote(noteLayout.getNote(), noteLayout);
                    if (noteLayout.getNote().isTieStart()) {
                        tieBuilder.addNoteLayout(noteLayout);
                    }
                }
            }
        }
    }

    public void addEndBarline(Measure measure, MeasureLayout measureLayout) {
        Segment endBarlineSegment = new Segment(SegmentType.BARLINE, measure);
        SegmentLayout endBarlineSegLayout = new SegmentLayout(endBarlineSegment, measureLayout);
        List<StaffLayout> staffs = measureLayout.getStaffs();
        int staffCount = staffs.size();
        for (int i = 0; i < staffCount; i++) {
            StaffLayout staffLayout = staffs.get(i);
            endBarlineSegment.addElement(staffLayout.getStaffIndex(), measure.getRightBarline());
            endBarlineSegLayout.addByStaff(staffLayout, new BarlineLayout(measure.getRightBarline(), staffLayout, endBarlineSegLayout));
        }
        measureLayout.add(endBarlineSegLayout);
    }

    private void addPermanentAttributesIfNeeded(Measure measure, MeasureLayout measureLayout) {
        Measure prevMeasure = measure.getPrev();
        if (prevMeasure == null) return;

        if (measure.getKeySignature() != null && prevMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(prevMeasure.getKeySignature())) {
            SegmentLayout segment = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
            segment.addKeySignature(measure.getKeySignature());
            measureLayout.add(segment);
        }

        TimeSignature currentTS = measure.getTimeSignature();
        TimeSignature prevTS = prevMeasure.getTimeSignature();

        if (currentTS != null && currentTS.isVisible() && prevTS != null) {
            if (!currentTS.equals(prevTS)) {
                SegmentLayout segment = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
                segment.addTimeSignature(currentTS);
                measureLayout.add(segment);
            }
        }
    }

    private GroupBeamBuilder populateMeasureSegments(Measure measure, MeasureLayout measureLayout, LayoutContext layoutContext, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
        GroupBeamBuilder groupBeamBuilder = new GroupBeamBuilder();
        List<Segment> segments = measure.getSegments();
        List<StaffLayout> staffs = measureLayout.getStaffs();

        int segmentCount = segments.size();
        int staffCount = staffs.size();

        for (int s = 0; s < segmentCount; s++) {
            Segment segment = segments.get(s);
            SegmentLayout segmentLayout = new SegmentLayout(segment, measureLayout);

            for (int st = 0; st < staffCount; st++) {
                StaffLayout staff = staffs.get(st);
                List<Element> elements = segment.getElementsByStaff(staff.getStaffIndex());
                int elementCount = elements.size();

                for (int e = 0; e < elementCount; e++) {
                    Element element = elements.get(e);
                    if (element instanceof Barline barline) {
                        segmentLayout.addByStaff(staff, new BarlineLayout(barline, staff, segmentLayout));
                    } else if (element instanceof Note note) {
                        NoteLayout noteLayout = new NoteLayout(note, staff, segmentLayout);
                        List<LyricLayout> lyrics = createSingleLyricLine(noteLayout, layoutContext.activeVerse());
                        noteLayout.setLyrics(lyrics);
                        segmentLayout.addByStaff(staff, noteLayout);

                        slurBuilder.putNote(note, noteLayout);
                        if (note.isTieStart()) {
                            tieBuilder.addNoteLayout(noteLayout);
                        }

                        if (note.isBeamed()) groupBeamBuilder.add(noteLayout);
                    } else if (element instanceof Rest rest) {
                        RestLayout restLayout = new RestLayout(rest, staff, segmentLayout);
                        segmentLayout.addByStaff(staff, restLayout);
                    }
                }
            }
            measureLayout.add(segmentLayout);
        }
        return groupBeamBuilder;
    }

    private List<LyricLayout> createSingleLyricLine(NoteLayout noteLayout, int verseNumber) {
        if (noteLayout == null || noteLayout.getNote() == null) {
            return Collections.emptyList();
        }

        Note note = noteLayout.getNote();
        Lyric lyric = note.getLyric(verseNumber);

        if (lyric != null) {
            String text = lyric.getText();
            boolean hasText = text != null && !isTrimmedEmpty(text);
            if (hasText || lyric.isConnected()) {
                List<LyricLayout> result = new ArrayList<>(1);
                result.add(new LyricLayout(lyric, noteLayout));
                return result;
            }
        }

        return Collections.emptyList();
    }

    private static boolean isTrimmedEmpty(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}