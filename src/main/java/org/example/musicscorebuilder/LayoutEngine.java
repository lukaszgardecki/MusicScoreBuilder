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
            boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < newSystem.getHeight() + style.getSystemSpacing();

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

            for (StaffLayout staffLayout : measureLayout.getStaffs()) {
                for (Element element : segment.getElementsForStaff(staffLayout.getStaff())) {

                    var el = switch(element) {
                        case Barline barline -> new BarlineLayout(barline, segmentLayout, staffLayout);
                        case Voice voice -> createVoiceLayout(voice, segmentLayout, staffLayout);
                        default -> new EmptyElement(segmentLayout);
                    };
                    segmentLayout.addElement(staffLayout, el);
                }
            }
            measureLayout.add(segmentLayout);
        }
        return measureLayout;
    }

    private void add1stMeasureAttributes(Mode mode, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;

        if (isFirstMeasure) {
            measureLayout.addTimeSignature(mode.getTimeSignature());
        }
        measureLayout.addKeySignature(mode.getKeySignature());
        measureLayout.addClef();
        if (mode.getStartBarline() == null) return;
        measureLayout.addStartBarline(mode.getStartBarline());
    }

    private VoiceLayout createVoiceLayout(Voice voice, SegmentLayout parent, StaffLayout staff) {
        VoiceLayout voiceLayout = new VoiceLayout(voice, parent);

        for(Chord chord : voice.getChords()) {
            ChordLayout chordLayout = new ChordLayout(chord, voiceLayout.getWidth(), parent);
            chord.getNotes().forEach(note -> chordLayout.add(new NoteLayout(note, parent, staff)));
            voiceLayout.add(chordLayout);
        }
        return voiceLayout;
    }
}
