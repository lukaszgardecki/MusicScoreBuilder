package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

import java.util.List;

public class LayoutEngine {
    private final ScoreStyle style;
    private final Page page;

    public LayoutEngine(Page page, ScoreStyle style) {
        this.page = page;
        this.style = style;
    }

    public ScoreLayout compute(Mode mode) {
        ScoreLayout scoreLayout = new ScoreLayout(style);
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);

        SystemLayout newSystem = addNewSystemToPage(currentPage, mode);

        for (Measure measure : mode.getMeasures()) {
            MeasureLayout measureLayout = createMeasureLayout(measure, newSystem);

            boolean noSpaceForNextMeasure = currentPage.getEffectiveWidth() - newSystem.getWidth() < measureLayout.getWidth();
            boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < newSystem.getHeight();
            if (noSpaceForNextMeasure) {
                justify(newSystem);

                if (noSpaceForNextSystem) {
                    currentPage = createPageLayout(scoreLayout);
                    scoreLayout.addPageLayout(currentPage);
                }
                newSystem = addNewSystemToPage(currentPage, mode);
                measureLayout.setX(newSystem.getWidth());
            }

            if (newSystem.getMeasures().isEmpty()) {
                add1stMeasureAttributes(mode, measureLayout, scoreLayout);
            }

            newSystem.add(measureLayout);
        }

        justify(newSystem);
        return scoreLayout;
    }

    private void justify(SystemLayout system) {
        double targetWidth = system.getPageLayout().getEffectiveWidth();
        if (system.getMeasures().isEmpty() || system.getWidth() < targetWidth * style.getSystemMinFullnessRatio()) return;
        double extraSpace = targetWidth - system.getWidth();
        if (extraSpace <= 0) return;

        List<MeasureLayout> measures = system.getMeasures();

        double totalDynamicSegmentsWidthSum = 0.0;
        double[] measuresDynamicWidths = new double[measures.size()];

        for (int i = 0; i < measures.size(); i++) {
            MeasureLayout m = measures.get(i);
            double dynamicSegmentsTotalWidth = 0.0;

            for (SegmentLayout segment : m.getSegments()) {
                if (segment.hasDynamicWidth()) dynamicSegmentsTotalWidth += segment.getWidth();
            }
            measuresDynamicWidths[i] = dynamicSegmentsTotalWidth;
            totalDynamicSegmentsWidthSum += dynamicSegmentsTotalWidth;
        }

        if (totalDynamicSegmentsWidthSum <= 0) return;

        double currentX = system.getBraceWidth();

        for (int i = 0; i < measures.size(); i++) {
            MeasureLayout measure = measures.get(i);
            measure.setX(currentX);

            double thisMeasureDynamicWidth = measuresDynamicWidths[i];
            double widthRatio = thisMeasureDynamicWidth / totalDynamicSegmentsWidthSum;
            double extraSpaceForThisMeasure = extraSpace * widthRatio;

            long dynamicSegmentsCount = measure.getSegments().stream()
                    .filter(SegmentLayout::hasDynamicWidth)
                    .count();

            if (dynamicSegmentsCount > 0) {
                double extraSpacePerSegment = extraSpaceForThisMeasure / dynamicSegmentsCount;

                for (SegmentLayout segment : measure.getSegments()) {
                    if (segment.hasDynamicWidth()) {
                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof VoiceLayout voiceLayout && element.hasDynamicWidth()) {
                                List<ChordLayout> chords = voiceLayout.getChords();
                                if (chords.size() <= 1) continue;
                                double extraSpacePerGap = extraSpacePerSegment / (chords.size() - 1);

                                for (int c = 0; c < chords.size(); c++) {
                                    ChordLayout chord = chords.get(c);
                                    double newX = chord.getX() + (c * extraSpacePerGap);
                                    chord.setX(newX);
                                }
                            }
                        }
                    }
                }
            }
            currentX += measure.getWidth();
        }
    }

    private SystemLayout addNewSystemToPage(PageLayout pageLayout, Mode mode) {
        pageLayout.setLastSystemSpaceBelow(style.getSystemSpacing());
        var newSystem = new SystemLayout(pageLayout, mode.getBraceType());
        pageLayout.add(newSystem);
        return newSystem;
    }

    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
        int pageIndex = scoreLayout.getPages().size();
        return new PageLayout(page, style, pageIndex);
    }

    private MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout) {
        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout.getWidth(), style);

        for (Staff staff : measure.getStaves()) {
            measureLayout.add(new StaffLayout(staff, measureLayout, style));
        }

        for (Segment segment : measure.getSegments()) {
            SegmentLayout segmentLayout = new SegmentLayout(segment.getType(), measureLayout);

            for (Element element : segment.getElements()) {
                var el = switch(element) {
                    case Barline barline -> new BarlineLayout(barline, segmentLayout, style);
                    case Voice voice -> createVoiceLayout(voice);
                    default -> new EmptyElement(style);
                };
                segmentLayout.add(el);
            }
            measureLayout.add(segmentLayout);
        }
        return measureLayout;
    }

    private void add1stMeasureAttributes(Mode mode, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        var staves = measureLayout.getStaffs();
        var segments = measureLayout.getSegments();
        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;

        if (isFirstMeasure) {
            SegmentLayout seg1 = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
            staves.forEach(staff -> seg1.add(new TimeSigLayout(mode.getTimeSignature(), staff, style)));
            segments.addFirst(seg1);
        }

        SegmentLayout seg2 = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
        staves.forEach(staff -> seg2.add(new KeySigLayout(mode.getKeySignature(), staff, style)));
        segments.addFirst(seg2);

        SegmentLayout seg3 = new SegmentLayout(SegmentType.CLEF, measureLayout);
        staves.forEach(staff -> seg3.add(staff.getClefLayout()));
        segments.addFirst(seg3);

        if (mode.getStartBarline() == null) return;
        SegmentLayout seg4 = new SegmentLayout(SegmentType.START_BARLINE, measureLayout);
        seg4.add(new BarlineLayout(mode.getStartBarline(), seg4, style));
        segments.addFirst(seg4);
    }

    private VoiceLayout createVoiceLayout(Voice voice) {
        VoiceLayout voiceLayout = new VoiceLayout(voice, style);

        for(Chord chord : voice.getChords()) {
            ChordLayout chordLayout = new ChordLayout(chord, voiceLayout.getWidth(), style);
            chord.getNotes().forEach(note -> chordLayout.add(new NoteLayout(note, style)));
            voiceLayout.add(chordLayout);
        }
        return voiceLayout;
    }
}
