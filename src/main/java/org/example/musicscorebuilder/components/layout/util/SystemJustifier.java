package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.SegmentType;

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

        int measureCount = measures.size();
        double[] measuresDynamicWidths = new double[measureCount];
        double totalDynamicWidthSum = 0.0;

        for (int i = 0; i < measureCount; i++) {
            double mWidth = calculateMeasureDynamicWidth(measures.get(i));
            measuresDynamicWidths[i] = mWidth;
            totalDynamicWidthSum += mWidth;
        }

        if (totalDynamicWidthSum <= 0) return;

        distributeSpaceAndPositionMeasures(system, measures, measuresDynamicWidths, totalDynamicWidthSum, extraSpace);
    }

    private void resetExtraWidths(List<MeasureLayout> measures) {
        int measureCount = measures.size();
        for (int i = 0; i < measureCount; i++) {
            List<SegmentLayout> segments = measures.get(i).getSegments();
            int segSize = segments.size();
            for (int s = 0; s < segSize; s++) {
                segments.get(s).setExtraWidth(0.0);
            }
        }
    }

    private void prepareEndBarline(SystemLayout system) {
        List<MeasureLayout> measures = system.getMeasures();
        if (measures.isEmpty()) return;

        MeasureLayout lastMeasure = measures.getLast();
        List<SegmentLayout> segments = lastMeasure.getSegments();
        if (segments.isEmpty()) return;

        SegmentLayout lastSeg = segments.get(segments.size() - 1);
        if (lastSeg.getType() == SegmentType.BARLINE) {
            lastSeg.setType(SegmentType.END_BARLINE);
        }
    }

    private double calculateMeasureDynamicWidth(MeasureLayout measure) {
        List<SegmentLayout> segments = measure.getSegments();
        double sum = 0.0;
        int size = segments.size();
        for (int i = 0; i < size; i++) {
            SegmentLayout seg = segments.get(i);
            if (seg.hasDynamicWidth()) {
                sum += seg.getWidth();
            }
        }
        return sum;
    }

    private void distributeSpaceAndPositionMeasures(
            SystemLayout system,
            List<MeasureLayout> measures,
            double[] measuresDynamicWidths,
            double totalDynamicWidth,
            double extraSpace
    ) {
        double currentX = system.getBraceWidth();
        int measureCount = measures.size();

        for (int i = 0; i < measureCount; i++) {
            MeasureLayout measure = measures.get(i);
            measure.setX(currentX);

            double measureDynamicWidth = measuresDynamicWidths[i];

            if (measureDynamicWidth > 0 && totalDynamicWidth > 0) {
                double widthRatio = measureDynamicWidth / totalDynamicWidth;
                double extraSpaceForMeasure = extraSpace * widthRatio;

                List<SegmentLayout> segments = measure.getSegments();
                int segSize = segments.size();

                int dynamicCount = 0;
                for (int s = 0; s < segSize; s++) {
                    if (segments.get(s).hasDynamicWidth()) {
                        dynamicCount++;
                    }
                }

                if (dynamicCount > 0) {
                    double extraPerSegment = extraSpaceForMeasure / dynamicCount;
                    for (int s = 0; s < segSize; s++) {
                        SegmentLayout segment = segments.get(s);
                        if (segment.hasDynamicWidth()) {
                            segment.setExtraWidth(extraPerSegment);
                        }
                    }
                }
            }

            currentX += measure.getWidth();
        }
    }
}