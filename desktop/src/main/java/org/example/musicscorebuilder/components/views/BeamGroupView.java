package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.BeamGroupLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;

import java.util.ArrayList;
import java.util.List;

public class BeamGroupView extends ComponentView {

    public void draw(GraphicsContext gc, BeamGroupLayout beamGroup, double measureX, double measureY, double sp) {
        if (beamGroup == null || beamGroup.isEmpty()) return;

        List<NoteLayout> notes = beamGroup.getNotes();
        if (notes == null || notes.isEmpty()) return;

        NoteLayout first = notes.getFirst();
        NoteLayout last = notes.getLast();
        if (first == null || last == null || first.getStem() == null || last.getStem() == null) return;

        ScoreStyle style = first.getScoreStyle();
        var stemWidth = style.getNoteStemWidth() * sp;
        var beamThickness = style.getNoteBeamThickness() * sp;
        var halfBeamThickness = 0.5 * beamThickness;
        var beamStep = beamThickness + style.getNoteBeamGap() * sp;

        boolean stemIsUp = first.getStem().isUp();
        int offsetDirection = (stemIsUp) ? 1 : -1;

        // Punkty bazowe głównej belki (poziom 0)
        double firstStemLocalX = (stemIsUp) ? first.getBoxWidth() - first.getStem().getWidth() : 0;
        double baseStartX = measureX + first.getParent().getX() * sp + first.getX() * sp + firstStemLocalX * sp;

        double lastStemLocalX = (stemIsUp) ? last.getBoxWidth() - last.getStem().getWidth() : 0;
        double baseEndX = measureX + last.getParent().getX() * sp + last.getX() * sp + lastStemLocalX * sp + stemWidth;

        double baseStartY = measureY + first.getParent().getY() * sp + first.getStem().getEndY() * sp;
        double baseEndY = measureY + last.getParent().getY() * sp + last.getStem().getEndY() * sp;

        int maxBeams = 0;
        for (NoteLayout nl : notes) {
            maxBeams = Math.max(maxBeams, nl.getNote().getType().getBeamCount());
        }

        gc.setFill(Color.web(style.getSelectColor(beamGroup)));

        for (int level = 0; level < maxBeams; level++) {
            List<List<NoteLayout>> subGroups = findSubGroupsForLevel(notes, level);

            for (List<NoteLayout> subGroup : subGroups) {
                if (subGroup.isEmpty()) continue;

                NoteLayout subFirst = subGroup.getFirst();
                NoteLayout subLast = subGroup.getLast();

                double subFirstStemX = (stemIsUp) ? subFirst.getBoxWidth() - subFirst.getStem().getWidth() : 0;
                double startX = measureX + subFirst.getParent().getX() * sp + subFirst.getX() * sp + subFirstStemX * sp;

                double subLastStemX = (stemIsUp) ? subLast.getBoxWidth() - subLast.getStem().getWidth() : 0;
                double endX = measureX + subLast.getParent().getX() * sp + subLast.getX() * sp + subLastStemX * sp + stemWidth;

                double levelOffsetY = level * beamStep * offsetDirection;

                // Dla 1 nuty w podgrupie (kikutek) skracamy odcinek X i wyznaczamy punkty Y z tej samej prostej
                if (subGroup.size() == 1) {
                    double stubLength = style.getNoteBeamStubLength() * sp;

                    if (subFirst == first) {
                        endX = startX + stubLength;
                    } else {
                        startX = endX - stubLength;
                    }
                }

                // Zachowujemy ten sam kąt dla każdego odcinka na danym poziomie
                double startY = interpolateY(baseStartX, baseStartY, baseEndX, baseEndY, startX) + levelOffsetY;
                double endY = interpolateY(baseStartX, baseStartY, baseEndX, baseEndY, endX) + levelOffsetY;

                // Poligon belki
                double[] xPoints = { startX, endX, endX, startX };
                double[] yPoints = {
                        startY - halfBeamThickness,
                        endY - halfBeamThickness,
                        endY + halfBeamThickness,
                        startY + halfBeamThickness
                };

                gc.fillPolygon(xPoints, yPoints, 4);
            }
        }
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