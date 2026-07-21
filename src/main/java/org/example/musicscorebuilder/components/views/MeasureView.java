package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.ScoreStateManager;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.util.Util;

public class MeasureView extends ComponentView {
    private final SegmentView segmentView = new SegmentView();
    private final StaffView staffView = new StaffView();
    private final MeasureStaffSelectionView selectionView = new MeasureStaffSelectionView();

    public void draw(GraphicsContext gc, MeasureLayout measure, double systemX, double systemY, double sp) {
        double measureX = measure.getX() * sp + systemX;
        double measureY = measure.getY() * sp + systemY;
        double widthPx = measure.getWidth() * sp;
        double heightPx = measure.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), measureX, measureY, widthPx, heightPx);

        for (StaffLayout staff : measure.getStaffs()) {
            staffView.draw(gc, staff, measureX, measureY, sp);
        }

        for (SegmentLayout segment : measure.getSegments()) {
            segmentView.draw(gc, segment, measureX, measureY, sp);
        }

        Selectable selectedItem = ScoreStateManager.getInstance().getSelectedItem();
        if (selectedItem instanceof MeasureStaffSelection selection) {
            if (selection.getMeasure().getMeasure().equals(measure.getMeasure())) {
                selectionView.draw(gc, selection, systemX, systemY, sp);
            }
        }
    }
}
