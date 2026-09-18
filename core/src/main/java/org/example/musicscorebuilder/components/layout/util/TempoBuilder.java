package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.NoteRestLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.TempoLayout;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.Segment;
import org.example.musicscorebuilder.components.music.Tempo;

import java.util.List;
import java.util.Map;

public class TempoBuilder {
    private final Map<Measure, MeasureLayout> measureCache;

    public TempoBuilder(Map<Measure, MeasureLayout> measureCache) {
        this.measureCache = measureCache;
    }

    public void buildTempos(List<Tempo> tempos) {

        for (MeasureLayout measureLayout : measureCache.values()) {
            for (SegmentLayout segmentLayout : measureLayout.getSegments()) {
                segmentLayout.setTempoLayout(null);
            }
        }

        if (tempos == null || tempos.isEmpty()) return;

        for (Tempo tempo : tempos) {
            Measure measure = tempo.getMeasure();
            if (measure == null) continue;

            MeasureLayout measureLayout = measureCache.get(measure);
            if (measureLayout == null) continue;

            List<Segment> musicSegments = measure.getSegments();
            int segIdx = tempo.getSegmentIndex();

            if (segIdx >= 0 && segIdx < musicSegments.size()) {
                Segment targetMusicSegment = musicSegments.get(segIdx);

                SegmentLayout targetSegmentLayout = measureLayout.getSegments().stream()
                        .filter(sl -> sl.getSegment() == targetMusicSegment)
                        .findFirst()
                        .orElse(null);

                if (targetSegmentLayout != null) {
                    NoteRestLayout targetNoteRest = targetSegmentLayout.getFirstNoteRestLayout();
                    if (targetNoteRest != null) {
                        TempoLayout tempoLayout = new TempoLayout(tempo, targetNoteRest);
                        targetSegmentLayout.setTempoLayout(tempoLayout);
                    }
                }
            }
        }
    }
}