package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.Volta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VoltaBuilder {
    private final Map<Measure, MeasureLayout> measureCache;

    public VoltaBuilder(Map<Measure, MeasureLayout> measureCache) {
        this.measureCache = measureCache;
    }

    public void buildVoltas(List<Volta> voltas, ScoreLayout scoreLayout) {
        if (voltas == null) return;

        clearExistingVoltaSlices(scoreLayout);

        for (Volta volta : voltas) {
            buildSingleVolta(volta);
        }
    }

    private void clearExistingVoltaSlices(ScoreLayout scoreLayout) {
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measureLayout : system.getMeasures()) {
                    measureLayout.setVoltaSlice(null);
                }
            }
        }
    }

    private void buildSingleVolta(Volta volta) {
        Measure startMeasure = volta.getStartMeasure();
        Measure endMeasure = volta.getEndMeasure();

        if (startMeasure == null || endMeasure == null) return;

        List<MeasureLayout> voltaMeasureLayouts = collectMeasureLayouts(startMeasure, endMeasure);
        if (voltaMeasureLayouts.isEmpty()) return;

        for (MeasureLayout ml : voltaMeasureLayouts) {
            VoltaSliceLayout slice = createVoltaSlice(volta, ml, startMeasure, endMeasure);
            ml.setVoltaSlice(slice);
        }
    }

    private List<MeasureLayout> collectMeasureLayouts(Measure startMeasure, Measure endMeasure) {
        List<MeasureLayout> layouts = new ArrayList<>();
        Measure current = startMeasure;

        while (current != null) {
            MeasureLayout ml = measureCache.get(current);
            if (ml != null) {
                layouts.add(ml);
            }
            if (current == endMeasure) break;
            current = current.getNext();
        }

        return layouts;
    }

    private VoltaSliceLayout createVoltaSlice(Volta volta, MeasureLayout ml, Measure startMeasure, Measure endMeasure) {
        boolean isVoltaStart = (ml.getMeasure() == startMeasure);
        boolean isVoltaEnd = (ml.getMeasure() == endMeasure);
        double startXOffset = MeasureOffsetCalculator.calculateStartXOffset(ml);
        double endXOffset = MeasureOffsetCalculator.calculateEndXOffset(ml);

        boolean drawLeftHook = isVoltaStart;
        boolean drawRightHook = isVoltaEnd && volta.isClosedEnd();
        String text = isVoltaStart ? volta.getText() : null;

        return new VoltaSliceLayout(volta, ml, text, drawLeftHook, drawRightHook, startXOffset, endXOffset);
    }
}