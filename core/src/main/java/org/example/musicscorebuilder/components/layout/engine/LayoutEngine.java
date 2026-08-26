package org.example.musicscorebuilder.components.layout.engine;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.util.GroupBeamBuilder;
import org.example.musicscorebuilder.components.layout.util.SystemJustifier;
import org.example.musicscorebuilder.components.music.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class LayoutEngine {
    private ScoreStyle style;
    private final SystemJustifier systemJustifier;
    private final Map<Measure, MeasureLayout> measureCache = new IdentityHashMap<>();

    private final Map<Note, NoteLayout> noteToLayoutMap = new IdentityHashMap<>();
    private final List<NoteLayout> tieStartNotes = new ArrayList<>();

    public LayoutEngine() {
        this.systemJustifier = new SystemJustifier();
    }

    public ScoreLayout compute(ScoreMode scoreMode) {
        this.style = scoreMode.getStyle();
        invalidateCacheIfNeeded(scoreMode);

        noteToLayoutMap.clear();
        tieStartNotes.clear();

        ScoreLayout scoreLayout = new ScoreLayout(scoreMode.getScore(), style);
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);
        SystemLayout currentSystem = addNewSystemToPage(currentPage, scoreMode);

        Map<Integer, List<Frame>> framesByMeasureIndex = new HashMap<>();
        for (Frame frame : scoreMode.getFrames()) {
            framesByMeasureIndex.computeIfAbsent(frame.getMeasureIndex(), k -> new ArrayList<>()).add(frame);
        }

        List<Measure> measures = scoreMode.getMeasures();
        for (int i = 0; i < measures.size(); i++) {
            Measure measure = measures.get(i);

            if (framesByMeasureIndex.containsKey(i)) {
                for (Frame frameData : framesByMeasureIndex.get(i)) {
                    if (!currentSystem.getMeasures().isEmpty()) {
                        systemJustifier.justify(currentSystem);
                    } else {
                        currentPage.getBlocks().remove(currentSystem);
                    }

                    FrameLayout frameLayout = new FrameLayout(currentPage, style, frameData);

                    if (currentPage.getRemainingHeight() < frameLayout.getHeight()) {
                        currentPage = createPageLayout(scoreLayout);
                        scoreLayout.addPageLayout(currentPage);
                        frameLayout = new FrameLayout(currentPage, style, frameData);
                    }

                    currentPage.addBlock(frameLayout);
                    currentSystem = addNewSystemToPage(currentPage, scoreMode);
                }
            }

            MeasureLayout measureLayout = getOrCreateMeasureLayout(measure, currentSystem);
            double courtesyPadding = calculateCourtesyPadding(measure, measureLayout);

            boolean forcedBreak = false;
            if (!currentSystem.getMeasures().isEmpty()) {
                Measure previousMeasure = currentSystem.getMeasures().get(currentSystem.getMeasures().size() - 1).getMeasure();
                forcedBreak = previousMeasure.hasSystemBreak();
            }

            boolean needsNewSystem = forcedBreak || !canFitMeasureInSystem(currentPage, currentSystem, measureLayout, courtesyPadding);

            if (needsNewSystem) {
                addCourtesyAttributesToLastMeasure(currentSystem, measure);
                currentSystem = finalizeSystemAndCreateNext(currentSystem, scoreLayout, scoreMode, measureLayout);
                currentPage = currentSystem.getPageLayout();
            }

            if (currentSystem.getMeasures().isEmpty()) {
                add1stMeasureAttributes(scoreMode, measureLayout, scoreLayout);
            }

            double startX = currentSystem.getMeasures().isEmpty() ? currentSystem.getBraceWidth() : currentSystem.getWidth();
            measureLayout.setX(startX);
            currentSystem.add(measureLayout);
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
                    noteToLayoutMap.put(noteLayout.getNote(), noteLayout);
                    if (noteLayout.getNote().isTieStart()) {
                        tieStartNotes.add(noteLayout);
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

    private SystemLayout finalizeSystemAndCreateNext(SystemLayout currentSystem, ScoreLayout scoreLayout, ScoreMode scoreMode, MeasureLayout nextMeasureLayout) {
        systemJustifier.justify(currentSystem);

        PageLayout currentPage = currentSystem.getPageLayout();
        boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < currentSystem.getHeight() + style.getSystemSpacing();

        if (noSpaceForNextSystem) {
            currentPage = createPageLayout(scoreLayout);
            scoreLayout.addPageLayout(currentPage);
        }

        SystemLayout newSystem = addNewSystemToPage(currentPage, scoreMode);
        nextMeasureLayout.setX(newSystem.getWidth());
        nextMeasureLayout.setParent(newSystem);

        return newSystem;
    }

    private SystemLayout addNewSystemToPage(PageLayout pageLayout, ScoreMode scoreMode) {
        boolean previousIsSystem = !pageLayout.getBlocks().isEmpty()
                && pageLayout.getBlocks().get(pageLayout.getBlocks().size() - 1) instanceof SystemLayout;

        if (previousIsSystem) {
            pageLayout.setLastSystemSpaceBelow(style.getSystemSpacing());
        }

        var newSystem = new SystemLayout(pageLayout, scoreMode.getBraceType());
        pageLayout.addBlock(newSystem);
        return newSystem;
    }

    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
        return new PageLayout(scoreLayout, scoreLayout.getPages().size());
    }

    // ========================================================================
    // ATTRIBUTES & COURTESY
    // ========================================================================

    private double calculateCourtesyPadding(Measure measure, MeasureLayout measureLayout) {
        Measure nextMeasure = measure.getNext();
        if (nextMeasure == null) return 0.0;

        double padding = 0.0;

        boolean keyChange = measure.getKeySignature() != null && nextMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(nextMeasure.getKeySignature());

        TimeSignature currTS = measure.getTimeSignature();
        TimeSignature nextTS = nextMeasure.getTimeSignature();
        boolean timeChange = currTS != null && nextTS != null
                && nextTS.isVisible()
                && !nextTS.equals(currTS);

        if (keyChange || timeChange) {
            Barline rightBarline = measure.getRightBarline();
            if (rightBarline != null && rightBarline.getStyle() == BarlineStyle.SINGLE) {
                SegmentLayout currentBarlineSeg = measureLayout.getSegments().get(measureLayout.getSegments().size() - 1);

                Barline doubleBarline = new Barline(BarlineStyle.DOUBLE_LIGHT, measure);
                SegmentLayout tempDoubleBarlineSeg = new SegmentLayout(new Segment(SegmentType.BARLINE, measure), measureLayout);
                for (StaffLayout staff : measureLayout.getStaffs()) {
                    tempDoubleBarlineSeg.addByStaff(staff, new BarlineLayout(doubleBarline, staff, tempDoubleBarlineSeg));
                }

                padding += (tempDoubleBarlineSeg.getWidth() - currentBarlineSeg.getWidth());
            }
        }

        if (keyChange) {
            SegmentLayout tempCourtesy = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
            tempCourtesy.addKeySignature(nextMeasure.getKeySignature());
            padding += tempCourtesy.getWidth();
        }

        if (timeChange) {
            SegmentLayout tempCourtesy = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
            tempCourtesy.addTimeSignature(nextTS);
            padding += tempCourtesy.getWidth();
        }

        return padding;
    }

    private void addCourtesyAttributesToLastMeasure(SystemLayout system, Measure nextMeasure) {
        if (system.getMeasures().isEmpty()) return;

        MeasureLayout lastMeasureLayout = system.getMeasures().get(system.getMeasures().size() - 1);
        Measure prevMeasure = lastMeasureLayout.getMeasure();

        boolean keyChange = nextMeasure.getKeySignature() != null && prevMeasure.getKeySignature() != null
                && !nextMeasure.getKeySignature().equals(prevMeasure.getKeySignature());

        TimeSignature prevTS = prevMeasure.getTimeSignature();
        TimeSignature nextTS = nextMeasure.getTimeSignature();
        boolean timeChange = prevTS != null && nextTS != null
                && nextTS.isVisible()
                && !nextTS.equals(prevTS);

        if (keyChange || timeChange) {
            Barline rightBarline = prevMeasure.getRightBarline();
            if (rightBarline != null && rightBarline.getStyle() == BarlineStyle.SINGLE) {
                lastMeasureLayout.getSegments().remove(lastMeasureLayout.getSegments().size() - 1);

                Segment doubleBarlineSegment = new Segment(SegmentType.BARLINE, prevMeasure);
                SegmentLayout doubleBarlineSegLayout = new SegmentLayout(doubleBarlineSegment, lastMeasureLayout);
                doubleBarlineSegLayout.setSystemGenerated(true);

                Barline doubleBarline = new Barline(BarlineStyle.DOUBLE_LIGHT, prevMeasure);
                for (StaffLayout staff : lastMeasureLayout.getStaffs()) {
                    doubleBarlineSegLayout.addByStaff(staff, new BarlineLayout(doubleBarline, staff, doubleBarlineSegLayout));
                }

                lastMeasureLayout.add(doubleBarlineSegLayout);
            }
        }

        if (keyChange) {
            SegmentLayout courtesyKeySig = new SegmentLayout(SegmentType.KEY_SIG, lastMeasureLayout);
            courtesyKeySig.addKeySignature(nextMeasure.getKeySignature());
            courtesyKeySig.setSystemGenerated(true);
            lastMeasureLayout.add(courtesyKeySig);
        }

        if (timeChange) {
            SegmentLayout courtesyTimeSig = new SegmentLayout(SegmentType.TIME_SIG, lastMeasureLayout);
            courtesyTimeSig.addTimeSignature(nextMeasure.getTimeSignature());
            courtesyTimeSig.setSystemGenerated(true);
            lastMeasureLayout.add(courtesyTimeSig);
        }
    }

    private void add1stMeasureAttributes(ScoreMode scoreMode, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
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
        if (scoreMode.getStartBarline() != null) {
            measureLayout.addSystemStartBarline(scoreMode.getStartBarline());
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
                        segmentLayout.addByStaff(staff, noteLayout);

                        noteToLayoutMap.put(note, noteLayout);
                        if (note.isTieStart()) {
                            tieStartNotes.add(noteLayout);
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
    // POST-PROCESSING (Slurs, Ties, Segments)
    // ========================================================================

    private void postProcessLayout(ScoreMode scoreMode, ScoreLayout scoreLayout) {
        linkAllSegments(scoreLayout);
        linkVoiceElements(scoreLayout);
        buildTies(scoreLayout);
        buildSlurs(scoreMode, scoreLayout);
    }

    private void linkAllSegments(ScoreLayout scoreLayout) {
        SegmentLayout prev = null;
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout current : measure.getSegments()) {
                        current.setPrev(prev);
                        if (prev != null) {
                            prev.setNext(current);
                        }
                        prev = current;
                    }
                }
            }
        }
        if (prev != null) {
            prev.setNext(null);
        }
    }

    private void linkVoiceElements(ScoreLayout scoreLayout) {
        Map<Long, NoteRestLayout> lastElementMap = new HashMap<>();
        Map<Long, NoteLayout> lastNoteMap = new HashMap<>();

        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout el : segment.getElements()) {

                            if (el instanceof NoteRestLayout current) {
                                int staffIdx = (current.getStaff() != null) ? current.getStaff().getStaffIndex() : 0;
                                int voice = current.getVoice();
                                long key = (((long) staffIdx) << 32) | (voice & 0xFFFFFFFFL);

                                NoteRestLayout prevElement = lastElementMap.get(key);
                                if (prevElement != null) {
                                    prevElement.setNextInVoice(current);
                                    current.setPrevInVoice(prevElement);
                                }
                                lastElementMap.put(key, current);

                                if (current instanceof NoteLayout note) {
                                    NoteLayout prevNote = lastNoteMap.get(key);
                                    if (prevNote != null) {
                                        prevNote.setNextNoteInVoice(note);
                                        note.setPrevNoteInVoice(prevNote);
                                    }
                                    lastNoteMap.put(key, note);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private void buildTies(ScoreLayout scoreLayout) {
        buildSpanners(
                scoreLayout,
                system -> system.getTies().clear(),
                tieStartNotes,
                this::findNextNoteInVoice,
                (system, start, end) -> system.addTie(new TieLayout(system, start, end))
        );
    }

    private void buildSlurs(ScoreMode scoreMode, ScoreLayout scoreLayout) {
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                system.getSlurs().clear();
            }
        }

        for (Slur slur : scoreMode.getSlurs()) {
            NoteLayout startLayout = noteToLayoutMap.get(slur.getStartNote());
            NoteLayout endLayout = noteToLayoutMap.get(slur.getEndNote());

            if (startLayout == null || endLayout == null) continue;

            SystemLayout startSystem = startLayout.getSegment().getParent().getParent();
            SystemLayout endSystem = endLayout.getSegment().getParent().getParent();

            if (startSystem == endSystem) {
                startSystem.addSlur(new SlurLayout(startSystem, startLayout, endLayout));
            } else {
                startSystem.addSlur(new SlurLayout(startSystem, startLayout, null));
                endSystem.addSlur(new SlurLayout(endSystem, null, endLayout));
            }
        }
    }

    private void buildSpanners(
            ScoreLayout scoreLayout,
            Consumer<SystemLayout> clearAction,
            List<NoteLayout> startNotes,
            Function<NoteLayout, NoteLayout> endFinder,
            TriConsumer<SystemLayout, NoteLayout, NoteLayout> addSpannerToSystem
    ) {
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                clearAction.accept(system);
            }
        }

        for (NoteLayout startNote : startNotes) {
            NoteLayout endNote = endFinder.apply(startNote);
            if (endNote == null) continue;

            SystemLayout startSystem = startNote.getSegment().getParent().getParent();
            SystemLayout endSystem = endNote.getSegment().getParent().getParent();

            if (startSystem == endSystem) {
                addSpannerToSystem.accept(startSystem, startNote, endNote);
            } else {
                addSpannerToSystem.accept(startSystem, startNote, null);
                addSpannerToSystem.accept(endSystem, null, endNote);
            }
        }
    }

    private NoteLayout findNextNoteInVoice(NoteLayout startNote) {
        SegmentLayout current = startNote.getSegment().getNext();
        int staffIndex = startNote.getStaff().getStaffIndex();
        int voice = startNote.getVoice();

        while (current != null) {
            for (ElementLayout el : current.getElements()) {
                if (el.getStaff() != null && el.getStaff().getStaffIndex() == staffIndex && el.getVoice() == voice) {
                    if (el instanceof NoteLayout note) return note;
                    if (el instanceof RestLayout) return null;
                }
            }
            current = current.getNext();
        }
        return null;
    }

    @FunctionalInterface
    private interface TriConsumer<System, StartNote, EndNote> {
        void accept(System system, StartNote startNote, EndNote endNote);
    }
}