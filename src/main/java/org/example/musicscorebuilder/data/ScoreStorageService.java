package org.example.musicscorebuilder.data;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.musicscorebuilder.components.music.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ScoreStorageService {

    private final ObjectMapper mapper;

    public ScoreStorageService() {
        this.mapper = new ObjectMapper();
        // Ładne formatowanie JSON w trybie tekstowym
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // Odporność na ewentualne nowe pola w przyszłości
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

            for (Measure measure : mode.getMeasures()) {

                if (measure.getTimeSignature() != null) {
                    measure.getTimeSignature().setParent(measure);
                }
                if (measure.getKeySignature() != null) {
                    measure.getKeySignature().setParent(measure);
                }
                if (measure.getRightBarline() != null) {
                    measure.getRightBarline().setParent(measure);
                }


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