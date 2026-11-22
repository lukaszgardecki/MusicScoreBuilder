//package org.example.musicscorebuilder.components.layout.util;
//
//import org.example.musicscorebuilder.components.layout.MeasureLayout;
//import org.example.musicscorebuilder.components.layout.SegmentLayout;
//import org.example.musicscorebuilder.components.layout.SystemLayout;
//import org.example.musicscorebuilder.components.layout.engine.LayoutContext;
//import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
//import org.example.musicscorebuilder.components.music.Measure;
//import org.example.musicscorebuilder.components.music.ScoreMode;
//import org.example.musicscorebuilder.components.music.SegmentType;
//
//import java.util.Collections;
//import java.util.IdentityHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//public class MeasureCacheManager {
//    private final Map<Measure, MeasureLayout> measureCache;
//    private final MeasureLayoutBuilder measureLayoutBuilder;
//
//    public MeasureCacheManager(Map<Measure, MeasureLayout> measureCache, MeasureLayoutBuilder measureLayoutBuilder) {
//        this.measureCache = measureCache;
//        this.measureLayoutBuilder = measureLayoutBuilder;
//    }
//
//    public void invalidateCacheIfNeeded(ScoreMode scoreMode) {
//        measureCache.keySet().removeIf(Measure::isDirty);
//        Set<Measure> activeMeasures = Collections.newSetFromMap(new IdentityHashMap<>());
//        activeMeasures.addAll(scoreMode.getMeasures());
//        measureCache.keySet().retainAll(activeMeasures);
//    }
//
//    public MeasureLayout getOrCreateMeasureLayout(Measure measure, SystemLayout currentSystem, ScoreStyle style, LayoutContext layoutContext, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
//        if (measureCache.containsKey(measure) && !measure.isDirty()) {
//            MeasureLayout measureLayout = measureCache.get(measure);
//            measureLayout.remove1stMeasureAttributes();
//
//            List<SegmentLayout> segments = measureLayout.getSegments();
//            if (!segments.isEmpty() && segments.get(segments.size() - 1).getSegment().getType() != SegmentType.BARLINE) {
//                measureLayoutBuilder.addEndBarline(measure, measureLayout);
//            }
//
//            measureLayout.resetLayoutState();
//            measureLayout.setParent(currentSystem);
//            measureLayoutBuilder.extractNotesToCacheMaps(measureLayout, tieBuilder, slurBuilder);
//            return measureLayout;
//        }
//
//        MeasureLayout measureLayout = measureLayoutBuilder.createMeasureLayout(measure, currentSystem, style, layoutContext, tieBuilder, slurBuilder);
//        measureCache.put(measure, measureLayout);
//        measure.setDirty(false);
//        return measureLayout;
//    }
//}

package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.components.layout.engine.LayoutContext;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MeasureCacheManager {
    private final Map<Measure, MeasureLayout> measureCache;
    private final MeasureLayoutBuilder measureLayoutBuilder;

    public MeasureCacheManager(Map<Measure, MeasureLayout> measureCache, MeasureLayoutBuilder measureLayoutBuilder) {
        this.measureCache = measureCache;
        this.measureLayoutBuilder = measureLayoutBuilder;
    }

    public void invalidateCacheIfNeeded(ScoreMode scoreMode) {
        if (measureCache.isEmpty()) return;

        List<Measure> activeMeasures = scoreMode.getMeasures();
        Set<Measure> activeSet = Collections.newSetFromMap(new IdentityHashMap<>(activeMeasures.size()));
        activeSet.addAll(activeMeasures);

        Iterator<Map.Entry<Measure, MeasureLayout>> iterator = measureCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Measure, MeasureLayout> entry = iterator.next();
            Measure measure = entry.getKey();
            if (measure.isDirty() || !activeSet.contains(measure)) {
                iterator.remove();
            }
        }
    }

    public MeasureLayout getOrCreateMeasureLayout(Measure measure, SystemLayout currentSystem, ScoreStyle style, LayoutContext layoutContext, TieBuilder tieBuilder, SlurBuilder slurBuilder) {
        MeasureLayout measureLayout = measureCache.get(measure);

        if (measureLayout != null && !measure.isDirty()) {
            measureLayout.remove1stMeasureAttributes();

            List<SegmentLayout> segments = measureLayout.getSegments();
            if (!segments.isEmpty() && segments.get(segments.size() - 1).getSegment().getType() != SegmentType.BARLINE) {
                measureLayoutBuilder.addEndBarline(measure, measureLayout);
            }

            measureLayout.resetLayoutState();
            measureLayout.setParent(currentSystem);
            measureLayoutBuilder.extractNotesToCacheMaps(measureLayout, tieBuilder, slurBuilder);
            return measureLayout;
        }

        measureLayout = measureLayoutBuilder.createMeasureLayout(measure, currentSystem, style, layoutContext, tieBuilder, slurBuilder);
        measureCache.put(measure, measureLayout);
        measure.setDirty(false);
        return measureLayout;
    }
}