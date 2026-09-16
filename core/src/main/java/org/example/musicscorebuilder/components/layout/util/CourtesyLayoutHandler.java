package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

public class CourtesyLayoutHandler {

    public double calculateCourtesyPadding(Measure measure, MeasureLayout measureLayout) {
        Measure nextMeasure = measure.getNext();
        if (nextMeasure == null) return 0.0;

        double padding = 0.0;

        boolean keyChange = measure.getKeySignature() != null && nextMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(nextMeasure.getKeySignature());

        TimeSignature currTS = measure.getTimeSignature();
        TimeSignature nextTS = nextMeasure.getTimeSignature();
        boolean timeChange = currTS != null && nextTS != null
                && nextTS.isVisible()
                && !nextTS.equals(currTS);

        if (keyChange || timeChange) {
            Barline rightBarline = measure.getRightBarline();
            if (rightBarline != null && rightBarline.getStyle() == BarlineStyle.SINGLE) {
                SegmentLayout currentBarlineSeg = measureLayout.getSegments().get(measureLayout.getSegments().size() - 1);

                Barline doubleBarline = new Barline(BarlineStyle.DOUBLE_LIGHT, measure);
                SegmentLayout tempDoubleBarlineSeg = new SegmentLayout(new Segment(SegmentType.BARLINE, measure), measureLayout);
                for (StaffLayout staff : measureLayout.getStaffs()) {
                    tempDoubleBarlineSeg.addByStaff(staff, new BarlineLayout(doubleBarline, staff, tempDoubleBarlineSeg));
                }

                padding += (tempDoubleBarlineSeg.getWidth() - currentBarlineSeg.getWidth());
            }
        }

        if (keyChange) {
            SegmentLayout tempCourtesy = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
            tempCourtesy.addKeySignature(nextMeasure.getKeySignature());
            padding += tempCourtesy.getWidth();
        }

        if (timeChange) {
            SegmentLayout tempCourtesy = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
            tempCourtesy.addTimeSignature(nextTS);
            padding += tempCourtesy.getWidth();
        }

        return padding;
    }

    public void addCourtesyAttributesToLastMeasure(SystemLayout system, Measure nextMeasure) {
        if (system.getMeasures().isEmpty()) return;

        MeasureLayout lastMeasureLayout = system.getMeasures().get(system.getMeasures().size() - 1);
        Measure prevMeasure = lastMeasureLayout.getMeasure();

        boolean keyChange = nextMeasure.getKeySignature() != null && prevMeasure.getKeySignature() != null
                && !nextMeasure.getKeySignature().equals(prevMeasure.getKeySignature());

        TimeSignature prevTS = prevMeasure.getTimeSignature();
        TimeSignature nextTS = nextMeasure.getTimeSignature();
        boolean timeChange = prevTS != null && nextTS != null
                && nextTS.isVisible()
                && !nextTS.equals(prevTS);

        if (keyChange || timeChange) {
            Barline rightBarline = prevMeasure.getRightBarline();
            if (rightBarline != null && rightBarline.getStyle() == BarlineStyle.SINGLE) {
                lastMeasureLayout.getSegments().remove(lastMeasureLayout.getSegments().size() - 1);

                Segment doubleBarlineSegment = new Segment(SegmentType.BARLINE, prevMeasure);
                SegmentLayout doubleBarlineSegLayout = new SegmentLayout(doubleBarlineSegment, lastMeasureLayout);
                doubleBarlineSegLayout.setSystemGenerated(true);

                Barline doubleBarline = new Barline(BarlineStyle.DOUBLE_LIGHT, prevMeasure);
                for (StaffLayout staff : lastMeasureLayout.getStaffs()) {
                    doubleBarlineSegLayout.addByStaff(staff, new BarlineLayout(doubleBarline, staff, doubleBarlineSegLayout));
                }

                lastMeasureLayout.add(doubleBarlineSegLayout);
            }
        }

        if (keyChange) {
            SegmentLayout courtesyKeySig = new SegmentLayout(SegmentType.KEY_SIG, lastMeasureLayout);
            courtesyKeySig.addKeySignature(nextMeasure.getKeySignature());
            courtesyKeySig.setSystemGenerated(true);
            lastMeasureLayout.add(courtesyKeySig);
        }

        if (timeChange) {
            SegmentLayout courtesyTimeSig = new SegmentLayout(SegmentType.TIME_SIG, lastMeasureLayout);
            courtesyTimeSig.addTimeSignature(nextMeasure.getTimeSignature());
            courtesyTimeSig.setSystemGenerated(true);
            lastMeasureLayout.add(courtesyTimeSig);
        }
    }
}
