package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.util.BeamBuilder;
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

    public ScoreLayout compute(ScoreMode scoreMode) {
        ScoreLayout scoreLayout = new ScoreLayout(style);
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);

        SystemLayout newSystem = addNewSystemToPage(currentPage, scoreMode);

        for (Measure measure : scoreMode.getMeasures()) {
            MeasureLayout measureLayout = createMeasureLayout(measure, newSystem);
            boolean noSpaceForNextMeasure = currentPage.getEffectiveWidth() - newSystem.getWidth() < measureLayout.getWidth();
            boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < newSystem.getHeight() + style.getSystemSpacing();

            if (noSpaceForNextMeasure) {
                systemJustifier.justify(newSystem);

                if (noSpaceForNextSystem) {
                    currentPage = createPageLayout(scoreLayout);
                    scoreLayout.addPageLayout(currentPage);
                }
                newSystem = addNewSystemToPage(currentPage, scoreMode);
                measureLayout.setX(newSystem.getWidth());
            }

            if (newSystem.getMeasures().isEmpty()) {
                add1stMeasureAttributes(scoreMode, measureLayout, scoreLayout);
            }

            newSystem.add(measureLayout);
        }

        systemJustifier.justify(newSystem);
        return scoreLayout;
    }

    private SystemLayout addNewSystemToPage(PageLayout pageLayout, ScoreMode scoreMode) {
        pageLayout.setLastSystemSpaceBelow(style.getSystemSpacing());
        var newSystem = new SystemLayout(pageLayout, scoreMode.getBraceType());
        pageLayout.add(newSystem);
        return newSystem;
    }

    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
        int pageIndex = scoreLayout.getPages().size();
        return new PageLayout(page, style, pageIndex);
    }

    private MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout) {
        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout.getWidth(), style);
        BeamBuilder beamBuilder = new BeamBuilder();

        for (Staff staff : measure.getStaves()) {
            measureLayout.add(new StaffLayout(staff, measureLayout, style));
        }

        for (Segment segment : measure.getSegments()) {
            SegmentLayout segmentLayout = new SegmentLayout(segment.getType(), measureLayout);
            for (StaffLayout staff : measureLayout.getStaffs()) {
                for (Element element : segment.getElementsByStaff(staff.getStaff())) {
                    if (element instanceof Barline barline) {
                        segmentLayout.addByStaff(staff, new BarlineLayout(barline, staff, segmentLayout));
                    } else if (element instanceof Note note) {
                        NoteLayout noteLayout = new NoteLayout(note, staff, segmentLayout);
                        segmentLayout.addByStaff(staff, noteLayout);
                        if (note.isBeamed()) beamBuilder.add(noteLayout);
                    }
                }
            }
            measureLayout.add(segmentLayout);
        }
        measureLayout.setBeamGroups(beamBuilder.build());
        return measureLayout;
    }

    private void add1stMeasureAttributes(ScoreMode scoreMode, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;

        if (isFirstMeasure) {
            measureLayout.addTimeSignature(scoreMode.getTimeSignature());
        }
        measureLayout.addKeySignature(scoreMode.getKeySignature());
        measureLayout.addClef();
        if (scoreMode.getStartBarline() == null) return;
        measureLayout.addStartBarline(scoreMode.getStartBarline());
    }
}
