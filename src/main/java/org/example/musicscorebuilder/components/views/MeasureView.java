package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;
import java.util.List;

public class MeasureView extends ComponentView {
    private final SegmentView segmentView = new SegmentView();
    private final StaffView staffView = new StaffView();
    private final BeamGroupView beamsView = new BeamGroupView();
    private final MeasureStaffSelectionView selectionView = new MeasureStaffSelectionView();
    private final EditCursorView editCursorView = new EditCursorView();
    private final BreakSystemIconView breakSystemIcon = new BreakSystemIconView();

    public void draw(GraphicsContext gc, MeasureLayout measure, double systemX, double systemY, double sp) {
        double measureX = measure.getX() * sp + systemX;
        double measureY = measure.getY() * sp + systemY;
        double widthPx = measure.getWidth() * sp;

        List<StaffLayout> staffs = measure.getStaffs();
        int staffCount = staffs.size();
        for (int i = 0; i < staffCount; i++) {
            staffView.draw(gc, staffs.get(i), measureX, measureY, sp);
        }

        List<SegmentLayout> segments = measure.getSegments();
        int segmentCount = segments.size();
        for (int i = 0; i < segmentCount; i++) {
            SegmentLayout segment = segments.get(i);
            if (segment.hasActiveCursor()) {
                editCursorView.draw(gc, segment.getCursor(), measureX, measureY, sp);
            }
            segmentView.draw(gc, segment, measureX, measureY, sp);
        }

        List<BeamGroupLayout> beamGroups = measure.getBeamGroups();
        int beamCount = beamGroups.size();
        for (int i = 0; i < beamCount; i++) {
            beamsView.draw(gc, beamGroups.get(i), measureX, measureY, sp);
        }

        Selectable selectedItem = ScoreStateManager.getInstance().getSelectedItem();
        if (selectedItem instanceof MeasureStaffSelection selection) {
            if (measure.getMeasure() != null && measure.getMeasure().equals(selection.getMeasure().getMeasure())) {
                selectionView.draw(gc, selection, systemX, systemY, sp);
            }
        }

        if (measure.getMeasure() != null && measure.getMeasure().hasSystemBreak()) {
            breakSystemIcon.draw(gc, measureX, measureY, widthPx, measure.getScoreStyle(), sp);
        }
    }
}