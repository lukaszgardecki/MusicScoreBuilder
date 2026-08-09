package org.example.musicscorebuilder.components.layout.engine;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.util.GroupBeamBuilder;
import org.example.musicscorebuilder.components.layout.util.SystemJustifier;
import org.example.musicscorebuilder.components.music.*;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class LayoutEngine {
    private final ScoreStyle style;
    private final SystemJustifier systemJustifier;
    private final Map<Measure, MeasureLayout> measureCache = new IdentityHashMap<>();

    public LayoutEngine() {
        this.style = new ScoreStyle();
        this.systemJustifier = new SystemJustifier(style);
    }

    public ScoreLayout compute(ScoreMode scoreMode) {
        invalidateCacheIfNeeded(scoreMode);

        ScoreLayout scoreLayout = new ScoreLayout(scoreMode.getScore(), style);
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);
        SystemLayout currentSystem = addNewSystemToPage(currentPage, scoreMode);

        for (Measure measure : scoreMode.getMeasures()) {
            MeasureLayout measureLayout = getOrCreateMeasureLayout(measure, currentSystem);

            double courtesyPadding = calculateCourtesyPadding(measure, measureLayout);
            boolean needsNewSystem = !canFitMeasureInSystem(currentPage, currentSystem, measureLayout, courtesyPadding);

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

        systemJustifier.justify(currentSystem);

        postProcessLayout(scoreMode, scoreLayout);
        return scoreLayout;
    }

    // ========================================================================
    // CACHE & SETUP
    // ========================================================================

    private void invalidateCacheIfNeeded(ScoreMode scoreMode) {
        if (scoreMode.getMeasures().stream().anyMatch(Measure::isDirty)) {
            measureCache.clear();
        }
    }

    private MeasureLayout getOrCreateMeasureLayout(Measure measure, SystemLayout currentSystem) {
        if (measureCache.containsKey(measure) && !measure.isDirty()) {
            MeasureLayout measureLayout = measureCache.get(measure);
            measureLayout.remove1stMeasureAttributes();
            measureLayout.resetLayoutState();
            measureLayout.setParent(currentSystem);
            return measureLayout;
        }

        MeasureLayout measureLayout = createMeasureLayout(measure, currentSystem);
        measureCache.put(measure, measureLayout);
        measure.setDirty(false);
        return measureLayout;
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
        pageLayout.setLastSystemSpaceBelow(style.getSystemSpacing());
        var newSystem = new SystemLayout(pageLayout, scoreMode.getBraceType());
        pageLayout.add(newSystem);
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

        if (measure.getTimeSignature() != null && nextMeasure.getTimeSignature() != null
                && !measure.getTimeSignature().equals(nextMeasure.getTimeSignature())) {
            SegmentLayout tempCourtesy = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
            tempCourtesy.addTimeSignature(nextMeasure.getTimeSignature());
            padding += tempCourtesy.getWidth();
        }

        if (measure.getKeySignature() != null && nextMeasure.getKeySignature() != null
                && !measure.getKeySignature().equals(nextMeasure.getKeySignature())) {
            SegmentLayout tempCourtesy = new SegmentLayout(SegmentType.KEY_SIG, measureLayout);
            tempCourtesy.addKeySignature(nextMeasure.getKeySignature());
            padding += tempCourtesy.getWidth();
        }

        return padding;
    }

    private void addCourtesyAttributesToLastMeasure(SystemLayout system, Measure nextMeasure) {
        if (system.getMeasures().isEmpty()) return;

        MeasureLayout lastMeasureLayout = system.getMeasures().getLast();
        Measure prevMeasure = lastMeasureLayout.getMeasure();

        if (nextMeasure.getKeySignature() != null && prevMeasure.getKeySignature() != null
                && !nextMeasure.getKeySignature().equals(prevMeasure.getKeySignature())) {
            SegmentLayout courtesyKeySig = new SegmentLayout(SegmentType.KEY_SIG, lastMeasureLayout);
            courtesyKeySig.addKeySignature(nextMeasure.getKeySignature());
            courtesyKeySig.setSystemGenerated(true);
            lastMeasureLayout.add(courtesyKeySig);
        }

        if (nextMeasure.getTimeSignature() != null && prevMeasure.getTimeSignature() != null
                && !nextMeasure.getTimeSignature().equals(prevMeasure.getTimeSignature())) {
            SegmentLayout courtesyTimeSig = new SegmentLayout(SegmentType.TIME_SIG, lastMeasureLayout);
            courtesyTimeSig.addTimeSignature(nextMeasure.getTimeSignature());
            courtesyTimeSig.setSystemGenerated(true);
            lastMeasureLayout.add(courtesyTimeSig);
        }
    }

    private void add1stMeasureAttributes(ScoreMode scoreMode, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().getFirst().getSystems().size() == 1;
        Measure measure = measureLayout.getMeasure();

        if (isFirstMeasure) {
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

        if (measure.getTimeSignature() != null && prevMeasure.getTimeSignature() != null
                && !measure.getTimeSignature().equals(prevMeasure.getTimeSignature())) {
            SegmentLayout segment = new SegmentLayout(SegmentType.TIME_SIG, measureLayout);
            segment.addTimeSignature(measure.getTimeSignature());
            measureLayout.add(segment);
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
        buildTies(scoreLayout);
        buildSlurs(scoreMode, scoreLayout);
    }

    private void linkAllSegments(ScoreLayout scoreLayout) {
        java.util.List<SegmentLayout> allSegments = new java.util.ArrayList<>();
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    allSegments.addAll(measure.getSegments());
                }
            }
        }
        for (int i = 0; i < allSegments.size(); i++) {
            SegmentLayout current = allSegments.get(i);
            current.setPrev(i > 0 ? allSegments.get(i - 1) : null);
            current.setNext(i < allSegments.size() - 1 ? allSegments.get(i + 1) : null);
        }
    }

    private void buildTies(ScoreLayout scoreLayout) {
        buildSpanners(
                scoreLayout,
                system -> system.getTies().clear(),
                Note::isTieStart,
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

        Map<Note, NoteLayout> noteToLayoutMap = new HashMap<>();
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof NoteLayout noteLayout) {
                                noteToLayoutMap.put(noteLayout.getNote(), noteLayout);
                            }
                        }
                    }
                }
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
            Predicate<Note> isStartPredicate,
            Function<NoteLayout, NoteLayout> endFinder,
            TriConsumer<SystemLayout, NoteLayout, NoteLayout> addSpannerToSystem
    ) {
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                clearAction.accept(system);
            }
        }

        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof NoteLayout startNote && isStartPredicate.test(startNote.getNote())) {
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
                    }
                }
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