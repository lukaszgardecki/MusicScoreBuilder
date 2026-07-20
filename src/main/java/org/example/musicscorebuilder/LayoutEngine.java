package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

public class LayoutEngine {
    private final ScoreStyle style;
    private final Page page;
    private final SystemJustifier systemJustifier;

    public LayoutEngine(Page page, ScoreStyle style) {
        this.page = page;
        this.style = style;
        this.systemJustifier = new SystemJustifier(style);
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
                systemJustifier.justify(newSystem);

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

        systemJustifier.justify(newSystem);
        return scoreLayout;
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

            for (StaffLayout staff : measureLayout.getStaffs()) {
                for (Element element : segment.getElementsForStaff(staff.getStaff())) {

                    var el = switch(element) {
                        case Barline barline -> new BarlineLayout(barline, staff, segmentLayout);
                        case Voice voice -> createVoiceLayout(voice, staff, segmentLayout);
                        default -> new EmptyElement(segmentLayout);
                    };
                    segmentLayout.addByStaff(staff, el);
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

    private VoiceLayout createVoiceLayout(Voice voice, StaffLayout staff, SegmentLayout parent) {
        VoiceLayout voiceLayout = new VoiceLayout(voice, parent);

        for(Chord chord : voice.getChords()) {
            ChordLayout chordLayout = new ChordLayout(chord, voiceLayout.getWidth(), parent);
            chord.getNotes().forEach(note -> chordLayout.add(new NoteLayout(note, parent, staff)));
            voiceLayout.add(chordLayout);
        }
        return voiceLayout;
    }
}
