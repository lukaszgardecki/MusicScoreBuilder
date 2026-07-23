package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LayoutHitTester {

    public static Selectable findClickedElement(List<PageLayout> pages, double globalX, double globalY) {
        for (PageLayout page : pages) {
            double pageX = globalX - page.getX();
            double pageY = globalY;

            for (SystemLayout system : page.getSystems()) {
                double systemX = pageX - system.getX();
                double systemY = pageY - system.getY();

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

    public static List<Selectable> resolveSelection(Selectable clickedElement) {
        if (clickedElement == null) { return Collections.emptyList(); }
        List<Selectable> itemsToSelect = new ArrayList<>();

        if (clickedElement instanceof ElementLayout element) {
            if (clickedElement instanceof TimeSigLayout || clickedElement instanceof KeySigLayout) {
                itemsToSelect.addAll(element.getParent().getElements());
            } else {
                itemsToSelect.add(element);
            }
        } else if (clickedElement instanceof StemLayout stem) {
            itemsToSelect.add(stem);
        } else if (clickedElement instanceof BeamGroupLayout beam) {
            itemsToSelect.add(beam);
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
                        List<ElementLayout> staffElements = segment.getElementsForStaff(targetStaff);

                        for (ElementLayout element : staffElements) {
                            itemsToSelect.add(element);
                            if (element instanceof NoteLayout noteLayout) {
                                if (noteLayout.getStem() != null) {
                                    itemsToSelect.add(noteLayout.getStem());
                                }
                                if (noteLayout.getBeamSingle() != null) {
                                    itemsToSelect.add(noteLayout.getBeamSingle());
                                }
                            }
                        }
                    }
                }

                if (measure.getBeamGroups() != null) {
                    for (BeamGroupLayout beamGroup : measure.getBeamGroups()) {
                        if (!beamGroup.isEmpty()) {
                            StaffLayout groupStaff = beamGroup.getFirstNote().getStaffLayout();
                            if (groupStaff == targetStaff) {
                                itemsToSelect.add(beamGroup);
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
}