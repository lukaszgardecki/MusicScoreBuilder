package org.example.musicscorebuilder.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.example.musicscorebuilder.components.music.*;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class JsonFileService {
    private final ObjectMapper mapper;

    public JsonFileService() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public String toJsonString(Score score) {
        if (score == null) return "";
        try {
            return mapper.writeValueAsString(score);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
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

    public boolean isGzipCompressed(File file) {
        if (!file.isFile() || !file.canRead()) {
            return false;
        }

        try (InputStream fis = new FileInputStream(file)) {
            byte[] magic = new byte[2];
            int read = fis.read(magic);
            return read == 2 && magic[0] == (byte) 0x1F && magic[1] == (byte) 0x8B;
        } catch (IOException e) {
            return false;
        }
    }

    private void hydrate(Score score) {
        if (score == null || score.getModes() == null) return;

        for (ScoreMode mode : score.getModes()) {
            mode.setScore(score);

            if (mode.getMeasures() == null) continue;
            mode.updateMeasureLinks();

            TimeSignature activeTimeSignature = null;
            KeySignature activeKeySignature = null;

            for (Measure measure : mode.getMeasures()) {

                if (measure.getStaves().isEmpty()) {
                    if (measure.getPrev() != null && !measure.getPrev().getStaves().isEmpty()) {
                        measure.getStaves().addAll(measure.getPrev().getStaves());
                    } else if (mode.getStaves() != null && !mode.getStaves().isEmpty()) {
                        measure.getStaves().addAll(mode.getStaves());
                    }
                }

                // 1. Odtwarzanie TimeSignature
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
                    measure.setTimeSignature(inheritedTimeSig, false);
                } else {
                    TimeSignature defaultTimeSig = new TimeSignature(4, 4, TimeSignature.Type.FRACTIONAL, measure);
                    measure.setTimeSignature(defaultTimeSig, false);
                    activeTimeSignature = defaultTimeSig;
                }

                // 2. Odtwarzanie KeySignature
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

                if (measure.getRightBarline() != null) {
                    measure.getRightBarline().setParent(measure);
                }
                measure.recalculateSegmentDurations();
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