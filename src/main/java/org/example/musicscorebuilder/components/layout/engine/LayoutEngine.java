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
        ScoreLayout scoreLayout = new ScoreLayout(scoreMode.getScore(), style);
        PageLayout currentPage = createPageLayout(scoreLayout);
        scoreLayout.addPageLayout(currentPage);

        SystemLayout newSystem = addNewSystemToPage(currentPage, scoreMode);

        for (Measure measure : scoreMode.getMeasures()) {
            MeasureLayout measureLayout;
            if (measureCache.containsKey(measure) && !measure.isDirty()) {
                measureLayout = measureCache.get(measure);
                measureLayout.remove1stMeasureAttributes();
                measureLayout.resetLayoutState();
            } else {
                measureLayout = createMeasureLayout(measure, newSystem);
                measureCache.put(measure, measureLayout);
                measure.setDirty(false);
            }

            boolean noSpaceForNextMeasure = currentPage.getEffectiveWidth() - newSystem.getWidth() < measureLayout.getWidth();
            boolean noSpaceForNextSystem = currentPage.getRemainingHeight() < newSystem.getHeight() + style.getSystemSpacing();

            if (noSpaceForNextMeasure) {
                systemJustifier.justify(newSystem);

                if (noSpaceForNextSystem) {
                    currentPage = createPageLayout(scoreLayout);
                    scoreLayout.addPageLayout(currentPage);
                }
                newSystem = addNewSystemToPage(currentPage, scoreMode);
                measureLayout.setX(newSystem.getWidth());
            }

            if (newSystem.getMeasures().isEmpty()) {
                add1stMeasureAttributes(scoreMode, measureLayout, scoreLayout);
            }

            double startX = newSystem.getMeasures().isEmpty() ? newSystem.getBraceWidth() : newSystem.getWidth();
            measureLayout.setX(startX);
            newSystem.add(measureLayout);
        }

        systemJustifier.justify(newSystem);

        linkAllSegments(scoreLayout);
        buildTies(scoreLayout);
        buildSlurs(scoreMode, scoreLayout);
        return scoreLayout;
    }

    private SystemLayout addNewSystemToPage(PageLayout pageLayout, ScoreMode scoreMode) {
        pageLayout.setLastSystemSpaceBelow(style.getSystemSpacing());
        var newSystem = new SystemLayout(pageLayout, scoreMode.getBraceType());
        pageLayout.add(newSystem);
        return newSystem;
    }

    private PageLayout createPageLayout(ScoreLayout scoreLayout) {
        int pageIndex = scoreLayout.getPages().size();
        return new PageLayout(scoreLayout, pageIndex);
    }

    private MeasureLayout createMeasureLayout(Measure measure, SystemLayout systemLayout) {
        MeasureLayout measureLayout = new MeasureLayout(measure, systemLayout, style);
        GroupBeamBuilder groupBeamBuilder = new GroupBeamBuilder();

        for (Staff staff : measure.getStaves()) {
            measureLayout.add(new StaffLayout(staff, measureLayout, style));
        }

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

        Segment endBarlineSegment = new Segment(SegmentType.BARLINE, measure);
        SegmentLayout endBarlineSegLayout = new SegmentLayout(endBarlineSegment, measureLayout);
        for (StaffLayout staffLayout : measureLayout.getStaffs()) {
            endBarlineSegment.addElement(staffLayout.getStaffIndex(), measure.getRightBarline());
            endBarlineSegLayout.addByStaff(staffLayout, new BarlineLayout(measure.getRightBarline(), staffLayout, endBarlineSegLayout));
        }
        measureLayout.add(endBarlineSegLayout);

        measureLayout.setBeamGroups(groupBeamBuilder.build());
        return measureLayout;
    }

    private void add1stMeasureAttributes(ScoreMode scoreMode, MeasureLayout measureLayout, ScoreLayout scoreLayout) {
        var isFirstMeasure = scoreLayout.getPages().size() == 1 && scoreLayout.getPages().get(0).getSystems().size() == 1;

        if (isFirstMeasure) {
            measureLayout.addSystemTimeSignature(measureLayout.getMeasure().getTimeSignature());
        }
        measureLayout.addSystemKeySignature(measureLayout.getMeasure().getKeySignature());
        measureLayout.addSystemClef();
        if (scoreMode.getStartBarline() == null) return;
        measureLayout.addSystemStartBarline(scoreMode.getStartBarline());
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
                // Łuk przełamany między systemami
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
                                    // Łącznik przecięty zmianą systemu/linii
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
                    if (el instanceof RestLayout) return null; // Pauza przerywa łuk
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