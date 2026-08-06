package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.musicscorebuilder.components.music.util.MeasureTimeSignatureAdjuster;
import org.example.musicscorebuilder.palette.PreDefinedTimeSignature;

import java.util.ArrayList;
import java.util.List;

public class ScoreMode {
    @JsonIgnore
    private Score score;

    private final ModeType type;
    private final BraceType braceType;
    private final Barline startBarline;
    private final List<Staff> staves = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();
    private final List<Slur> slurs = new ArrayList<>();

    @JsonCreator
    public ScoreMode(
            @JsonProperty("type") ModeType type,
            @JsonProperty("braceType") BraceType braceType,
            @JsonProperty("startBarline") Barline startBarline,
            @JsonProperty("staves") List<Staff> staves,
            @JsonProperty("measures") List<Measure> measures,
            @JsonProperty("slurs") List<Slur> slurs
    ) {
        this.type = type;
        this.braceType = braceType;
        this.startBarline = startBarline;
        if (staves != null) this.staves.addAll(staves);
        if (measures != null) this.measures.addAll(measures);
        if (slurs != null) this.slurs.addAll(slurs);
    }

    public ScoreMode(Score score, ModeType type) {
        this.score = score;
        this.type = type;
        this.braceType = type == ModeType.SOLO ? BraceType.NONE : BraceType.BRACE;
        this.startBarline = type == ModeType.SOLO
                ? new Barline(BarlineStyle.NONE, Barline.Type.START, null)
                : new Barline(BarlineStyle.SINGLE, Barline.Type.START, null);
        addDefaultStaves();
    }

    public void appendMeasures(int count) {
        for (int i = 0; i < count; i++) appendMeasure();
    }
    public void addSlur(Slur slur) { slurs.add(slur); }

    public void appendMeasure() {
        Measure measure = new Measure(staves);

        if (!measures.isEmpty()) {
            Measure lastMeasure = measures.getLast();
            measure.setKeySignature(lastMeasure.getKeySignature());

            TimeSignature lastTimeSig = lastMeasure.getTimeSignature();
            measure.setTimeSignature(new TimeSignature(
                    lastTimeSig.getBeat(),
                    lastTimeSig.getBeatType(),
                    lastTimeSig.getType(),
                    measure
            ));

            measure.setBarlineStyle(lastMeasure.getBarlineStyle());
            lastMeasure.setBarlineStyle(BarlineStyle.SINGLE);
        } else {
            MeasureTimeSignatureAdjuster.adjustSegmentsToTimeSignature(measure);
        }

        measures.add(measure);
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        measures.removeLast();
        if (measures.isEmpty()) return;
        measures.getLast().setBarlineStyle(BarlineStyle.FINAL);
    }

    public void removeSlur(Slur slur) { slurs.remove(slur); }

    public Score getScore() { return score; }
    public ModeType getType() { return type; }
    public BraceType getBraceType() { return braceType; }
    public Barline getStartBarline() { return startBarline; }
    public List<Staff> getStaves() { return staves; }
    public List<Measure> getMeasures() { return measures; }
    public List<Slur> getSlurs() { return slurs; }

    public void setScore(Score score) { this.score = score; }
    public void setTimeSignature(PreDefinedTimeSignature timeSig) {
        if (measures.isEmpty()) return;

        measures.forEach(m -> {
            m.setTimeSignature(new TimeSignature(
                    timeSig.getBeat(),
                    timeSig.getBeatType(),
                    timeSig.getType(),
                    m
            ));
        });
    }

    public void setKeySignature(Integer key) {
        if (measures.isEmpty()) return;
        measures.forEach(m -> m.setKeySignature(new KeySignature(key, m)));
    }

    private void addDefaultStaves() {
        switch (type) {
            case SOLO -> staves.add(new Staff(0, new Clef(ClefType.G)));
            case HARMONY -> {
                staves.add(new Staff(0, new Clef(ClefType.G)));
                staves.add(new Staff(1, new Clef(ClefType.F)));
            }
        }
    }
}