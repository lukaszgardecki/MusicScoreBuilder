package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.ArrayList;
import java.util.List;

public class BeamGroupLayout implements Selectable {
    private final List<NoteLayout> notes = new ArrayList<>();
    private boolean selected;

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return notes.isEmpty() ? 1 : notes.get(0).getVoice(); }
    @Override
    public boolean contains(double measureX, double measureY) {
        if (notes.isEmpty()) return false;

        NoteLayout first = getFirstNote();
        NoteLayout last = getLastNote();
        if (first == null || last == null || first.getStem() == null || last.getStem() == null) return false;

        ScoreStyle style = first.getScoreStyle();

        var stemWidth = style.getNoteStemWidth();
        var beamThickness = style.getNoteBeamThickness();
        var halfBeamThickness = 0.5 * beamThickness;
        var beamGap = style.getNoteBeamGap();
        var beamStep = beamThickness + beamGap;

        boolean stemIsUp = first.getStem().isUp();
        int offsetDirection = (stemIsUp) ? 1 : -1;

        double baseStartX = getStemX(first, stemIsUp);
        double baseEndX = getStemX(last, stemIsUp) + stemWidth;

        double baseStartY = getStemEndY(first);
        double baseEndY = getStemEndY(last);

        int maxBeams = 0;
        for (NoteLayout nl : notes) {
            maxBeams = Math.max(maxBeams, nl.getNote().getType().getBeamCount());
        }

        for (int level = 0; level < maxBeams; level++) {
            List<List<NoteLayout>> subGroups = findSubGroupsForLevel(notes, level);

            for (List<NoteLayout> subGroup : subGroups) {
                if (subGroup.isEmpty()) continue;

                NoteLayout subFirst = subGroup.get(0);
                NoteLayout subLast = subGroup.get(subGroup.size() - 1);

                double startX = getStemX(subFirst, stemIsUp);
                double endX = getStemX(subLast, stemIsUp) + stemWidth;

                double levelOffsetY = level * beamStep * offsetDirection;

                if (subGroup.size() == 1) {
                    double stubLength = style.getNoteBeamStubLength();

                    if (subFirst == first) {
                        endX = startX + stubLength;
                    } else {
                        startX = endX - stubLength;
                    }
                }

                double startY = interpolateY(baseStartX, baseStartY, baseEndX, baseEndY, startX) + levelOffsetY;
                double endY = interpolateY(baseStartX, baseStartY, baseEndX, baseEndY, endX) + levelOffsetY;

                if (measureX >= startX && measureX <= endX) {
                    double centerY = interpolateY(startX, startY, endX, endY, measureX);
                    if (measureY >= (centerY - halfBeamThickness) && measureY <= (centerY + halfBeamThickness)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public double getTopYForNote(NoteLayout note) {
        if (notes.isEmpty() || note == null) return note != null ? note.getBoxY() : 0.0;

        NoteLayout first = getFirstNote();
        NoteLayout last = getLastNote();
        if (first == null || last == null || first.getStem() == null || last.getStem() == null) {
            return note.getBoxY();
        }

        ScoreStyle style = first.getScoreStyle();
        double beamThickness = style.getNoteBeamThickness();
        double halfBeamThickness = 0.5 * beamThickness;
        double beamGap = style.getNoteBeamGap();
        double beamStep = beamThickness + beamGap;

        boolean stemIsUp = first.getStem().isUp();

        double baseStartX = getStemX(first, stemIsUp);
        double baseEndX = getStemX(last, stemIsUp);
        double baseStartY = getStemEndY(first);
        double baseEndY = getStemEndY(last);

        double noteStemX = getStemX(note, stemIsUp);
        double mainBeamY = interpolateY(baseStartX, baseStartY, baseEndX, baseEndY, noteStemX);

        int beamCount = Math.max(1, note.getNote().getType().getBeamCount());

        if (stemIsUp) {
            return mainBeamY - halfBeamThickness;
        } else {
            return mainBeamY - ((beamCount - 1) * beamStep) - halfBeamThickness;
        }
    }

    public double getBottomYForNote(NoteLayout note) {
        if (notes.isEmpty() || note == null) return note != null ? note.getBoxY() + note.getHeight() : 0.0;

        NoteLayout first = getFirstNote();
        NoteLayout last = getLastNote();
        if (first == null || last == null || first.getStem() == null || last.getStem() == null) {
            return note.getBoxY() + note.getHeight();
        }

        ScoreStyle style = first.getScoreStyle();
        double beamThickness = style.getNoteBeamThickness();
        double halfBeamThickness = 0.5 * beamThickness;
        double beamGap = style.getNoteBeamGap();
        double beamStep = beamThickness + beamGap;

        boolean stemIsUp = first.getStem().isUp();

        double baseStartX = getStemX(first, stemIsUp);
        double baseEndX = getStemX(last, stemIsUp);
        double baseStartY = getStemEndY(first);
        double baseEndY = getStemEndY(last);

        double noteStemX = getStemX(note, stemIsUp);
        double mainBeamY = interpolateY(baseStartX, baseStartY, baseEndX, baseEndY, noteStemX);

        int beamCount = Math.max(1, note.getNote().getType().getBeamCount());

        if (stemIsUp) {
            return mainBeamY + ((beamCount - 1) * beamStep) + halfBeamThickness;
        } else {
            return mainBeamY + halfBeamThickness;
        }
    }

    public double getTopY() {
        double minY = Double.MAX_VALUE;
        for (NoteLayout note : notes) {
            minY = Math.min(minY, getTopYForNote(note));
        }
        return minY == Double.MAX_VALUE ? 0.0 : minY;
    }

    public double getBottomY() {
        double maxY = -Double.MAX_VALUE;
        for (NoteLayout note : notes) {
            maxY = Math.max(maxY, getBottomYForNote(note));
        }
        return maxY == -Double.MAX_VALUE ? 0.0 : maxY;
    }

    @Override public SegmentLayout getSegment() { return notes.isEmpty() ? null : notes.get(0).getSegment(); }
    @Override public StaffLayout getStaff() { return notes.isEmpty() ? null : notes.get(0).getStaff(); }

    public void addNote(NoteLayout note) { notes.add(note); }

    public void clear() {
        for (NoteLayout note : notes) {
            note.setBeamGroup(null);
        }
    }

    public List<NoteLayout> getNotes() { return notes; }

    public NoteLayout getFirstNote() {
        if (notes.isEmpty()) return null;
        return notes.get(0);
    }

    public NoteLayout getLastNote() {
        if (notes.isEmpty()) return null;
        return notes.get(notes.size() - 1);
    }

    public int size() { return notes.size(); }
    public boolean isEmpty() { return notes.isEmpty(); }

    private double getStemX(NoteLayout nl, boolean stemIsUp) {
        double stemLocalX = (stemIsUp) ? nl.getBoxWidth() - nl.getStem().getWidth() : 0;
        double parentX = nl.getParent() != null ? nl.getParent().getX() : 0.0;
        return parentX + nl.getX() + stemLocalX;
    }

    private double getStemEndY(NoteLayout nl) {
        double parentY = nl.getParent() != null ? nl.getParent().getY() : 0.0;
        return parentY + (nl.getStem() != null ? nl.getStem().getEndY() : 0.0);
    }

    private List<List<NoteLayout>> findSubGroupsForLevel(List<NoteLayout> notes, int level) {
        List<List<NoteLayout>> result = new ArrayList<>();
        List<NoteLayout> currentSubGroup = new ArrayList<>();

        for (NoteLayout nl : notes) {
            if (nl.getNote().getType().getBeamCount() > level) {
                currentSubGroup.add(nl);
            } else {
                if (!currentSubGroup.isEmpty()) {
                    result.add(new ArrayList<>(currentSubGroup));
                    currentSubGroup.clear();
                }
            }
        }
        if (!currentSubGroup.isEmpty()) {
            result.add(currentSubGroup);
        }
        return result;
    }

    private double interpolateY(double x1, double y1, double x2, double y2, double targetX) {
        if (Math.abs(x2 - x1) < 0.0001) return y1;
        double t = (targetX - x1) / (x2 - x1);
        return y1 + t * (y2 - y1);
    }
}