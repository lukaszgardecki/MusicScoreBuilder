package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.JumpMark;
import org.example.musicscorebuilder.components.music.JumpType;
import org.example.musicscorebuilder.components.music.Measure;

import java.util.List;
import java.util.Map;

public class JumpMarkBuilder {
    private final Map<Measure, MeasureLayout> measureCache;
    private static final double MARGIN_ABOVE_STAFF = 1.0;
    private static final double STACK_GAP = 0.5;

    public JumpMarkBuilder(Map<Measure, MeasureLayout> measureCache) {
        this.measureCache = measureCache;
    }

    public void buildJumpMarks(List<JumpMark> jumpMarks, ScoreLayout scoreLayout) {
        for (PageLayout page : scoreLayout.getPages()) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measureLayout : system.getMeasures()) {
                    measureLayout.clearJumpMarks();
                }
            }
        }

        if (jumpMarks == null || jumpMarks.isEmpty()) return;

        for (JumpMark jumpMark : jumpMarks) {
            Measure measure = jumpMark.getMeasure();
            if (measure == null) continue;

            MeasureLayout measureLayout = findMeasureLayout(measure);
            if (measureLayout == null || measureLayout.getStaffs().isEmpty()) continue;

            StaffLayout firstStaff = measureLayout.getStaffs().get(0);
            JumpType type = jumpMark.getType();
            JumpType.Position position = type.getDefaultPosition();

            double targetY = calculateNextY(measureLayout, firstStaff.getY(), position);

            measureLayout.add(type.isSymbol()
                    ? new JumpSignLayout(jumpMark, measureLayout, firstStaff, targetY)
                    : new JumpTextLayout(jumpMark, measureLayout, firstStaff, targetY));
        }
    }

    private MeasureLayout findMeasureLayout(Measure measure) {
        MeasureLayout layout = measureCache.get(measure);
        if (layout != null) return layout;

        for (Map.Entry<Measure, MeasureLayout> entry : measureCache.entrySet()) {
            if (entry.getKey().equals(measure) || entry.getKey().getIndex() == measure.getIndex()) {
                return entry.getValue();
            }
        }
        return null;
    }

    private double calculateNextY(MeasureLayout measureLayout, double staffY, JumpType.Position position) {
        List<JumpMarkLayout> existingMarks = measureLayout.getJumpMarks();

        double topMostY = staffY;
        boolean foundInColumn = false;

        for (JumpMarkLayout existing : existingMarks) {
            if (existing.getPosition() == position) {
                double boxY = existing.getBoxY();
                if (!foundInColumn || boxY < topMostY) {
                    topMostY = boxY;
                    foundInColumn = true;
                }
            }
        }

        if (!foundInColumn) {
            return staffY - MARGIN_ABOVE_STAFF;
        }

        return topMostY - STACK_GAP;
    }
}