package org.example.musicscorebuilder.components.layout.engine;

import org.example.musicscorebuilder.components.frames.FrameLayout;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.util.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.music.frames.Frame;

import java.util.*;
import java.util.stream.Collectors;

public class LayoutEngine {
    private ScoreStyle style;
    private LayoutContext layoutContext;
    private final SystemJustifier systemJustifier;
    private final Map<Measure, MeasureLayout> measureCache = new IdentityHashMap<>();

    private final TieBuilder tieBuilder;
    private final SlurBuilder slurBuilder;
    private final VoltaBuilder voltaBuilder;
    private final FrameBuilder frameBuilder;
    private final CourtesyLayoutHandler courtesyLayoutHandler;
    private final LayoutLinker layoutLinker;
    private final JumpMarkBuilder jumpMarkBuilder;
    private final TempoBuilder tempoBuilder;

    public LayoutEngine() {
        this.systemJustifier = new SystemJustifier();
        this.tieBuilder = new TieBuilder();
        this.slurBuilder = new SlurBuilder();
        this.voltaBuilder = new VoltaBuilder(measureCache);
        this.jumpMarkBuilder = new JumpMarkBuilder(measureCache);
        this.tempoBuilder = new TempoBuilder(measureCache);
        this.frameBuilder = new FrameBuilder();
        this.courtesyLayoutHandler = new CourtesyLayoutHandler();
        this.layoutLinker = new LayoutLinker();
    }

    public ScoreLayout compute(ScoreMode scoreMode, LayoutContext layoutContext) {
        this.style = scoreMode.getStyle();
        this.layoutContext = layoutContext;
        invalidateCacheIfNeeded(scoreMode);

        slurBuilder.clearCache();
        tieBuilder.clearCache();
        frameBuilder.clearCache();

        ScoreLayout scoreLayout = new ScoreLayout(scoreMode.getScore(), style);
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);
        BraceType systemBraceType = scoreMode.getBraceType();
        SystemLayout currentSystem = addNewSystemToPage(currentPage, systemBraceType);

        Map<Integer, List<Frame>> framesByMeasureIndex = new HashMap<>();
        for (Frame frame : scoreMode.getFrames()) {
            framesByMeasureIndex.computeIfAbsent(frame.getMeasureIndex(), k -> new ArrayList<>()).add(frame);
        }

        List<Measure> measures = scoreMode.getMeasures();
        for (int i = 0; i < measures.size(); i++) {
            Measure measure = measures.get(i);
            List<Frame> frames = framesByMeasureIndex.get(i);

            List<Frame> beforeFrames = Collections.emptyList();
            List<Frame> afterFrames = Collections.emptyList();

            if (frames != null && !frames.isEmpty()) {
                Map<Boolean, List<Frame>> partitioned = frames.stream()
                        .collect(Collectors.partitioningBy(Frame::isBeforeMeasure));
                beforeFrames = partitioned.get(true);
                afterFrames = partitioned.get(false);
            }

            for (Frame frameData : beforeFrames) {
                currentSystem = insertFrameBlock(frameData, currentSystem, scoreLayout, systemBraceType);
                currentPage = currentSystem.getPageLayout();
            }

            MeasureLayout measureLayout = getOrCreateMeasureLayout(measure, currentSystem);
            double courtesyPadding = courtesyLayoutHandler.calculateCourtesyPadding(measure, measureLayout);

            boolean forcedBreak = false;
            if (!currentSystem.getMeasures().isEmpty()) {
                Measure previousMeasure = currentSystem.getMeasures().get(currentSystem.getMeasures().size() - 1).getMeasure();
                forcedBreak = previousMeasure.hasSystemBreak();
            }

            boolean needsNewSystem = forcedBreak || !canFitMeasureInSystem(currentPage, currentSystem, measureLayout, courtesyPadding);

            if (needsNewSystem) {
                courtesyLayoutHandler.addCourtesyAttributesToLastMeasure(currentSystem, measure);
                currentSystem = finalizeSystemAndCreateNext(currentSystem, scoreLayout, systemBraceType, measureLayout);
                currentPage = currentSystem.getPageLayout();
            }

            if (currentSystem.getMeasures().isEmpty()) {
                Barline startBarline = scoreMode.getStartBarline();
                add1stMeasureAttributes(startBarline, measureLayout, scoreLayout);
            }

            double startX = currentSystem.getMeasures().isEmpty() ? currentSystem.getBraceWidth() : currentSystem.getWidth();
            measureLayout.setX(startX);
            currentSystem.add(measureLayout);

            for (Frame frameData : afterFrames) {
                currentSystem = insertFrameBlock(frameData, currentSystem, scoreLayout, systemBraceType);
                currentPage = currentSystem.getPageLayout();
            }
        }

        if (!currentSystem.getMeasures().isEmpty()) {
            systemJustifier.justify(currentSystem);
        }

        postProcessLayout(scoreMode, scoreLayout);
        return scoreLayout;
    }

    // ========================================================================
    // CACHE & SETUP
    // ========================================================================

    private void invalidateCacheIfNeeded(ScoreMode scoreMode) {
        measureCache.keySet().removeIf(Measure::isDirty);
        measureCache.keySet().retainAll(scoreMode.getMeasures());
    }

    private MeasureLayout getOrCreateMeasureLayout(Measure measure, SystemLayout currentSystem) {
        if (measureCache.containsKey(measure) && !measure.isDirty()) {
            MeasureLayout measureLayout = measureCache.get(measure);
            measureLayout.remove1stMeasureAttributes();

            var segments = measureLayout.getSegments();
            if (!segments.isEmpty() && segments.get(segments.size() - 1).getSegment().getType() != SegmentType.BARLINE) {
                addEndBarline(measure, measureLayout);
            }

            measureLayout.resetLayoutState();
            measureLayout.setParent(currentSystem);
            extractNotesToCacheMaps(measureLayout);
            return measureLayout;
        }

        MeasureLayout measureLayout = createMeasureLayout(measure, currentSystem);
        measureCache.put(measure, measureLayout);
        measure.setDirty(false);
        return measureLayout;
    }

    private void extractNotesToCacheMaps(MeasureLayout measureLayout) {
        for (SegmentLayout segment : measureLayout.getSegments()) {
            for (ElementLayout element : segment.getElements()) {
                if (element instanceof NoteLayout noteLayout) {
                    slurBuilder.putNote(noteLayout.getNote(), noteLayout);
                    if (noteLayout.getNote().isTieStart()) {
                        tieBuilder.addNoteLayout(noteLayout);
                    }
                }
            }
        }
    }

    // ========================================================================
    // PAGINATION & SYSTEM BREAKS
    // ========================================================================

    private boolean canFitMeasureInSystem(PageLayout page, SystemLayout system, MeasureLayout measureLayout, double courtesyPadding) {
        double requiredSpace = measureLayout.getWidth() + courtesyPadding;
        double availableSpace = page.getEffectiveWidth() - system.getWidth();
        return availableSpace >= requiredSpace;
    }

    private SystemLayout finalizeSystemAndCreateNext(SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType, MeasureLayout nextMeasureLayout) {
        systemJustifier.justify(currentSystem);

        PageLayout currentPage = currentSystem.getPageLayout();
        boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < currentSystem.getHeight() + style.getSystemSpacing();

        if (noSpaceForNextSystem) {
            currentPage = createPageLayout(scoreLayout);
            scoreLayout.addPageLayout(currentPage);
        }

        SystemLayout newSystem = addNewSystemToPage(currentPage, systemBraceType);
        nextMeasureLayout.setX(newSystem.getWidth());
        nextMeasureLayout.setParent(newSystem);

        return newSystem;
    }

    private SystemLayout addNewSystemToPage(PageLayout pageLayout, BraceType systemBraceType) {
        boolean previousIsSystem = !pageLayout.getBlocks().isEmpty()
                && pageLayout.getBlocks().get(pageLayout.getBlocks().size() - 1) instanceof SystemLayout;

        if (previousIsSystem) {
            pageLayout.setLastSystemSpaceBelow(style.getSystemSpacing());
        }

        var newSystem = new SystemLayout(pageLayout, systemBraceType);
        pageLayout.addBlock(newSystem);
        return newSystem;
    }

    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
        return new PageLayout(scoreLayout, scoreLayout.getPages().size());
    }

    private SystemLayout insertFrameBlock(Frame frameData, SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType) {
        PageLayout currentPage = currentSystem.getPageLayout();

        if (!currentSystem.getMeasures().isEmpty()) {
            systemJustifier.justify(currentSystem);
        } else {
            currentPage.getBlocks().remove(currentSystem);
        }

        FrameLayout frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);

        if (currentPage.getRemainingHeight() < frameLayout.getHeight()) {
            currentPage = createPageLayout(scoreLayout);
            scoreLayout.addPageLayout(currentPage);
            frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);
        }

        currentPage.addBlock(frameLayout);
        return addNewSystemToPage(currentPage, systemBraceType);
    }

    // ========================================================================
    // ATTRIBUTES
    // ========================================================================

    private void add1stMeasureAttributes(Barline startBarline, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;
        Measure measure = measureLayout.getMeasure();

        if (isFirstMeasure && measure.getTimeSignature() != null && measure.getTimeSignature().isVisible()) {
            measureLayout.addSystemTimeSignature(measure.getTimeSignature());
        }

        Measure prevMeasure = measure.getPrev();
        boolean isKeyChange = prevMeasure != null
                && measure.getKeySignature() != null
                && prevMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(prevMeasure.getKeySignature());

        if (!isKeyChange) {
            measureLayout.addSystemKeySignature(measure.getKeySignature());
        }

        measureLayout.addSystemClef();
        if (startBarline != null) {
            measureLayout.addSystemStartBarline(startBarline);
        }
    }

    // ========================================================================
    // MEASURE BUILDING
    // ========================================================================

    private MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout) {
        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout, style);

        for (Staff staff : measure.getStaves()) {
            measureLayout.add(new StaffLayout(staff, measureLayout, style));
        }

        addPermanentAttributesIfNeeded(measure, measureLayout);
        GroupBeamBuilder groupBeamBuilder = populateMeasureSegments(measure, measureLayout);
        addEndBarline(measure, measureLayout);

        measureLayout.setBeamGroups(groupBeamBuilder.build());
        return measureLayout;
    }

    private void addPermanentAttributesIfNeeded(Measure measure, MeasureLayout measureLayout) {
        Measure prevMeasure = measure.getPrev();
        if (prevMeasure == null) return;

        if (measure.getKeySignature() != null && prevMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(prevMeasure.getKeySignature())) {
            SegmentLayout segment = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
            segment.addKeySignature(measure.getKeySignature());
            measureLayout.add(segment);
        }

        TimeSignature currentTS = measure.getTimeSignature();
        TimeSignature prevTS = prevMeasure.getTimeSignature();

        if (currentTS != null && currentTS.isVisible() && prevTS != null) {
            if (!currentTS.equals(prevTS)) {
                SegmentLayout segment = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
                segment.addTimeSignature(currentTS);
                measureLayout.add(segment);
            }
        }
    }

    private GroupBeamBuilder populateMeasureSegments(Measure measure, MeasureLayout measureLayout) {
        GroupBeamBuilder groupBeamBuilder = new GroupBeamBuilder();
        for (Segment segment : measure.getSegments()) {
            SegmentLayout segmentLayout = new SegmentLayout(segment, measureLayout);
            for (StaffLayout staff : measureLayout.getStaffs()) {
                for (Element element : segment.getElementsByStaff(staff.getStaffIndex())) {
                    if (element instanceof Barline barline) {
                        segmentLayout.addByStaff(staff, new BarlineLayout(barline, staff, segmentLayout));
                    } else if (element instanceof Note note) {
                        NoteLayout noteLayout = new NoteLayout(note, staff, segmentLayout);
                        List<LyricLayout> lyrics = createSingleLyricLine(noteLayout, layoutContext.activeVerse());
                        noteLayout.setLyrics(lyrics);
                        segmentLayout.addByStaff(staff, noteLayout);

                        slurBuilder.putNote(note, noteLayout);
                        if (note.isTieStart()) {
                            tieBuilder.addNoteLayout(noteLayout);
                        }

                        if (note.isBeamed()) groupBeamBuilder.add(noteLayout);
                    } else if (element instanceof Rest rest) {
                        RestLayout restLayout = new RestLayout(rest, staff, segmentLayout);
                        segmentLayout.addByStaff(staff, restLayout);
                    }
                }
            }
            measureLayout.add(segmentLayout);
        }
        return groupBeamBuilder;
    }

    private void addEndBarline(Measure measure, MeasureLayout measureLayout) {
        Segment endBarlineSegment = new Segment(SegmentType.BARLINE, measure);
        SegmentLayout endBarlineSegLayout = new SegmentLayout(endBarlineSegment, measureLayout);
        for (StaffLayout staffLayout : measureLayout.getStaffs()) {
            endBarlineSegment.addElement(staffLayout.getStaffIndex(), measure.getRightBarline());
            endBarlineSegLayout.addByStaff(staffLayout, new BarlineLayout(measure.getRightBarline(), staffLayout, endBarlineSegLayout));
        }
        measureLayout.add(endBarlineSegLayout);
    }

    // ========================================================================
    // POST-PROCESSING (Text Frames, Slurs, Ties, Segments)
    // ========================================================================

    private void postProcessLayout(ScoreMode scoreMode, ScoreLayout scoreLayout) {
        List<PageLayout> pages = scoreLayout.getPages();
        layoutLinker.linkAllSegments(pages);
        layoutLinker.linkVoiceElements(pages);
        frameBuilder.updateTextFrames(scoreMode.getVerses(), scoreMode.getFrames(), pages);
        tieBuilder.buildTies(pages);
        slurBuilder.buildSlurs(scoreMode.getSlurs(), pages);
        voltaBuilder.buildVoltas(scoreMode.getVoltas(), scoreLayout);
        jumpMarkBuilder.buildJumpMarks(scoreMode.getJumpMarks(), scoreLayout);
        tempoBuilder.buildTempos(scoreMode.getTempos());
    }

    private List<LyricLayout> createSingleLyricLine(NoteLayout noteLayout, int verseNumber) {
        List<LyricLayout> result = new ArrayList<>();
        if (noteLayout == null || noteLayout.getNote() == null) return result;

        Note note = noteLayout.getNote();
        Lyric lyric = note.getLyric(verseNumber);

        if (lyric != null) {
            boolean hasText = lyric.getText() != null && !lyric.getText().trim().isEmpty();
            if (hasText || lyric.isConnected()) {
                result.add(new LyricLayout(lyric, noteLayout));
            }
        }

        return result;
    }
}