package org.example.musicscorebuilder.components.layout;

import javafx.scene.shape.Polygon;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.ArrayList;
import java.util.List;

public class BeamGroupLayout implements Selectable {
    private final List<NoteLayout> notes = new ArrayList<>();
    private boolean selected;

    @Override public boolean isSelected() { return selected; }
    @Override public void setSelected(boolean selected) { this.selected = selected; }
    @Override public int getVoice() { return notes.isEmpty() ? 1 : notes.getFirst().getVoice(); }
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

        double firstStemLocalX = (stemIsUp) ? first.getBoxWidth() - first.getStem().getWidth() : 0;
        double baseStartX = first.getParent().getX() + first.getX() + firstStemLocalX;

        double lastStemLocalX = (stemIsUp) ? last.getBoxWidth() - last.getStem().getWidth() : 0;
        double baseEndX = last.getParent().getX() + last.getX() + lastStemLocalX + stemWidth;

        double baseStartY = first.getParent().getY() + first.getStem().getEndY();
        double baseEndY = last.getParent().getY() + last.getStem().getEndY();

        int maxBeams = 0;
        for (NoteLayout nl : notes) {
            maxBeams = Math.max(maxBeams, nl.getNote().getType().getBeamCount());
        }

        for (int level = 0; level < maxBeams; level++) {
            List<List<NoteLayout>> subGroups = findSubGroupsForLevel(notes, level);

            for (List<NoteLayout> subGroup : subGroups) {
                if (subGroup.isEmpty()) continue;

                NoteLayout subFirst = subGroup.getFirst();
                NoteLayout subLast = subGroup.getLast();

                double subFirstStemX = (stemIsUp) ? subFirst.getBoxWidth() - subFirst.getStem().getWidth() : 0;
                double startX = subFirst.getParent().getX() + subFirst.getX() + subFirstStemX;

                double subLastStemX = (stemIsUp) ? subLast.getBoxWidth() - subLast.getStem().getWidth() : 0;
                double endX = subLast.getParent().getX() + subLast.getX() + subLastStemX + stemWidth;

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

                Polygon poly = new Polygon(
                        startX, startY - halfBeamThickness,
                        endX, endY - halfBeamThickness,
                        endX, endY + halfBeamThickness,
                        startX, startY + halfBeamThickness
                );

                if (poly.contains(measureX, measureY)) return true;
            }
        }

        return false;
    }
    @Override public SegmentLayout getSegment() { return notes.getFirst().getSegment(); }
    @Override public StaffLayout getStaff() { return notes.isEmpty() ? null : notes.getFirst().getStaff(); }

    public void addNote(NoteLayout note) { notes.add(note); }

    public void clear() {
        for (NoteLayout note : notes) {
            note.setBeamGroup(null);
        }
    }

    public List<NoteLayout> getNotes() {
        return notes;
    }

    public NoteLayout getFirstNote() {
        if (notes.isEmpty()) return null;
        return notes.getFirst();
    }

    public NoteLayout getLastNote() {
        if (notes.isEmpty()) return null;
        return notes.getLast();
    }

    public int size() { return notes.size(); }
    public boolean isEmpty() { return notes.isEmpty(); }

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
