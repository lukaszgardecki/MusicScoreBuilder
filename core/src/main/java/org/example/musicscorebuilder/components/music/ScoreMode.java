package org.example.musicscorebuilder.components.music;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.frames.Frame;
import org.example.musicscorebuilder.components.music.frames.HeaderFrame;
import org.example.musicscorebuilder.components.music.frames.TextFrame;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;
import org.example.musicscorebuilder.components.music.frames.VerseTextMapper;
import org.example.musicscorebuilder.components.music.util.MeasureTimeSignatureAdjuster;

import java.util.*;

public class ScoreMode {
    @JsonIgnore
    private Score score;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private ScoreStyle style;

    private final ModeType type;
    private final BraceType braceType;
    private final Barline startBarline;
    private final List<Staff> staves = new ArrayList<>();
    private final List<Measure> measures = new ArrayList<>();
    private final List<Slur> slurs = new ArrayList<>();

    @JsonProperty("frames")
    private final List<Frame> frames = new ArrayList<>();

    @JsonIgnore
    private final Map<Integer, Verse> verses = new TreeMap<>();

    @JsonCreator
    public ScoreMode(
            @JsonProperty("type") ModeType type,
            @JsonProperty("braceType") BraceType braceType,
            @JsonProperty("startBarline") Barline startBarline,
            @JsonProperty("staves") List<Staff> staves,
            @JsonProperty("measures") List<Measure> measures,
            @JsonProperty("slurs") List<Slur> slurs,
            @JsonProperty("frames") List<Frame> frames,
            @JsonProperty("style") ScoreStyle style
    ) {
        this.type = type;
        this.braceType = braceType;
        this.startBarline = startBarline;
        this.style = style != null ? style : new ScoreStyle();
        if (staves != null) this.staves.addAll(staves);
        if (measures != null) {
            this.measures.addAll(measures);
            updateMeasureLinks();
        }
        if (slurs != null) {
            this.slurs.addAll(slurs);
            rebindSlurs();
        }
        if (frames != null) {
            this.frames.addAll(frames);
        }

        rebuildVersesIndex();
    }

    public ScoreMode(Score score, ModeType type, ScoreStyle style) {
        this.score = score;
        this.type = type;
        this.braceType = type == ModeType.SOLO ? BraceType.NONE : BraceType.BRACE;
        this.startBarline = type == ModeType.SOLO
                ? new Barline(BarlineStyle.NONE, Barline.Type.START, null)
                : new Barline(BarlineStyle.SINGLE, Barline.Type.START, null);
        this.style = style;
        addDefaultStaves();
        addDefaultHeaderFrame();
    }

    public ScoreStyle getStyle() {
        return style;
    }

    public void setStyle(ScoreStyle style) {
        this.style = style != null ? style : new ScoreStyle();
    }

    public void appendMeasures(int count) {
        for (int i = 0; i < count; i++) appendMeasure();
    }
    public void addSlur(Slur slur) { slurs.add(slur); }
    public void addFrame(Frame frame) { frames.add(frame); }

    public void appendMeasure() {
        Measure measure = new Measure(staves);
        measure.setParentMode(this);

        if (!measures.isEmpty()) {
            Measure lastMeasure = measures.get(measures.size() - 1);
            lastMeasure.setNext(measure);
            measure.setPrev(lastMeasure);

            measure.setKeySignature(lastMeasure.getKeySignature());

            TimeSignature lastTimeSig = lastMeasure.getTimeSignature();
            if (lastTimeSig != null) {
                measure.setTimeSignature(new TimeSignature(
                        lastTimeSig.getBeat(),
                        lastTimeSig.getBeatType(),
                        lastTimeSig.getType(),
                        measure
                ), false);
            }

            lastMeasure.setBarlineStyle(BarlineStyle.SINGLE);
        }

        measure.setBarlineStyle(BarlineStyle.FINAL);

        MeasureTimeSignatureAdjuster.adjustFromMeasure(measure);
        measures.add(measure);
    }

    public Measure removeMeasure(int index) {
        if (index < 0 || index >= measures.size()) return null;

        Measure removed = measures.remove(index);
        removed.setParentMode(null);
        removed.setPrev(null);
        removed.setNext(null);

        updateMeasureLinks();

        if (!measures.isEmpty()) {
            measures.get(measures.size() - 1).setBarlineStyle(BarlineStyle.FINAL);
        }

        validateAndCleanSlurs();
        rebuildVersesIndex();

        return removed;
    }

    public boolean removeMeasure(Measure measure) {
        if (measure == null) return false;
        int index = measures.indexOf(measure);
        if (index != -1) {
            removeMeasure(index);
            return true;
        }
        return false;
    }

    public void removeLastMeasure() {
        if (measures.isEmpty()) return;
        Measure removed = measures.remove(measures.size() - 1);
        removed.setPrev(null);
        removed.setParentMode(null);

        if (measures.isEmpty()) return;
        Measure last = measures.get(measures.size() - 1);
        last.setNext(null);
        last.setBarlineStyle(BarlineStyle.FINAL);
    }

    public void removeSlur(Slur slur) { slurs.remove(slur); }

    public Score getScore() { return score; }
    public ModeType getType() { return type; }
    public BraceType getBraceType() { return braceType; }
    public Barline getStartBarline() { return startBarline; }
    public List<Staff> getStaves() { return staves; }
    public List<Measure> getMeasures() { return measures; }
    public List<Slur> getSlurs() {
        updateSlurIds();
        return slurs;
    }

    @JsonProperty("frames")
    public List<Frame> getFrames() { return frames; }

    public void setFrames(List<Frame> frames) {
        this.frames.clear();
        if (frames != null) {
            this.frames.addAll(frames);
        }
    }

    @JsonIgnore
    public Map<Integer, Verse> getVerses() {
        return verses;
    }

    public Verse getOrCreateVerse(int verseNumber) {
        return verses.computeIfAbsent(verseNumber, Verse::new);
    }

    public void removeVerse(int verseNumber) {
        verses.remove(verseNumber);
        for (Note note : getAllNotes()) {
            note.removeLyric(verseNumber);
        }
    }

    public void populateTextFrameFromVerse(TextFrame textFrame, int verseNumber) {
        Verse verse = verses.get(verseNumber);
        if (verse == null) return;
        textFrame.addVerse(verse.toTextFrameVerse());
    }

    public void rebuildVersesIndex() {
        verses.clear();
        for (Note note : getAllNotes()) {
            if (note.getLyrics() != null) {
                note.getLyrics().forEach(lyric -> {
                    Verse verse = getOrCreateVerse(lyric.getVerse());
                    verse.addSyllable(note, lyric);
                    if (lyric.isLineBreakAfter()) {
                        verse.setCustomLineBreaks(true);
                    }
                });
            }
        }
    }

    @JsonIgnore
    public List<Note> getAllNotes() {
        List<Note> notes = new ArrayList<>();
        for (Measure m : measures) {
            if (m.getSegments() == null) continue;
            for (Segment seg : m.getSegments()) {
                if (seg.getStaffElements() == null) continue;
                for (List<Element> elements : seg.getStaffElements().values()) {
                    if (elements == null) continue;
                    for (Element el : elements) {
                        if (el instanceof Note note) {
                            notes.add(note);
                        }
                    }
                }
            }
        }
        return notes;
    }

    private boolean isNotePresentInMeasures(Note note) {
        if (note == null) return false;
        for (Measure m : measures) {
            for (Segment seg : m.getSegments()) {
                for (List<Element> elements : seg.getStaffElements().values()) {
                    for (Element el : elements) {
                        if (el == note) return true;
                    }
                }
            }
        }
        return false;
    }

    public void setScore(Score score) { this.score = score; }

    @JsonIgnore
    public void setNewTimeSignatureFromMeasure(TimeSignature timeSignature, Measure measure) {
        if (measure == null) return;

        int startIndex = measures.indexOf(measure);
        if (startIndex == -1) return;

        for (int i = startIndex; i < measures.size(); i++) {
            Measure m = measures.get(i);
            m.setTimeSignature(timeSignature != null ? new TimeSignature(
                    timeSignature.getBeat(),
                    timeSignature.getBeatType(),
                    timeSignature.getType(),
                    m
            ) : null, false);
        }

        MeasureTimeSignatureAdjuster.adjustFromMeasure(measure);
        validateAndCleanSlurs();
    }

    public void setNewKeySignatureFromMeasure(int key, Measure measure) {
        if (measure == null) return;

        int startIndex = measures.indexOf(measure);
        if (startIndex == -1) return;

        for (int i = startIndex; i < measures.size(); i++) {
            Measure m = measures.get(i);
            m.setKeySignature(new KeySignature(key, m));
        }
    }

    private void validateAndCleanSlurs() {
        if (slurs.isEmpty()) return;
        slurs.removeIf(slur -> slur.getStartNote() == null || slur.getEndNote() == null ||
                !isNotePresentInMeasures(slur.getStartNote()) || !isNotePresentInMeasures(slur.getEndNote()));
    }

    public void updateMeasureLinks() {
        for (int i = 0; i < measures.size(); i++) {
            Measure curr = measures.get(i);
            curr.setParentMode(this);
            curr.setPrev(i > 0 ? measures.get(i - 1) : null);
            curr.setNext(i < measures.size() - 1 ? measures.get(i + 1) : null);
        }
    }

    private void buildNoteIdMaps(Map<Note, String> noteToId, Map<String, Note> idToNote) {
        for (int mIdx = 0; mIdx < measures.size(); mIdx++) {
            Measure m = measures.get(mIdx);
            if (m.getSegments() == null) continue;

            List<Segment> segments = m.getSegments();
            for (int sIdx = 0; sIdx < segments.size(); sIdx++) {
                Segment seg = segments.get(sIdx);
                if (seg.getStaffElements() == null) continue;

                for (Map.Entry<?, List<Element>> entry : seg.getStaffElements().entrySet()) {
                    Object staffKey = entry.getKey();
                    List<Element> elements = entry.getValue();
                    if (elements == null) continue;

                    for (int eIdx = 0; eIdx < elements.size(); eIdx++) {
                        Element el = elements.get(eIdx);
                        if (el instanceof Note note) {
                            String id = mIdx + "_" + sIdx + "_" + staffKey + "_" + eIdx;
                            if (noteToId != null) noteToId.put(note, id);
                            if (idToNote != null) idToNote.put(id, note);
                        }
                    }
                }
            }
        }
    }

    public void updateSlurIds() {
        if (measures.isEmpty()) {
            slurs.clear();
            return;
        }

        Map<Note, String> noteToId = new HashMap<>();
        buildNoteIdMaps(noteToId, null);

        slurs.removeIf(slur -> slur.getStartNote() == null
                || slur.getEndNote() == null
                || !noteToId.containsKey(slur.getStartNote())
                || !noteToId.containsKey(slur.getEndNote()));

        for (Slur slur : slurs) {
            slur.setStartNoteId(noteToId.get(slur.getStartNote()));
            slur.setEndNoteId(noteToId.get(slur.getEndNote()));
        }
    }

    public void rebindSlurs() {
        if (measures.isEmpty()) {
            slurs.clear();
            return;
        }

        Map<String, Note> idToNote = new HashMap<>();
        buildNoteIdMaps(null, idToNote);

        for (Slur slur : slurs) {
            if (slur.getStartNoteId() != null) {
                slur.setStartNote(idToNote.get(slur.getStartNoteId()));
            }
            if (slur.getEndNoteId() != null) {
                slur.setEndNote(idToNote.get(slur.getEndNoteId()));
            }
        }

        slurs.removeIf(slur -> slur.getStartNote() == null || slur.getEndNote() == null);
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

    private void addDefaultHeaderFrame() {
        HeaderFrame frame = new HeaderFrame();
        frame.setTitle(score.getTitle());
        frame.setSubtitle(score.getSubtitle());
        frame.setComposer(score.getComposer());
        frame.setNumberNew(score.getNumberNew());
        frame.setNumberOld(score.getNumberOld());
        frame.setMarginTop(0.0);
        frame.setMarginBottom(style.getFrameDefMarginBottom());
        this.frames.add(frame);
    }
}