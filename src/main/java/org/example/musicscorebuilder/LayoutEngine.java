package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

import java.util.List;

public class LayoutEngine {
    private final double spatiumInMm = 1.75;
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout compute(Score score) {
        ScoreLayout scoreLayout = new ScoreLayout();
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);

        SystemLayout newSystem = addNewSystemToPage(currentPage);

        for (Measure measure : score.getMeasures()) {
            addMeasureToAllParts(measure, score.getParts(), newSystem);

            if (newSystem.getWidth() > currentPage.getEffectiveWidth()) {
                SystemLayout oldSystem = newSystem;

                removeLastMeasureFromSystem(oldSystem, score.getParts());

                justify(oldSystem);

                if (currentPage.getRemainingHeight() < oldSystem.getHeight()) {
                    currentPage = createPageLayout(scoreLayout);
                    scoreLayout.addPageLayout(currentPage);
                }

                newSystem = addNewSystemToPage(currentPage);
                addMeasureToAllParts(measure, score.getParts(), newSystem);
            }
        }

        justify(newSystem);
        return scoreLayout;
    }

    private void justify(SystemLayout system) {
        double targetWidth = page.getEffectiveWidth() / spatiumInMm;
        double MIN_FULLNESS_RATIO = 0.5;
        if (system.getParts().isEmpty() || system.getWidth() < targetWidth * MIN_FULLNESS_RATIO) return;
        double extraSpace = targetWidth - system.getWidth();
        if (extraSpace <= 0) return;

        for (PartLayout partLayout : system.getParts()) {
            double currentX = partLayout.getBraceLayout().getWidth();
            List<PMeasureLayout> partMeasures = partLayout.getPartMeasures();

            double totalNotesMinWidthSum = 0.0;
            double[] measuresMinWidths = new double[partMeasures.size()];

            for (int i = 0; i < partMeasures.size(); i++) {
                PMeasureLayout pm = partMeasures.get(i);
                List<SMeasureLayout> sMeasures = pm.getSMeasures();

                if (!sMeasures.isEmpty()) {
                    double measureMinWidthSum = 0.0;
                    for (SegmentLayout segment : sMeasures.getFirst().getActiveSegments()) {
                        if (segment.hasDynamicWidth()) {
                            measureMinWidthSum += segment.getWidth();
                        }
                    }
                    measuresMinWidths[i] = measureMinWidthSum;
                    totalNotesMinWidthSum += measureMinWidthSum;
                }
            }

            if (totalNotesMinWidthSum <= 0) continue;

            for (int i = 0; i < partMeasures.size(); i++) {
                PMeasureLayout pMeasure = partMeasures.get(i);
                pMeasure.setX(currentX);

                double thisMeasureNotesMinWidth = measuresMinWidths[i];
                double share = thisMeasureNotesMinWidth / totalNotesMinWidthSum;
                double extraSpaceForThisMeasure = extraSpace * share;

                for (SMeasureLayout sMeasure : pMeasure.getSMeasures()) {
                    List<SegmentLayout> activeSegments = sMeasure.getActiveSegments();

                    int dynamicSegmentsCount = 0;
                    for (SegmentLayout segment : activeSegments) {
                        if (segment.hasDynamicWidth()) {
                            dynamicSegmentsCount++;
                        }
                    }

                    double extraSpacePerSegment = dynamicSegmentsCount > 0 ? (extraSpaceForThisMeasure / dynamicSegmentsCount) : 0;

                    double segmentX = 0.0;
                    for (SegmentLayout segment : activeSegments) {
                        if (segment.hasDynamicWidth()) {
                            segment.setWidth(segment.getWidth() + extraSpacePerSegment);
                        }
                        segment.setX(segmentX);
                        segmentX += segment.getWidth();
                    }
                }
                currentX += pMeasure.getWidth();
            }
        }
    }

    private SystemLayout addNewSystemToPage(PageLayout pageLayout) {
        if (!pageLayout.getSystems().isEmpty()) {
            pageLayout.getSystems().getLast().setDefaultSpaceBelow();
        }
        var newSystem = createSystemLayout(pageLayout);
        newSystem.setSpaceBelow(0);
        pageLayout.add(newSystem);
        return newSystem;
    }

    private void addMeasureToAllParts(Measure measure, List<Part> parts, SystemLayout system) {
        parts.forEach(part -> addMeasureToPartInSystem(measure, part, system));
        setFirstMeasuresInSystem(system);
    }

    private void setFirstMeasuresInSystem(SystemLayout system) {
        system.getParts().forEach(part -> {
            if (part.getPartMeasures().size() == 1) {
                part.getPartMeasures().getFirst().setFirstInSystem();
            }
        });
    }

    private void removeLastMeasureFromSystem(SystemLayout system, List<Part> parts) {
        for (Part part : parts) {
            system.findPartLayout(part).ifPresent(PartLayout::removeLastPMeasureLayout);
        }
    }

    private void addMeasureToPartInSystem(Measure measure, Part part, SystemLayout system) {
        PartLayout partLayout = system.findPartLayout(part)
                .orElseGet(() -> addNewPartToSystem(part, system));
        PMeasureLayout pMeasureLayout = createPMeasureLayout(measure, partLayout);
        partLayout.add(pMeasureLayout);
    }

    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
        int size = scoreLayout.getPages().size();
        double widthSp = page.getWidthMm() / spatiumInMm;
        double heightSp = page.getHeightMm() / spatiumInMm;
        double xSp = size * (page.getWidthMm() / spatiumInMm + scoreLayout.getSpacing());
        double effectiveWidthSp = page.getEffectiveWidth() / spatiumInMm;
        double effectiveHeightSp = page.getEffectiveHeight() / spatiumInMm;
        return new PageLayout(widthSp, heightSp, xSp,  effectiveWidthSp, effectiveHeightSp);
    }

    private SystemLayout createSystemLayout(PageLayout pageLayout) {
        double xSp = page.getMarginLeftMm() / spatiumInMm;
        double ySp = page.getMarginTopMm() / spatiumInMm + pageLayout.getOccupiedHeight();
        return new SystemLayout(xSp, ySp);
    }

    private PartLayout addNewPartToSystem(Part part, SystemLayout system) {
        if (!system.getParts().isEmpty()) {
            system.getParts().getLast().setDefaultSpaceBelow();
        }
        PartLayout newPartLayout = new PartLayout(part, 0, system.getHeight());
        newPartLayout.setSpaceBelow(0);
        system.add(newPartLayout);
        return newPartLayout;
    }

    private PMeasureLayout createPMeasureLayout(Measure measure, PartLayout partLayout) {
        PMeasureLayout pMeasureLayout = new PMeasureLayout(partLayout, measure, partLayout.getWidth(), 0);
        var staves = partLayout.getPart().getStaves();
        for (Staff staff : staves) {
            SMeasureLayout sMeasureLayout = createSMeasureLayout(pMeasureLayout, staff);
            if (staves.getLast() == staff) sMeasureLayout.setSpaceBelow(0);
            pMeasureLayout.add(sMeasureLayout);
        }
        return pMeasureLayout;
    }

    private SMeasureLayout createSMeasureLayout(PMeasureLayout pMeasure, Staff staff) {
        var measureTime = pMeasure.getMeasure().getTimeSignature().getValue();
        SMeasureLayout sMeasure = new SMeasureLayout(pMeasure, staff, 0, pMeasure.getHeight());
        sMeasure.add(SegmentType.CLEF);
        sMeasure.add(SegmentType.KEY_SIG);
        sMeasure.add(SegmentType.TIME_SIG);

        for (int i = 0; i < measureTime; i++) {
            sMeasure.add(SegmentType.CHORD_REST);
        }
        return sMeasure;
    }
}
