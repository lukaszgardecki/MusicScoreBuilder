//package org.example.musicscorebuilder.components.layout.engine;
//
//import org.example.musicscorebuilder.components.frames.FrameLayout;
//import org.example.musicscorebuilder.components.layout.*;
//import org.example.musicscorebuilder.components.layout.util.*;
//import org.example.musicscorebuilder.components.music.*;
//import org.example.musicscorebuilder.components.music.frames.Frame;
//
//import java.util.*;
//
//public class LayoutEngine {
//    private ScoreStyle style;
//    private LayoutContext layoutContext;
//    private final SystemJustifier systemJustifier;
//    private final Map<Measure, MeasureLayout> measureCache = new IdentityHashMap<>();
//
//    private final TieBuilder tieBuilder;
//    private final SlurBuilder slurBuilder;
//    private final VoltaBuilder voltaBuilder;
//    private final FrameBuilder frameBuilder;
//    private final CourtesyLayoutHandler courtesyLayoutHandler;
//    private final LayoutLinker layoutLinker;
//    private final JumpMarkBuilder jumpMarkBuilder;
//    private final TempoBuilder tempoBuilder;
//
//    public LayoutEngine() {
//        this.systemJustifier = new SystemJustifier();
//        this.tieBuilder = new TieBuilder();
//        this.slurBuilder = new SlurBuilder();
//        this.voltaBuilder = new VoltaBuilder(measureCache);
//        this.jumpMarkBuilder = new JumpMarkBuilder(measureCache);
//        this.tempoBuilder = new TempoBuilder(measureCache);
//        this.frameBuilder = new FrameBuilder();
//        this.courtesyLayoutHandler = new CourtesyLayoutHandler();
//        this.layoutLinker = new LayoutLinker();
//    }
//
//    public ScoreLayout compute(ScoreMode scoreMode, LayoutContext layoutContext) {
//        this.style = scoreMode.getStyle();
//        this.layoutContext = layoutContext;
//        invalidateCacheIfNeeded(scoreMode);
//
//        slurBuilder.clearCache();
//        tieBuilder.clearCache();
//        frameBuilder.clearCache();
//
//        ScoreLayout scoreLayout = new ScoreLayout(scoreMode.getScore(), style);
//        PageLayout currentPage = createPageLayout(scoreLayout);
//        scoreLayout.addPageLayout(currentPage);
//        BraceType systemBraceType = scoreMode.getBraceType();
//        SystemLayout currentSystem = addNewSystemToPage(currentPage, systemBraceType);
//
//        Map<Integer, List<Frame>> framesByMeasureIndex = new HashMap<>();
//        List<Frame> scoreFrames = scoreMode.getFrames();
//        for (int i = 0; i < scoreFrames.size(); i++) {
//            Frame frame = scoreFrames.get(i);
//            framesByMeasureIndex.computeIfAbsent(frame.getMeasureIndex(), k -> new ArrayList<>()).add(frame);
//        }
//
//        List<Measure> measures = scoreMode.getMeasures();
//        for (int i = 0; i < measures.size(); i++) {
//            Measure measure = measures.get(i);
//            List<Frame> frames = framesByMeasureIndex.get(i);
//
//            List<Frame> beforeFrames = Collections.emptyList();
//            List<Frame> afterFrames = Collections.emptyList();
//
//            if (frames != null && !frames.isEmpty()) {
//                beforeFrames = new ArrayList<>();
//                afterFrames = new ArrayList<>();
//                for (int f = 0; f < frames.size(); f++) {
//                    Frame frameData = frames.get(f);
//                    if (frameData.isBeforeMeasure()) {
//                        beforeFrames.add(frameData);
//                    } else {
//                        afterFrames.add(frameData);
//                    }
//                }
//            }
//
//            for (int f = 0; f < beforeFrames.size(); f++) {
//                Frame frameData = beforeFrames.get(f);
//                currentSystem = insertFrameBlock(frameData, currentSystem, scoreLayout, systemBraceType);
//                currentPage = currentSystem.getPageLayout();
//            }
//
//            MeasureLayout measureLayout = getOrCreateMeasureLayout(measure, currentSystem);
//            double courtesyPadding = courtesyLayoutHandler.calculateCourtesyPadding(measure, measureLayout);
//
//            boolean forcedBreak = false;
//            List<MeasureLayout> systemMeasures = currentSystem.getMeasures();
//            if (!systemMeasures.isEmpty()) {
//                Measure previousMeasure = systemMeasures.get(systemMeasures.size() - 1).getMeasure();
//                forcedBreak = previousMeasure.hasSystemBreak();
//            }
//
//            boolean needsNewSystem = forcedBreak || !canFitMeasureInSystem(currentPage, currentSystem, measureLayout, courtesyPadding);
//
//            if (needsNewSystem) {
//                courtesyLayoutHandler.addCourtesyAttributesToLastMeasure(currentSystem, measure);
//                currentSystem = finalizeSystemAndCreateNext(currentSystem, scoreLayout, systemBraceType, measureLayout);
//                currentPage = currentSystem.getPageLayout();
//            }
//
//            if (currentSystem.getMeasures().isEmpty()) {
//                Barline startBarline = scoreMode.getStartBarline();
//                add1stMeasureAttributes(startBarline, measureLayout, scoreLayout);
//            }
//
//            double startX = currentSystem.getMeasures().isEmpty() ? currentSystem.getBraceWidth() : currentSystem.getWidth();
//            measureLayout.setX(startX);
//            currentSystem.add(measureLayout);
//
//            for (int f = 0; f < afterFrames.size(); f++) {
//                Frame frameData = afterFrames.get(f);
//                currentSystem = insertFrameBlock(frameData, currentSystem, scoreLayout, systemBraceType);
//                currentPage = currentSystem.getPageLayout();
//            }
//        }
//
//        if (!currentSystem.getMeasures().isEmpty()) {
//            applyStaffVerticalLayout(currentSystem);
//            systemJustifier.justify(currentSystem);
//        }
//
//        postProcessLayout(scoreMode, scoreLayout);
//        return scoreLayout;
//    }
//
//    // ========================================================================
//    // CACHE & SETUP
//    // ========================================================================
//
//    private void invalidateCacheIfNeeded(ScoreMode scoreMode) {
//        measureCache.keySet().removeIf(Measure::isDirty);
//        Set<Measure> activeMeasures = Collections.newSetFromMap(new IdentityHashMap<>());
//        activeMeasures.addAll(scoreMode.getMeasures());
//        measureCache.keySet().retainAll(activeMeasures);
//    }
//
//    private MeasureLayout getOrCreateMeasureLayout(Measure measure, SystemLayout currentSystem) {
//        if (measureCache.containsKey(measure) && !measure.isDirty()) {
//            MeasureLayout measureLayout = measureCache.get(measure);
//            measureLayout.remove1stMeasureAttributes();
//
//            List<SegmentLayout> segments = measureLayout.getSegments();
//            if (!segments.isEmpty() && segments.get(segments.size() - 1).getSegment().getType() != SegmentType.BARLINE) {
//                addEndBarline(measure, measureLayout);
//            }
//
//            measureLayout.resetLayoutState();
//            measureLayout.setParent(currentSystem);
//            extractNotesToCacheMaps(measureLayout);
//            return measureLayout;
//        }
//
//        MeasureLayout measureLayout = createMeasureLayout(measure, currentSystem);
//        measureCache.put(measure, measureLayout);
//        measure.setDirty(false);
//        return measureLayout;
//    }
//
//    private void extractNotesToCacheMaps(MeasureLayout measureLayout) {
//        List<SegmentLayout> segments = measureLayout.getSegments();
//        for (int s = 0; s < segments.size(); s++) {
//            SegmentLayout segment = segments.get(s);
//            List<ElementLayout> elements = segment.getElements();
//            for (int e = 0; e < elements.size(); e++) {
//                ElementLayout element = elements.get(e);
//                if (element instanceof NoteLayout noteLayout) {
//                    slurBuilder.putNote(noteLayout.getNote(), noteLayout);
//                    if (noteLayout.getNote().isTieStart()) {
//                        tieBuilder.addNoteLayout(noteLayout);
//                    }
//                }
//            }
//        }
//    }
//
//    // ========================================================================
//    // PAGINATION & SYSTEM BREAKS
//    // ========================================================================
//
//    private boolean canFitMeasureInSystem(PageLayout page, SystemLayout system, MeasureLayout measureLayout, double courtesyPadding) {
//        double requiredSpace = measureLayout.getWidth() + courtesyPadding;
//        double availableSpace = page.getEffectiveWidth() - system.getWidth();
//        return availableSpace >= requiredSpace;
//    }
//
//    private SystemLayout finalizeSystemAndCreateNext(SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType, MeasureLayout nextMeasureLayout) {
//        applyStaffVerticalLayout(currentSystem);
//        systemJustifier.justify(currentSystem);
//
//        PageLayout currentPage = currentSystem.getPageLayout();
//        boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < currentSystem.getHeight() + style.getSystemSpacing();
//
//        if (noSpaceForNextSystem) {
//            currentPage = createPageLayout(scoreLayout);
//            scoreLayout.addPageLayout(currentPage);
//        }
//
//        SystemLayout newSystem = addNewSystemToPage(currentPage, systemBraceType);
//        nextMeasureLayout.setX(newSystem.getWidth());
//        nextMeasureLayout.setParent(newSystem);
//
//        return newSystem;
//    }
//
//    private SystemLayout addNewSystemToPage(PageLayout pageLayout, BraceType systemBraceType) {
//        var newSystem = new SystemLayout(pageLayout, systemBraceType);
//        newSystem.setSpaceBelow(style.getSystemSpacing());
//        pageLayout.addBlock(newSystem);
//        return newSystem;
//    }
//
//    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
//        return new PageLayout(scoreLayout, scoreLayout.getPages().size());
//    }
//
//    private SystemLayout insertFrameBlock(Frame frameData, SystemLayout currentSystem, ScoreLayout scoreLayout, BraceType systemBraceType) {
//        PageLayout currentPage = currentSystem.getPageLayout();
//
//        if (!currentSystem.getMeasures().isEmpty()) {
//            systemJustifier.justify(currentSystem);
//        } else {
//            currentPage.getBlocks().remove(currentSystem);
//        }
//
//        FrameLayout frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);
//
//        if (currentPage.getRemainingHeight() < frameLayout.getHeight()) {
//            currentPage = createPageLayout(scoreLayout);
//            scoreLayout.addPageLayout(currentPage);
//            frameLayout = frameBuilder.createFrameLayout(currentPage, style, frameData);
//        }
//
//        currentPage.addBlock(frameLayout);
//        return addNewSystemToPage(currentPage, systemBraceType);
//    }
//
//    // ========================================================================
//    // ATTRIBUTES
//    // ========================================================================
//
//    private void add1stMeasureAttributes(Barline startBarline, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
//        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;
//        Measure measure = measureLayout.getMeasure();
//
//        if (isFirstMeasure && measure.getTimeSignature() != null && measure.getTimeSignature().isVisible()) {
//            measureLayout.addSystemTimeSignature(measure.getTimeSignature());
//        }
//
//        Measure prevMeasure = measure.getPrev();
//        boolean isKeyChange = prevMeasure != null
//                && measure.getKeySignature() != null
//                && prevMeasure.getKeySignature() != null
//                && !measure.getKeySignature().equals(prevMeasure.getKeySignature());
//
//        if (!isKeyChange) {
//            measureLayout.addSystemKeySignature(measure.getKeySignature());
//        }
//
//        measureLayout.addSystemClef();
//        if (startBarline != null) {
//            measureLayout.addSystemStartBarline(startBarline);
//        }
//    }
//
//    // ========================================================================
//    // MEASURE BUILDING
//    // ========================================================================
//
//    private MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout) {
//        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout, style);
//
//        List<Staff> staves = measure.getStaves();
//        for (int i = 0; i < staves.size(); i++) {
//            measureLayout.add(new StaffLayout(staves.get(i), measureLayout, style));
//        }
//
//        addPermanentAttributesIfNeeded(measure, measureLayout);
//        GroupBeamBuilder groupBeamBuilder = populateMeasureSegments(measure, measureLayout);
//        addEndBarline(measure, measureLayout);
//
//        measureLayout.setBeamGroups(groupBeamBuilder.build());
//        return measureLayout;
//    }
//
//    private void addPermanentAttributesIfNeeded(Measure measure, MeasureLayout measureLayout) {
//        Measure prevMeasure = measure.getPrev();
//        if (prevMeasure == null) return;
//
//        if (measure.getKeySignature() != null && prevMeasure.getKeySignature() != null
//                && !measure.getKeySignature().equals(prevMeasure.getKeySignature())) {
//            SegmentLayout segment = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
//            segment.addKeySignature(measure.getKeySignature());
//            measureLayout.add(segment);
//        }
//
//        TimeSignature currentTS = measure.getTimeSignature();
//        TimeSignature prevTS = prevMeasure.getTimeSignature();
//
//        if (currentTS != null && currentTS.isVisible() && prevTS != null) {
//            if (!currentTS.equals(prevTS)) {
//                SegmentLayout segment = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
//                segment.addTimeSignature(currentTS);
//                measureLayout.add(segment);
//            }
//        }
//    }
//
//    private GroupBeamBuilder populateMeasureSegments(Measure measure, MeasureLayout measureLayout) {
//        GroupBeamBuilder groupBeamBuilder = new GroupBeamBuilder();
//        List<Segment> segments = measure.getSegments();
//        List<StaffLayout> staffs = measureLayout.getStaffs();
//
//        for (int s = 0; s < segments.size(); s++) {
//            Segment segment = segments.get(s);
//            SegmentLayout segmentLayout = new SegmentLayout(segment, measureLayout);
//
//            for (int st = 0; st < staffs.size(); st++) {
//                StaffLayout staff = staffs.get(st);
//                List<Element> elements = segment.getElementsByStaff(staff.getStaffIndex());
//                for (int e = 0; e < elements.size(); e++) {
//                    Element element = elements.get(e);
//                    if (element instanceof Barline barline) {
//                        segmentLayout.addByStaff(staff, new BarlineLayout(barline, staff, segmentLayout));
//                    } else if (element instanceof Note note) {
//                        NoteLayout noteLayout = new NoteLayout(note, staff, segmentLayout);
//                        List<LyricLayout> lyrics = createSingleLyricLine(noteLayout, layoutContext.activeVerse());
//                        noteLayout.setLyrics(lyrics);
//                        segmentLayout.addByStaff(staff, noteLayout);
//
//                        slurBuilder.putNote(note, noteLayout);
//                        if (note.isTieStart()) {
//                            tieBuilder.addNoteLayout(noteLayout);
//                        }
//
//                        if (note.isBeamed()) groupBeamBuilder.add(noteLayout);
//                    } else if (element instanceof Rest rest) {
//                        RestLayout restLayout = new RestLayout(rest, staff, segmentLayout);
//                        segmentLayout.addByStaff(staff, restLayout);
//                    }
//                }
//            }
//            measureLayout.add(segmentLayout);
//        }
//        return groupBeamBuilder;
//    }
//
//    private void addEndBarline(Measure measure, MeasureLayout measureLayout) {
//        Segment endBarlineSegment = new Segment(SegmentType.BARLINE, measure);
//        SegmentLayout endBarlineSegLayout = new SegmentLayout(endBarlineSegment, measureLayout);
//        List<StaffLayout> staffs = measureLayout.getStaffs();
//        for (int i = 0; i < staffs.size(); i++) {
//            StaffLayout staffLayout = staffs.get(i);
//            endBarlineSegment.addElement(staffLayout.getStaffIndex(), measure.getRightBarline());
//            endBarlineSegLayout.addByStaff(staffLayout, new BarlineLayout(measure.getRightBarline(), staffLayout, endBarlineSegLayout));
//        }
//        measureLayout.add(endBarlineSegLayout);
//    }
//
//    // ========================================================================
//    // POST-PROCESSING (Text Frames, Slurs, Ties, Segments)
//    // ========================================================================
//
//    private void postProcessLayout(ScoreMode scoreMode, ScoreLayout scoreLayout) {
//        List<PageLayout> pages = scoreLayout.getPages();
//        layoutLinker.linkAllSegments(pages);
//        layoutLinker.linkVoiceElements(pages);
//        frameBuilder.updateTextFrames(scoreMode.getVerses(), scoreMode.getFrames(), pages);
//
//        applySystemsVerticalLayout(scoreLayout);
//        applyPagesVerticalLayout(scoreLayout);
//
//        tieBuilder.buildTies(pages);
//        slurBuilder.buildSlurs(scoreMode.getSlurs(), pages);
//        voltaBuilder.buildVoltas(scoreMode.getVoltas(), scoreLayout);
//        jumpMarkBuilder.buildJumpMarks(scoreMode.getJumpMarks(), scoreLayout);
//        tempoBuilder.buildTempos(scoreMode.getTempos());
//    }
//
//    private void applySystemsVerticalLayout(ScoreLayout scoreLayout) {
//        List<PageLayout> pages = scoreLayout.getPages();
//        for (int p = 0; p < pages.size(); p++) {
//            List<SystemLayout> systems = pages.get(p).getSystems();
//            for (int s = 0; s < systems.size(); s++) {
//                applyStaffVerticalLayout(systems.get(s));
//            }
//        }
//    }
//
//    private void applyStaffVerticalLayout(SystemLayout system) {
//        List<MeasureLayout> measures = system.getMeasures();
//        if (measures.isEmpty()) return;
//
//        int staffCount = measures.get(0).getStaffs().size();
//        if (staffCount == 0) return;
//
//        double currentY = 0.0;
//        double prevNominalHeight = 0.0;
//        double prevLyricBottomOverflow = 0.0;
//
//        for (int k = 0; k < staffCount; k++) {
//            // 1. Obliczenie maxSystemNoteBodyOverflow w systemie
//            double maxSystemNoteBodyOverflow = 0.0;
//            for (MeasureLayout measure : measures) {
//                if (k < measure.getStaffs().size()) {
//                    maxSystemNoteBodyOverflow = Math.max(maxSystemNoteBodyOverflow, measure.getStaffs().get(k).getNoteBodyBottomOverflow());
//                }
//            }
//
//            // 2. Ustawienie tymczasowego Y oraz overflow ciała nut
//            for (MeasureLayout measure : measures) {
//                if (k < measure.getStaffs().size()) {
//                    StaffLayout staff = measure.getStaffs().get(k);
//                    staff.setSystemNoteBodyBottomOverflow(maxSystemNoteBodyOverflow);
//                    staff.setY(currentY);
//                }
//            }
//
//            // 3. Wyznaczenie przepełnień dla bieżącego poziomu Y
//            double maxTopOverflow = 0.0;
//            double maxLyricBottomOverflow = 0.0;
//            double nominalHeight = 0.0;
//
//            for (MeasureLayout measure : measures) {
//                if (k < measure.getStaffs().size()) {
//                    StaffLayout staff = measure.getStaffs().get(k);
//                    maxTopOverflow = Math.max(maxTopOverflow, staff.getTopOverflow());
//                    nominalHeight = Math.max(nominalHeight, staff.getHeight());
//                    maxLyricBottomOverflow = Math.max(maxLyricBottomOverflow, staff.getLyricBottomOverflow());
//                }
//            }
//
//            // 4. Obliczenie docelowego Y
//            if (k == 0) {
//                currentY = 0.0;
//            } else {
//                if (prevLyricBottomOverflow > 0.0 || maxTopOverflow > 0.0) {
//                    double absoluteBottomTextY = currentY + prevNominalHeight + prevLyricBottomOverflow;
//                    currentY = absoluteBottomTextY + maxTopOverflow;
//                } else {
//                    currentY += style.getStaffSpacing();
//                }
//
//                for (MeasureLayout measure : measures) {
//                    if (k < measure.getStaffs().size()) {
//                        measure.getStaffs().get(k).setY(currentY);
//                    }
//                }
//            }
//
//            // 5. Ustawienie odstępu pod poprzednią pięciolinią
//            if (k > 0) {
//                int prevStaffIdx = k - 1;
//                double prevStaffY = measures.get(0).getStaffs().get(prevStaffIdx).getY();
//                double clearance = (currentY - prevStaffY) - prevNominalHeight;
//
//                for (MeasureLayout measure : measures) {
//                    if (prevStaffIdx < measure.getStaffs().size()) {
//                        measure.getStaffs().get(prevStaffIdx).setSpaceBelow(clearance);
//                    }
//                }
//            }
//
//            prevNominalHeight = nominalHeight;
//            prevLyricBottomOverflow = maxLyricBottomOverflow;
//        }
//
//        // 6. Vyczyszczenie odstępu dla ostatniej pięciolinii
//        int lastStaffIdx = staffCount - 1;
//        for (MeasureLayout measure : measures) {
//            if (lastStaffIdx < measure.getStaffs().size()) {
//                measure.getStaffs().get(lastStaffIdx).setSpaceBelow(0.0);
//            }
//        }
//    }
//
//    private void applyPagesVerticalLayout(ScoreLayout scoreLayout) {
//        List<PageLayout> pages = scoreLayout.getPages();
//        for (int p = 0; p < pages.size(); p++) {
//            PageLayout page = pages.get(p);
//            List<PageBlockLayout> blocks = page.getBlocks();
//            if (blocks.isEmpty()) continue;
//
//            double currentY = page.getMarginTop();
//
//            for (int i = 0; i < blocks.size(); i++) {
//                PageBlockLayout block = blocks.get(i);
//                if (block instanceof SystemLayout system) {
//                    boolean nextIsSystem = (i + 1 < blocks.size()) && (blocks.get(i + 1) instanceof SystemLayout);
//                    double bottomOverflow = system.getBottomOverflow();
//                    double spacing = nextIsSystem ? style.getSystemSpacing() : 0.0;
//
//                    system.setSpaceBelow(spacing);
//                    currentY += system.getTopOverflow();
//                    system.setY(currentY);
//                    double effectiveSpacing = (bottomOverflow > 0) ? Math.min(spacing, 10.0) : spacing;
//                    currentY += (system.getNominalHeight() + bottomOverflow + effectiveSpacing);
//                } else {
//                    block.setY(currentY);
//                    currentY += block.getHeight();
//                }
//            }
//        }
//    }
//
//    private List<LyricLayout> createSingleLyricLine(NoteLayout noteLayout, int verseNumber) {
//        List<LyricLayout> result = new ArrayList<>();
//        if (noteLayout == null || noteLayout.getNote() == null) return result;
//
//        Note note = noteLayout.getNote();
//        Lyric lyric = note.getLyric(verseNumber);
//
//        if (lyric != null) {
//            boolean hasText = lyric.getText() != null && !lyric.getText().trim().isEmpty();
//            if (hasText || lyric.isConnected()) {
//                result.add(new LyricLayout(lyric, noteLayout));
//            }
//        }
//
//        return result;
//    }
//}

package org.example.musicscorebuilder.components.layout.engine;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.util.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.music.frames.Frame;

import java.util.*;

public class LayoutEngine {
    private ScoreStyle style;
    private LayoutContext layoutContext;

    private final Map<Measure, MeasureLayout> measureCache = new IdentityHashMap<>();
    private final SystemJustifier systemJustifier;
    private final TieBuilder tieBuilder;
    private final SlurBuilder slurBuilder;
    private final VoltaBuilder voltaBuilder;
    private final FrameBuilder frameBuilder;
    private final CourtesyLayoutHandler courtesyLayoutHandler;
    private final LayoutLinker layoutLinker;
    private final JumpMarkBuilder jumpMarkBuilder;
    private final TempoBuilder tempoBuilder;

    private final VerticalLayoutCalculator verticalCalculator;
    private final MeasureLayoutBuilder measureLayoutBuilder;
    private final MeasureCacheManager cacheManager;
    private final PagePaginator paginator;

    private final Map<Integer, List<Frame>> framesByMeasureIndex = new HashMap<>();
    private final List<Frame> beforeFramesBuf = new ArrayList<>();
    private final List<Frame> afterFramesBuf = new ArrayList<>();

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

        this.verticalCalculator = new VerticalLayoutCalculator();
        this.measureLayoutBuilder = new MeasureLayoutBuilder();
        this.cacheManager = new MeasureCacheManager(measureCache, measureLayoutBuilder);
        this.paginator = new PagePaginator(systemJustifier, frameBuilder, verticalCalculator);
    }

    public ScoreLayout compute(ScoreMode scoreMode, LayoutContext layoutContext) {
        long tStart = System.nanoTime();

        this.style = scoreMode.getStyle();
        this.layoutContext = layoutContext;

        cacheManager.invalidateCacheIfNeeded(scoreMode);

        slurBuilder.clearCache();
        tieBuilder.clearCache();
        frameBuilder.clearCache();

        ScoreLayout scoreLayout = new ScoreLayout(scoreMode.getScore(), style);
        PageLayout currentPage = paginator.createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);
        BraceType systemBraceType = scoreMode.getBraceType();
        SystemLayout currentSystem = paginator.addNewSystemToPage(currentPage, systemBraceType, style);

        populateFramesByMeasure(scoreMode.getFrames());

        long tPrep = System.nanoTime();

        List<Measure> measures = scoreMode.getMeasures();
        int measuresCount = measures.size();

        for (int i = 0; i < measuresCount; i++) {
            Measure measure = measures.get(i);


            partitionFramesForMeasure(i);

            int beforeCount = beforeFramesBuf.size();
            for (int f = 0; f < beforeCount; f++) {
                Frame frameData = beforeFramesBuf.get(f);
                currentSystem = paginator.insertFrameBlock(frameData, currentSystem, scoreLayout, systemBraceType, style);
                currentPage = currentSystem.getPageLayout();
            }

            MeasureLayout measureLayout = cacheManager.getOrCreateMeasureLayout(measure, currentSystem, style, layoutContext, tieBuilder, slurBuilder);




            double courtesyPadding = courtesyLayoutHandler.calculateCourtesyPadding(measure, measureLayout);

            boolean forcedBreak = false;
            List<MeasureLayout> systemMeasures = currentSystem.getMeasures();
            if (!systemMeasures.isEmpty()) {
                Measure previousMeasure = systemMeasures.get(systemMeasures.size() - 1).getMeasure();
                forcedBreak = previousMeasure.hasSystemBreak();
            }

//            long t = System.nanoTime();
            boolean needsNewSystem = forcedBreak || !paginator.canFitMeasureInSystem(currentPage, currentSystem, measureLayout, courtesyPadding);
//            System.out.println(
//                    "needsNewSystem check="
//                            + ((System.nanoTime() - t) / 1e6)
//            );
            if (needsNewSystem) {
                courtesyLayoutHandler.addCourtesyAttributesToLastMeasure(currentSystem, measure);
//                t = System.nanoTime();
                currentSystem = paginator.finalizeSystemAndCreateNext(currentSystem, scoreLayout, systemBraceType, measureLayout, style);
//                System.out.println(
//                        "finalizeSystemAndCreateNext="
//                                + ((System.nanoTime() - t) / 1e6)
//                );
                currentPage = currentSystem.getPageLayout();
            }

            if (currentSystem.getMeasures().isEmpty()) {
                Barline startBarline = scoreMode.getStartBarline();
                measureLayoutBuilder.add1stMeasureAttributes(startBarline, measureLayout, scoreLayout);
            }

            double startX = currentSystem.getMeasures().isEmpty() ? currentSystem.getBraceWidth() : currentSystem.getWidth();
            measureLayout.setX(startX);
            currentSystem.add(measureLayout);

            int afterCount = afterFramesBuf.size();
            for (int f = 0; f < afterCount; f++) {
                Frame frameData = afterFramesBuf.get(f);
                currentSystem = paginator.insertFrameBlock(frameData, currentSystem, scoreLayout, systemBraceType, style);
                currentPage = currentSystem.getPageLayout();
            }


//            System.out.printf(
//                    "Measure %d -> %.2f ms%n",
//                    i,
//                    (System.nanoTime() - t) / 1e6
//            );
        }

        long tMeasuresLoop = System.nanoTime();

        if (!currentSystem.getMeasures().isEmpty()) {
            verticalCalculator.applyStaffVerticalLayout(currentSystem, style);
            systemJustifier.justify(currentSystem);
        }

        long tFinalSystem = System.nanoTime();

        postProcessLayout(scoreMode, scoreLayout);

        long tEnd = System.nanoTime();

        double prepMs = (tPrep - tStart) / 1e6;
        double loopMs = (tMeasuresLoop - tPrep) / 1e6;
        double finalSystemMs = (tFinalSystem - tMeasuresLoop) / 1e6;
        double postProcessMs = (tEnd - tFinalSystem) / 1e6;
        double totalMs = (tEnd - tStart) / 1e6;

        System.out.println("=== PERF BENCHMARK LOG ===");
        System.out.printf("1. Prep & Invalidate Cache:  %8.2f ms%n", prepMs);
        System.out.printf("2. Measures Loop (%d msrs):  %8.2f ms%n", measuresCount, loopMs);
        System.out.printf("3. Final System Justify:     %8.2f ms%n", finalSystemMs);
        System.out.printf("4. postProcessLayout:        %8.2f ms%n", postProcessMs);
        System.out.println("----------------------------------------");
        System.out.printf("TOTAL COMPUTE TIME:          %8.2f ms%n", totalMs);
        System.out.println("========================================");

        return scoreLayout;
    }

    private void populateFramesByMeasure(List<Frame> scoreFrames) {
        for (List<Frame> list : framesByMeasureIndex.values()) {
            list.clear();
        }

        int frameCount = scoreFrames.size();
        for (int i = 0; i < frameCount; i++) {
            Frame frame = scoreFrames.get(i);
            int measureIdx = frame.getMeasureIndex();

            List<Frame> list = framesByMeasureIndex.get(measureIdx);
            if (list == null) {
                list = new ArrayList<>();
                framesByMeasureIndex.put(measureIdx, list);
            }
            list.add(frame);
        }
    }

    private void partitionFramesForMeasure(int measureIndex) {
        beforeFramesBuf.clear();
        afterFramesBuf.clear();

        List<Frame> frames = framesByMeasureIndex.get(measureIndex);
        if (frames != null && !frames.isEmpty()) {
            int size = frames.size();
            for (int f = 0; f < size; f++) {
                Frame frameData = frames.get(f);
                if (frameData.isBeforeMeasure()) {
                    beforeFramesBuf.add(frameData);
                } else {
                    afterFramesBuf.add(frameData);
                }
            }
        }
    }

    private void postProcessLayout(ScoreMode scoreMode, ScoreLayout scoreLayout) {
        List<PageLayout> pages = scoreLayout.getPages();
        layoutLinker.linkAllSegments(pages);
        layoutLinker.linkVoiceElements(pages);
        frameBuilder.updateTextFrames(scoreMode.getVerses(), scoreMode.getFrames(), pages);

        verticalCalculator.applySystemsVerticalLayout(scoreLayout, style);
        verticalCalculator.applyPagesVerticalLayout(scoreLayout, style);

        tieBuilder.buildTies(pages);
        slurBuilder.buildSlurs(scoreMode.getSlurs(), pages);
        voltaBuilder.buildVoltas(scoreMode.getVoltas(), scoreLayout);
        jumpMarkBuilder.buildJumpMarks(scoreMode.getJumpMarks(), scoreLayout);
        tempoBuilder.buildTempos(scoreMode.getTempos());
    }
}