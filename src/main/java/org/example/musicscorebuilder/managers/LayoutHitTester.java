package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayoutHitTester {

    public record SegmentStaffAndY(SegmentLayout segment, StaffLayout staff, double measureY) {}

    public static Selectable findClickedElement(List<PageLayout> pages, double globalX, double globalY) {
        for (PageLayout page : pages) {
            double pageX = globalX - page.getX();
            double pageY = globalY;

            FrameLayout header = page.getHeader();
            if (header != null && header.contains(pageX, pageY)) {
                return header;
            }

            for (SystemLayout system : page.getSystems()) {
                double systemX = pageX - system.getX();
                double systemY = pageY - system.getY();

                if (system.getTies() != null) {
                    for (TieLayout tie : system.getTies()) {
                        if (tie.contains(systemX, systemY)) {
                            return tie;
                        }
                    }
                }

                if (system.getSlurs() != null) {
                    for (SlurLayout slur : system.getSlurs()) {
                        if (slur.contains(systemX, systemY)) {
                            return slur;
                        }
                    }
                }

                for (MeasureLayout measure : system.getMeasures()) {
                    double measureX = systemX - measure.getX();
                    double measureY = systemY - measure.getY();

                    if (measure.getBeamGroups() != null && !measure.getBeamGroups().isEmpty()) {
                        for (BeamGroupLayout beamGroup : measure.getBeamGroups()) {
                            if (beamGroup.contains(measureX, measureY)) {
                                return beamGroup;
                            }
                        }
                    }

                    for (SegmentLayout segment : measure.getSegments()) {
                        double segmentMusicX = measureX - segment.getX();
                        double segmentMusicY = measureY - segment.getY();

                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof NoteLayout noteLayout) {
                                if (noteLayout.getBeamSingle() != null && noteLayout.getBeamSingle().contains(segmentMusicX, segmentMusicY)) {
                                    return noteLayout.getBeamSingle();
                                }

                                if (noteLayout.getStem() != null && noteLayout.getStem().contains(segmentMusicX, segmentMusicY)) {
                                    return noteLayout.getStem();
                                }
                                if (!noteLayout.getDots().isEmpty()) {
                                    for (DotLayout dot : noteLayout.getDots()) {
                                        if (dot.contains(segmentMusicX,  segmentMusicY)) return dot;
                                    }
                                }
                                if (noteLayout.getAccidental() != null) {
                                    var acc =  noteLayout.getAccidental();
                                    if (acc.contains(segmentMusicX, segmentMusicY)) return acc;
                                }
                            }

                            if (element.contains(segmentMusicX, segmentMusicY)) {
                                return element;
                            }
                        }
                    }

                    MeasureStaffSelection region = measure.getElementsRegionAt(measureX, measureY);
                    if (region != null && region.contains(measureX, measureY)) {
                        return region;
                    }
                }
            }
        }

        return null;
    }

    public static SegmentStaffAndY findSegmentAndStaffAt(List<PageLayout> pages, double globalX, double globalY) {
        SegmentStaffAndY xMatchedFallback = null;

        for (PageLayout page : pages) {
            double pageX = globalX - page.getX();
            double pageY = globalY;

            if (pageX < 0 || pageX > page.getWidth() || pageY < 0 || pageY > page.getHeight()) {
                continue;
            }

            for (SystemLayout system : page.getSystems()) {
                double systemX = pageX - system.getX();
                double systemY = pageY - system.getY();

                double verticalBuffer = 0.0;
                if (!system.getMeasures().isEmpty() && !system.getMeasures().get(0).getStaffs().isEmpty()) {
                    ScoreStyle style = system.getMeasures().get(0).getScoreStyle();
                    verticalBuffer = style.getNoteMaxLedgerLines() * style.getStaffLineSpacing();
                }

                if (systemX < 0 || systemX > system.getWidth() || systemY < -verticalBuffer || systemY > system.getHeight() + verticalBuffer) {
                    continue;
                }

                for (MeasureLayout measure : system.getMeasures()) {
                    double measureX = systemX - measure.getX();
                    double measureY = systemY - measure.getY();

                    if (measureX < 0 || measureX > measure.getWidth()) {
                        continue;
                    }

                    for (SegmentLayout segment : measure.getSegments()) {
                        double segX = segment.getX();
                        double segWidth = segment.getWidth();

                        if (measureX >= segX && measureX <= segX + segWidth) {
                            if (segment.getType() != SegmentType.NOTEREST) {
                                return null;
                            }
                        }
                    }

                    StaffLayout targetStaff = null;
                    for (StaffLayout staff : measure.getStaffs()) {
                        double staffY = staff.getY();
                        double spacing = measure.getScoreStyle().getStaffLineSpacing();
                        int ledgersLimit = measure.getScoreStyle().getNoteMaxLedgerLines();

                        double staffTop = staffY - (ledgersLimit * spacing);
                        double staffBottom = staffY + (4 * spacing) + (ledgersLimit * spacing);

                        if (measureY >= staffTop && measureY <= staffBottom) {
                            targetStaff = staff;
                            break;
                        }
                    }

                    for (SegmentLayout segment : measure.getSegments()) {
                        if (segment.getType() != SegmentType.NOTEREST) {
                            continue;
                        }

                        double segX = segment.getX();
                        double segWidth = segment.getWidth();

                        if (measureX >= segX && measureX <= segX + segWidth) {
                            StaffLayout fallbackStaff = targetStaff != null ? targetStaff : (measure.getStaffs().isEmpty() ? null : measure.getStaffs().get(0));

                            if (fallbackStaff != null) {
                                SegmentStaffAndY match = new SegmentStaffAndY(segment, fallbackStaff, measureY);

                                if (targetStaff != null) {
                                    return match;
                                } else {
                                    xMatchedFallback = match;
                                }
                            }
                        }
                    }
                }
            }
        }

        return xMatchedFallback;
    }

    public static List<Selectable> resolveSelection(Selectable clickedElement) {
        if (clickedElement == null) { return Collections.emptyList(); }
        List<Selectable> itemsToSelect = new ArrayList<>();

        if (clickedElement instanceof ElementLayout element) {
            if (clickedElement instanceof TimeSigLayout || clickedElement instanceof KeySigLayout) {
                if (element.getParent() != null && element.getParent().getElements() != null) {
                    for (ElementLayout child : element.getParent().getElements()) {
                        if (child.getClass() == element.getClass()) {
                            itemsToSelect.add(child);
                        }
                    }
                } else {
                    itemsToSelect.add(element);
                }
            } else {
                itemsToSelect.add(element);
            }
        } else if (clickedElement instanceof DotLayout dot) {
            itemsToSelect.add(dot);
        } else if (clickedElement instanceof AccidentalLayout accidental) {
            itemsToSelect.add(accidental);
        } else if (clickedElement instanceof StemLayout stem) {
            itemsToSelect.add(stem);
        } else if (clickedElement instanceof BeamGroupLayout beam) {
            itemsToSelect.add(beam);
        } else if (clickedElement instanceof TieLayout tie) {
            itemsToSelect.add(tie);
        } else if (clickedElement instanceof SlurLayout slur) {
            itemsToSelect.add(slur);
        } else if (clickedElement instanceof MeasureStaffSelection selection) {
            itemsToSelect.add(selection);

            MeasureLayout measure = selection.getMeasure();
            StaffLayout targetStaff = selection.getStaff();

            if (measure != null && measure.getSegments() != null && targetStaff != null) {
                List<SegmentLayout> segments = measure.getSegments();

                int firstChordRestIdx = -1;
                int lastChordRestIdx = -1;

                for (int i = 0; i < segments.size(); i++) {
                    if (segments.get(i).getType() == SegmentType.NOTEREST) {
                        if (firstChordRestIdx == -1) {
                            firstChordRestIdx = i;
                        }
                        lastChordRestIdx = i;
                    }
                }

                if (firstChordRestIdx != -1) {
                    for (int i = firstChordRestIdx; i <= lastChordRestIdx; i++) {
                        SegmentLayout segment = segments.get(i);
                        List<ElementLayout> staffElements = segment.getElementsByStaff(targetStaff);

                        for (ElementLayout element : staffElements) {
                            itemsToSelect.add(element);
                            if (element instanceof NoteLayout noteLayout) {
                                if (noteLayout.getStem() != null) {
                                    itemsToSelect.add(noteLayout.getStem());
                                }
                                if (noteLayout.getBeamSingle() != null) {
                                    itemsToSelect.add(noteLayout.getBeamSingle());
                                }
                                if (!noteLayout.getDots().isEmpty()) {
                                    itemsToSelect.addAll(noteLayout.getDots());
                                }
                                if (noteLayout.getAccidental() != null) {
                                    itemsToSelect.add(noteLayout.getAccidental());
                                }
                            }
                        }
                    }
                }

                if (measure.getBeamGroups() != null) {
                    for (BeamGroupLayout beamGroup : measure.getBeamGroups()) {
                        if (!beamGroup.isEmpty()) {
                            StaffLayout groupStaff = beamGroup.getFirstNote().getStaff();
                            if (groupStaff == targetStaff) {
                                itemsToSelect.add(beamGroup);
                            }
                        }
                    }
                }

                SystemLayout system = measure.getParent();
                if (system != null && system.getTies() != null) {
                    for (TieLayout tie : system.getTies()) {
                        if (tie.getStartNote() != null && tie.getEndNote() != null) {
                            if (itemsToSelect.contains(tie.getStartNote()) && itemsToSelect.contains(tie.getEndNote())) {
                                itemsToSelect.add(tie);
                            }
                        }
                    }
                }
                if (system != null && system.getSlurs() != null) {
                    for (SlurLayout slur : system.getSlurs()) {
                        if (slur.getStartNote() != null && slur.getEndNote() != null) {
                            if (itemsToSelect.contains(slur.getStartNote()) && itemsToSelect.contains(slur.getEndNote())) {
                                itemsToSelect.add(slur);
                            }
                        }
                    }
                }
            }
        } else {
            itemsToSelect.add(clickedElement);
        }
        return itemsToSelect;
    }

    public static Selectable findSelectableForElement(List<PageLayout> pages, Segment targetSegment, int staffId, NoteRestElement targetNre) {
        if (pages == null || targetSegment == null || targetNre == null) return null;

        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measureLayout : system.getMeasures()) {
                    for (SegmentLayout segLayout : measureLayout.getSegments()) {
                        if (segLayout.getSegment() == targetSegment) {
                            for (StaffLayout staffLayout : measureLayout.getStaffs()) {
                                if (staffLayout.getStaffIndex() == staffId) {
                                    List<ElementLayout> elements = segLayout.getElementsByStaff(staffLayout);
                                    if (elements != null) {
                                        for (ElementLayout el : elements) {
                                            if (el instanceof NoteLayout nl && nl.getNote() == targetNre) {
                                                return nl;
                                            } else if (el instanceof RestLayout rl && rl.getRest() == targetNre) {
                                                return rl;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Selectable findSelectableForSegmentAndStaff(List<PageLayout> pages, Segment targetSegment, int staffId, int voice) {
        if (pages == null || targetSegment == null) return null;

        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measureLayout : system.getMeasures()) {
                    for (SegmentLayout segLayout : measureLayout.getSegments()) {
                        if (segLayout.getSegment() == targetSegment) {
                            for (StaffLayout staffLayout : measureLayout.getStaffs()) {
                                if (staffLayout.getStaffIndex() == staffId) {
                                    List<ElementLayout> elements = segLayout.getElementsByStaff(staffLayout);
                                    if (elements != null && !elements.isEmpty()) {
                                        for (ElementLayout el : elements) {
                                            if (el instanceof NoteLayout nl && nl.getNote().getVoice() == voice) {
                                                return nl;
                                            } else if (el instanceof RestLayout rl && rl.getRest().getVoice() == voice) {
                                                return rl;
                                            }
                                        }
                                        return elements.getFirst();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public record LyricHit(NoteLayout noteLayout, int verse) implements Selectable {

        @Override
        public boolean isSelected() {
            return false;
        }

        @Override
        public void setSelected(boolean selected) {

        }

        @Override
        public int getVoice() {
            return 0;
        }

        @Override
        public boolean contains(double x, double y) {
            return false;
        }

        @Override
        public SegmentLayout getSegment() {
            return null;
        }

        @Override
        public StaffLayout getStaff() {
            return null;
        }
    }

    public record PositionedNote(NoteLayout noteLayout, double segmentX, double segmentY) {}
    public record Point(double x,  double y) {}

    public static List<PositionedNote> getAllPositionedNotes(List<PageLayout> pages) {
        List<PositionedNote> result = new ArrayList<>();
        if (pages == null) return result;

        for (PageLayout page : pages) {
            double pageX = page.getX();
            double pageY = 0;

            for (SystemLayout system : page.getSystems()) {
                double systemX = pageX + system.getX();
                double systemY = pageY + system.getY();

                for (MeasureLayout measure : system.getMeasures()) {
                    double measureX = systemX + measure.getX();
                    double measureY = systemY + measure.getY();

                    for (SegmentLayout segment : measure.getSegments()) {
                        double segX = measureX + segment.getX();
                        double segY = measureY + segment.getY();

                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof NoteLayout note) {
                                result.add(new PositionedNote(note, segX, segY));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public static LyricHit findClickedLyric(List<PageLayout> pages, double x, double y) {
        for (PositionedNote pn : getAllPositionedNotes(pages)) {
            NoteLayout noteLayout = pn.noteLayout();
            var note = noteLayout.getNote();
            if (note == null || note.getLyrics().isEmpty()) continue;

            StaffLayout staff = noteLayout.getStaff();
            if (staff == null) continue;

            ScoreStyle style = noteLayout.getScoreStyle();
            double fontSizeInSpatium = (style != null) ? style.getNoteLyricFontSize() : 1.3;

            double absNoteCenterX = pn.segmentX() + (noteLayout.getX() + (noteLayout.getFontWidth() / 2.0));
            double absStaffBottomY = pn.segmentY() + (staff.getY() + staff.getHeight());

            for (Lyric lyric : note.getLyrics()) {
                if (lyric == null || lyric.getText() == null || lyric.getText().isBlank()) continue;

                int verse = lyric.getVerse();
                double lyricY = absStaffBottomY + 2.5 + ((verse - 1) * 1.5);
                double textWidth = FontManager.getTextWidth(FontManager.FontType.FREE_SERIF, lyric.getText(), fontSizeInSpatium);
                double textHeight = FontManager.getTextHeight(FontManager.FontType.FREE_SERIF, lyric.getText(), fontSizeInSpatium);

                double minX = absNoteCenterX - (textWidth / 2.0);
                double maxX = absNoteCenterX + (textWidth / 2.0);
                double minY = lyricY;
                double maxY = lyricY + textHeight;

                if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
                    return new LyricHit(noteLayout, verse);
                }
            }
        }
        return null;
    }

    public static Point getLyricAbsolutePosition(ScoreLayout scoreLayout, NoteLayout targetNote, int verse) {
        if (scoreLayout == null || targetNote == null) return new Point(0, 0);

        for (PositionedNote pn : getAllPositionedNotes(scoreLayout.getPages())) {
            if (pn.noteLayout() == targetNote) {

                double absX = pn.segmentX() + (targetNote.getX() + (targetNote.getFontWidth() / 2.0));
                StaffLayout staff = targetNote.getStaff();

                double staffBottomY = (staff != null)
                        ? pn.segmentY() + staff.getY() + staff.getHeight()
                        : pn.segmentY() + targetNote.getY();

                double absY = staffBottomY + 2.5 + ((verse - 1) * 1.5);
                return new Point(absX, absY);
            }
        }
        return new Point(0, 0);
    }
}