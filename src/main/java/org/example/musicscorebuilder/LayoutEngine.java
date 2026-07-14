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
        ScoreLayout scoreLayout = new ScoreLayout();
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);

        SystemLayout newSystem = addNewSystemToPage(currentPage, mode);

        for (Measure measure : mode.getMeasures()) {
            MeasureLayout measureLayout = createMeasureLayout(measure,  newSystem);

            boolean noSpaceForNextMeasure = currentPage.getEffectiveWidth() - newSystem.getWidth() < measureLayout.getWidth();
            boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < newSystem.getHeight();
            if (noSpaceForNextMeasure) {
                if (noSpaceForNextSystem) {
                    currentPage = createPageLayout(scoreLayout);
                    scoreLayout.addPageLayout(currentPage);
                }
                newSystem = addNewSystemToPage(currentPage, mode);
                measureLayout.setX(newSystem.getWidth());
            }

            if (newSystem.getMeasures().isEmpty()) {
                measureLayout.addAtBegin(mode.getStartBarline());
            }
            newSystem.add(measureLayout);
            justify(newSystem);
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
                        segment.setWidth(segment.getWidth() + extraSpacePerSegment);
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
        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout.getWidth());
        measureLayout.setStaffSpacing(style.getStaffSpacing());

        for (Staff staff : measure.getStaves()) {
            measureLayout.add(new StaffLayout(staff, measureLayout));
        }

        for (Segment segment : measure.getSegments()) {
            SegmentLayout segmentLayout = new SegmentLayout(measureLayout);

            for (Element element : segment.getElements()) {
                var el = switch(element) {
                    case Barline barline -> new BarlineLayout(barline, segmentLayout);
                    default -> new ElementLayout();
                };
                segmentLayout.add(el);
            }
            measureLayout.add(segmentLayout);
        }
        return measureLayout;
    }
}
