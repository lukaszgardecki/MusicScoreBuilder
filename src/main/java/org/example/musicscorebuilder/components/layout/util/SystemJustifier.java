package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.Arrays;
import java.util.List;

public class SystemJustifier {
    private final ScoreStyle style;

    public SystemJustifier(ScoreStyle style) {
        this.style = style;
    }

    public void justify(SystemLayout system) {
        List<MeasureLayout> measures = system.getMeasures();
        if (measures.isEmpty()) return;

        prepareEndBarline(system);

        double targetWidth = system.getPageLayout().getEffectiveWidth();

        resetExtraWidths(measures);

        double currentSystemWidth = system.getWidth();
        if (currentSystemWidth < targetWidth * style.getSystemMinFullnessRatio()) { return; }

        double extraSpace = targetWidth - currentSystemWidth;

        if (extraSpace <= 0) return;

        double[] measuresDynamicWidths = calculateDynamicWidths(measures);
        double totalDynamicWidthSum = Arrays.stream(measuresDynamicWidths).sum();

        if (totalDynamicWidthSum <= 0) return;

        distributeSpaceAndPositionMeasures(system, measures, measuresDynamicWidths, totalDynamicWidthSum, extraSpace);
    }

    private void resetExtraWidths(List<MeasureLayout> measures) {
        for (MeasureLayout measure : measures) {
            for (SegmentLayout segment : measure.getSegments()) {
                segment.setExtraWidth(0.0);
            }
        }
    }

    private void prepareEndBarline(SystemLayout system) {
        MeasureLayout lastMeasure = system.getMeasures().getLast();
        if (lastMeasure.getSegments().isEmpty()) return;

        SegmentLayout lastSeg = lastMeasure.getSegments().getLast();
        if (lastSeg.getType() == SegmentType.BARLINE) {
            lastSeg.setType(SegmentType.END_BARLINE);
        }
    }

    private double[] calculateDynamicWidths(List<MeasureLayout> measures) {
        double[] widths = new double[measures.size()];
        for (int i = 0; i < measures.size(); i++) {
            widths[i] = calculateMeasureDynamicWidth(measures.get(i));
        }
        return widths;
    }

    private double calculateMeasureDynamicWidth(MeasureLayout measure) {
        return measure.getSegments().stream()
                .filter(SegmentLayout::hasDynamicWidth)
                .mapToDouble(SegmentLayout::getWidth)
                .sum();
    }

    private void distributeSpaceAndPositionMeasures(
            SystemLayout system,
            List<MeasureLayout> measures,
            double[] measuresDynamicWidths,
            double totalDynamicWidth,
            double extraSpace
    ) {
        double currentX = system.getBraceWidth();

        for (int i = 0; i < measures.size(); i++) {
            MeasureLayout measure = measures.get(i);
            measure.setX(currentX);

            double measureDynamicWidth = measuresDynamicWidths[i];

            if (measureDynamicWidth > 0 && totalDynamicWidth > 0) {
                double widthRatio = measureDynamicWidth / totalDynamicWidth;
                double extraSpaceForMeasure = extraSpace * widthRatio;

                List<SegmentLayout> dynamicSegments = measure.getSegments().stream()
                        .filter(SegmentLayout::hasDynamicWidth)
                        .toList();

                if (!dynamicSegments.isEmpty()) {
                    double extraPerSegment = extraSpaceForMeasure / dynamicSegments.size();
                    for (SegmentLayout segment : dynamicSegments) {
                        segment.setExtraWidth(extraPerSegment);
                    }
                }
            }

            currentX += measure.getWidth();
        }
    }
}