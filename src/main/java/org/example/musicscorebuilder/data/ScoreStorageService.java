package org.example.musicscorebuilder.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.musicscorebuilder.components.music.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ScoreStorageService {

    private final ObjectMapper mapper;

    public ScoreStorageService() {
        this.mapper = new ObjectMapper();
//        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void saveToJson(Score score, File file) throws IOException {
        mapper.writeValue(file, score);
    }

    public void saveToCompressedJson(Score score, File file) throws IOException {
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(new FileOutputStream(file))) {
            mapper.writeValue(gzipOut, score);
        }
    }

    public Score loadFromJson(File file) throws IOException {
        Score score = mapper.readValue(file, Score.class);
        hydrate(score);
        return score;
    }

    public Score loadFromCompressedJson(File file) throws IOException {
        Score score;
        try (GZIPInputStream gzipIn = new GZIPInputStream(new FileInputStream(file))) {
            score = mapper.readValue(gzipIn, Score.class);
        }
        hydrate(score);
        return score;
    }

    private void hydrate(Score score) {
        if (score == null || score.getModes() == null) return;

        for (ScoreMode mode : score.getModes()) {
            mode.setScore(score);

            if (mode.getMeasures() == null) continue;

            TimeSignature activeTimeSignature = null;
            KeySignature activeKeySignature = null;

            for (Measure measure : mode.getMeasures()) {

                // 1. Odtwarzanie TimeSignature (dziedziczenie z poprzedniego taktu, jeśli brak w JSON)
                if (measure.getTimeSignature() != null) {
                    activeTimeSignature = measure.getTimeSignature();
                    activeTimeSignature.setParent(measure);
                } else if (activeTimeSignature != null) {
                    TimeSignature inheritedTimeSig = new TimeSignature(
                            activeTimeSignature.getBeat(),
                            activeTimeSignature.getBeatType(),
                            activeTimeSignature.getType(),
                            measure
                    );
                    measure.setTimeSignature(inheritedTimeSig);
                } else {
                    TimeSignature defaultTimeSig = new TimeSignature(4, 4, TimeSignature.Type.FRACTIONAL, measure);
                    measure.setTimeSignature(defaultTimeSig);
                    activeTimeSignature = defaultTimeSig;
                }

                // 2. Odtwarzanie KeySignature (dziedziczenie z poprzedniego taktu, jeśli brak w JSON)
                if (measure.getKeySignature() != null) {
                    activeKeySignature = measure.getKeySignature();
                    activeKeySignature.setParent(measure);
                } else if (activeKeySignature != null) {
                    KeySignature inheritedKeySig = new KeySignature(
                            activeKeySignature.getFifths(),
                            measure
                    );
                    measure.setKeySignature(inheritedKeySig);
                } else {
                    KeySignature defaultKeySig = new KeySignature(0, measure);
                    measure.setKeySignature(defaultKeySig);
                    activeKeySignature = defaultKeySig;
                }

                // 3. Upewnienie się, że kreska taktowa ma ustawionego rodzica
                if (measure.getRightBarline() != null) {
                    measure.getRightBarline().setParent(measure);
                }

                // 4. Przeliczenie segmentów w takcie po odtworzeniu metrum
                measure.recalculateSegmentDurations();

                // 5. Powiązanie rodziców (parent) dla segmentów i elementów
                if (measure.getSegments() != null) {
                    for (Segment segment : measure.getSegments()) {
                        segment.setParent(measure);

                        if (segment.getStaffElements() != null) {
                            segment.getStaffElements().values().forEach(elementList -> {
                                if (elementList != null) {
                                    for (Element element : elementList) {
                                        element.setParent(measure);
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
    }
}