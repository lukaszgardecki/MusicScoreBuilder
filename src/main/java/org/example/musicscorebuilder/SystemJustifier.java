package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.Arrays;
import java.util.List;

public class SystemJustifier {
    private ScoreStyle style;

    public SystemJustifier(ScoreStyle style) {
        this.style = style;
    }

    public void justify(SystemLayout system) {
        if (system.getMeasures().isEmpty()) return;

        prepareEndBarline(system);

        double targetWidth = system.getPageLayout().getEffectiveWidth();
        if (system.getWidth() < targetWidth * style.getSystemMinFullnessRatio()) return;

        double extraSpace = targetWidth - system.getWidth();
        if (extraSpace <= 0) return;

        List<MeasureLayout> measures = system.getMeasures();
        double[] measuresDynamicWidths = calculateDynamicWidths(measures);
        double totalDynamicWidthSum = Arrays.stream(measuresDynamicWidths).sum();

        if (totalDynamicWidthSum <= 0) return;

        distributeSpaceAndPositionMeasures(system, measures, measuresDynamicWidths, totalDynamicWidthSum, extraSpace);
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
            double widthRatio = measureDynamicWidth / totalDynamicWidth;
            double extraSpaceForMeasure = extraSpace * widthRatio;

            List<SegmentLayout> dynamicSegments = measure.getSegments().stream().filter(SegmentLayout::hasDynamicWidth).toList();
            distributeExtraSpaceInSegments(dynamicSegments, extraSpaceForMeasure);

            currentX += measure.getWidth();
        }
    }

    private void distributeExtraSpaceInSegments(List<SegmentLayout> dynamicSegments, double extraSpaceForMeasure) {
        if (dynamicSegments.isEmpty()) return;
        double extraSpacePerSegment = extraSpaceForMeasure / dynamicSegments.size();
        dynamicSegments.forEach(segment -> distributeExtraSpaceInSegment(segment, extraSpacePerSegment));
    }

    private void distributeExtraSpaceInSegment(SegmentLayout segment, double extraSpacePerSegment) {
        segment.getElements().stream()
                .filter(VoiceLayout.class::isInstance)
                .map(VoiceLayout.class::cast)
                .forEach(voice -> distributeExtraSpaceInVoice(voice, extraSpacePerSegment));
    }

    private void distributeExtraSpaceInVoice(VoiceLayout voice, double extraSpacePerSegment) {
        List<ChordLayout> chords = voice.getChords();
        if (chords.size() <= 1) return;

        double extraSpacePerGap = extraSpacePerSegment / (chords.size() - 1);
        for (int c = 0; c < chords.size(); c++) {
            ChordLayout chord = chords.get(c);
            chord.setX(chord.getX() + (c * extraSpacePerGap));
        }
    }
}