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
        if (pages == null || pages.isEmpty()) return null;

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            double pageX = globalX - page.getX();
            double pageY = globalY - page.getY();

            if (pageX < -2.0 || pageX > page.getWidth() + 2.0 || pageY < -2.0 || pageY > page.getHeight() + 2.0) {
                continue;
            }

            FrameLayout header = page.getHeader();
            if (header != null && header.contains(pageX, pageY)) {
                return header;
            }

            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                SystemLayout system = systems.get(s);
                double systemX = pageX - system.getX();
                double systemY = pageY - system.getY();

                double sysMarginY = 5.0;
                if (systemX < -2.0 || systemX > system.getWidth() + 2.0 ||
                        systemY < -sysMarginY || systemY > system.getHeight() + sysMarginY) {
                    continue;
                }

                List<TieLayout> ties = system.getTies();
                for (int t = 0; t < ties.size(); t++) {
                    TieLayout tie = ties.get(t);
                    if (tie.contains(systemX, systemY)) {
                        return tie;
                    }
                }

                List<SlurLayout> slurs = system.getSlurs();
                for (int sl = 0; sl < slurs.size(); sl++) {
                    SlurLayout slur = slurs.get(sl);
                    if (slur.contains(systemX, systemY)) {
                        return slur;
                    }
                }

                List<MeasureLayout> measures = system.getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout measure = measures.get(m);
                    double measureX = systemX - measure.getX();
                    double measureY = systemY - measure.getY();

                    if (measureX < -1.0 || measureX > measure.getWidth() + 1.0) {
                        continue;
                    }

                    if (!measure.getBeamGroups().isEmpty()) {
                        List<BeamGroupLayout> beamGroups = measure.getBeamGroups();
                        for (int bg = 0; bg < beamGroups.size(); bg++) {
                            BeamGroupLayout beamGroup = beamGroups.get(bg);
                            if (beamGroup.contains(measureX, measureY)) {
                                return beamGroup;
                            }
                        }
                    }

                    List<SegmentLayout> segments = measure.getSegments();
                    for (int sg = 0; sg < segments.size(); sg++) {
                        SegmentLayout segment = segments.get(sg);
                        double segmentMusicX = measureX - segment.getX();
                        double segmentMusicY = measureY - segment.getY();

                        List<ElementLayout> elements = segment.getElements();
                        for (int el = 0; el < elements.size(); el++) {
                            ElementLayout element = elements.get(el);

                            if (element instanceof NoteLayout noteLayout) {
                                if (noteLayout.getBeamSingle() != null && noteLayout.getBeamSingle().contains(segmentMusicX, segmentMusicY)) {
                                    return noteLayout.getBeamSingle();
                                }

                                if (noteLayout.getStem() != null && noteLayout.getStem().contains(segmentMusicX, segmentMusicY)) {
                                    return noteLayout.getStem();
                                }
                                if (!noteLayout.getDots().isEmpty()) {
                                    List<DotLayout> dots = noteLayout.getDots();
                                    for (int d = 0; d < dots.size(); d++) {
                                        if (dots.get(d).contains(segmentMusicX, segmentMusicY)) return dots.get(d);
                                    }
                                }
                                if (noteLayout.getAccidental() != null) {
                                    var acc = noteLayout.getAccidental();
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
        if (pages == null || pages.isEmpty()) return null;
        SegmentStaffAndY xMatchedFallback = null;

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            double pageX = globalX - page.getX();
            double pageY = globalY - page.getY();

            if (pageX < 0 || pageX > page.getWidth() || pageY < 0 || pageY > page.getHeight()) {
                continue;
            }

            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                SystemLayout system = systems.get(s);
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

                List<MeasureLayout> measures = system.getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout measure = measures.get(m);
                    double measureX = systemX - measure.getX();
                    double measureY = systemY - measure.getY();

                    if (measureX < 0 || measureX > measure.getWidth()) {
                        continue;
                    }

                    StaffLayout targetStaff = null;
                    List<StaffLayout> staffs = measure.getStaffs();
                    for (int st = 0; st < staffs.size(); st++) {
                        StaffLayout staff = staffs.get(st);
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

                    List<SegmentLayout> segments = measure.getSegments();
                    for (int sg = 0; sg < segments.size(); sg++) {
                        SegmentLayout segment = segments.get(sg);
                        double segX = segment.getX();
                        double segWidth = segment.getWidth();

                        if (measureX >= segX && measureX <= segX + segWidth) {
                            if (segment.getType() != SegmentType.NOTEREST) {
                                return null;
                            }

                            StaffLayout fallbackStaff = targetStaff != null ? targetStaff : (staffs.isEmpty() ? null : staffs.get(0));
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
                    List<ElementLayout> children = element.getParent().getElements();
                    for (int i = 0; i < children.size(); i++) {
                        ElementLayout child = children.get(i);
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

                        for (int e = 0; e < staffElements.size(); e++) {
                            ElementLayout element = staffElements.get(e);
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
                    List<BeamGroupLayout> beamGroups = measure.getBeamGroups();
                    for (int bg = 0; bg < beamGroups.size(); bg++) {
                        BeamGroupLayout beamGroup = beamGroups.get(bg);
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
                    List<TieLayout> ties = system.getTies();
                    for (int t = 0; t < ties.size(); t++) {
                        TieLayout tie = ties.get(t);
                        if (tie.getStartNote() != null && tie.getEndNote() != null) {
                            if (itemsToSelect.contains(tie.getStartNote()) && itemsToSelect.contains(tie.getEndNote())) {
                                itemsToSelect.add(tie);
                            }
                        }
                    }
                }
                if (system != null && system.getSlurs() != null) {
                    List<SlurLayout> slurs = system.getSlurs();
                    for (int sl = 0; sl < slurs.size(); sl++) {
                        SlurLayout slur = slurs.get(sl);
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

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                List<MeasureLayout> measures = systems.get(s).getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout measureLayout = measures.get(m);
                    List<SegmentLayout> segments = measureLayout.getSegments();
                    for (int sg = 0; sg < segments.size(); sg++) {
                        SegmentLayout segLayout = segments.get(sg);
                        if (segLayout.getSegment() == targetSegment) {
                            List<StaffLayout> staffs = measureLayout.getStaffs();
                            for (int st = 0; st < staffs.size(); st++) {
                                StaffLayout staffLayout = staffs.get(st);
                                if (staffLayout.getStaffIndex() == staffId) {
                                    List<ElementLayout> elements = segLayout.getElementsByStaff(staffLayout);
                                    if (elements != null) {
                                        for (int el = 0; el < elements.size(); el++) {
                                            ElementLayout element = elements.get(el);
                                            if (element instanceof NoteLayout nl && nl.getNote() == targetNre) {
                                                return nl;
                                            } else if (element instanceof RestLayout rl && rl.getRest() == targetNre) {
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

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                List<MeasureLayout> measures = systems.get(s).getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout measureLayout = measures.get(m);
                    List<SegmentLayout> segments = measureLayout.getSegments();
                    for (int sg = 0; sg < segments.size(); sg++) {
                        SegmentLayout segLayout = segments.get(sg);
                        if (segLayout.getSegment() == targetSegment) {
                            List<StaffLayout> staffs = measureLayout.getStaffs();
                            for (int st = 0; st < staffs.size(); st++) {
                                StaffLayout staffLayout = staffs.get(st);
                                if (staffLayout.getStaffIndex() == staffId) {
                                    List<ElementLayout> elements = segLayout.getElementsByStaff(staffLayout);
                                    if (elements != null && !elements.isEmpty()) {
                                        for (int el = 0; el < elements.size(); el++) {
                                            ElementLayout element = elements.get(el);
                                            if (element instanceof NoteLayout nl && nl.getNote().getVoice() == voice) {
                                                return nl;
                                            } else if (element instanceof RestLayout rl && rl.getRest().getVoice() == voice) {
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

    public static MeasureStaffSelection findSelectionInScoreLayout(ScoreLayout scoreLayout, Measure targetMeasure, int staffIndex) {
        if (scoreLayout == null || targetMeasure == null) return null;

        List<PageLayout> pages = scoreLayout.getPages();
        if (pages == null) return null;

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            if (page.getSystems() == null) continue;
            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                SystemLayout system = systems.get(s);
                if (system.getMeasures() == null) continue;
                List<MeasureLayout> measures = system.getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout ml = measures.get(m);
                    if (ml.getMeasure() == targetMeasure) {
                        StaffLayout targetStaff = null;
                        List<StaffLayout> staffs = ml.getStaffs();
                        if (staffs != null && !staffs.isEmpty()) {
                            for (int st = 0; st < staffs.size(); st++) {
                                StaffLayout sLayout = staffs.get(st);
                                if (sLayout.getStaffIndex() == staffIndex) {
                                    targetStaff = sLayout;
                                    break;
                                }
                            }
                            if (targetStaff == null) {
                                targetStaff = staffs.getFirst();
                            }
                        }

                        if (targetStaff != null) {
                            return new MeasureStaffSelection(ml, targetStaff);
                        }
                    }
                }
            }
        }
        return null;
    }

    public record LyricHit(NoteLayout noteLayout, int verse) implements Selectable {
        @Override public boolean isSelected() { return false; }
        @Override public void setSelected(boolean selected) {}
        @Override public int getVoice() { return 0; }
        @Override public boolean contains(double x, double y) { return false; }
        @Override public SegmentLayout getSegment() { return null; }
        @Override public StaffLayout getStaff() { return null; }
    }

    public record PositionedNote(NoteLayout noteLayout, double segmentX, double segmentY) {}
    public record Point(double x, double y) {}

    public static LyricHit findClickedLyric(List<PageLayout> pages, double x, double y) {
        if (pages == null) return null;

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            double pageX = page.getX();
            double pageY = page.getY();

            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                SystemLayout system = systems.get(s);
                double systemX = pageX + system.getX();
                double systemY = pageY + system.getY();

                List<MeasureLayout> measures = system.getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout measure = measures.get(m);
                    double measureX = systemX + measure.getX();
                    double measureY = systemY + measure.getY();

                    List<SegmentLayout> segments = measure.getSegments();
                    for (int sg = 0; sg < segments.size(); sg++) {
                        SegmentLayout segment = segments.get(sg);
                        double segX = measureX + segment.getX();
                        double segY = measureY + segment.getY();

                        List<ElementLayout> elements = segment.getElements();
                        for (int el = 0; el < elements.size(); el++) {
                            ElementLayout element = elements.get(el);
                            if (element instanceof NoteLayout noteLayout) {
                                var note = noteLayout.getNote();
                                if (note == null || note.getLyrics().isEmpty()) continue;

                                StaffLayout staff = noteLayout.getStaff();
                                if (staff == null) continue;

                                ScoreStyle style = noteLayout.getScoreStyle();
                                double fontSizeInSpatium = (style != null) ? style.getNoteLyricFontSize() : 1.3;

                                double absNoteCenterX = segX + (noteLayout.getX() + (noteLayout.getFontWidth() / 2.0));
                                double absStaffBottomY = segY + (staff.getY() + staff.getHeight());

                                List<Lyric> lyrics = note.getLyrics();
                                for (int l = 0; l < lyrics.size(); l++) {
                                    Lyric lyric = lyrics.get(l);
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
                        }
                    }
                }
            }
        }
        return null;
    }

    public static Point getLyricAbsolutePosition(ScoreLayout scoreLayout, NoteLayout targetNote, int verse) {
        if (scoreLayout == null || targetNote == null) return new Point(0, 0);

        List<PageLayout> pages = scoreLayout.getPages();
        if (pages == null) return new Point(0, 0);

        for (int p = 0; p < pages.size(); p++) {
            PageLayout page = pages.get(p);
            double pageX = page.getX();
            double pageY = page.getY();

            List<SystemLayout> systems = page.getSystems();
            for (int s = 0; s < systems.size(); s++) {
                SystemLayout system = systems.get(s);
                double systemX = pageX + system.getX();
                double systemY = pageY + system.getY();

                List<MeasureLayout> measures = system.getMeasures();
                for (int m = 0; m < measures.size(); m++) {
                    MeasureLayout measure = measures.get(m);
                    double measureX = systemX + measure.getX();
                    double measureY = systemY + measure.getY();

                    List<SegmentLayout> segments = measure.getSegments();
                    for (int sg = 0; sg < segments.size(); sg++) {
                        SegmentLayout segment = segments.get(sg);
                        double segX = measureX + segment.getX();
                        double segY = measureY + segment.getY();

                        List<ElementLayout> elements = segment.getElements();
                        for (int el = 0; el < elements.size(); el++) {
                            ElementLayout element = elements.get(el);
                            if (element == targetNote) {
                                double absX = segX + (targetNote.getX() + (targetNote.getFontWidth() / 2.0));
                                StaffLayout staff = targetNote.getStaff();

                                double staffBottomY = (staff != null)
                                        ? segY + staff.getY() + staff.getHeight()
                                        : segY + targetNote.getY();

                                double absY = staffBottomY + 2.5 + ((verse - 1) * 1.5);
                                return new Point(absX, absY);
                            }
                        }
                    }
                }
            }
        }
        return new Point(0, 0);
    }
}