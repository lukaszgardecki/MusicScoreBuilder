package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public class SystemView extends ComponentView {
    private final MeasureView measureView = new MeasureView();
    private final MeasureStaffSelectionView selectionView = new MeasureStaffSelectionView();
    private final TieView tieView = new TieView();
    private final SlurView slurView = new SlurView();
    private final BraceView braceView = new BraceView();

    public void draw(GraphicsContext gc, SystemLayout system, double pageX, double pageY, double sp) {
        double systemX = system.getX() * sp + pageX;
        double systemY = system.getY() * sp + pageY;

        system.getBraceLayout().ifPresent(brace -> braceView.draw(gc, brace, systemX, systemY, sp));

        List<MeasureLayout> measures = system.getMeasures();
        int measureCount = measures.size();
        for (int i = 0; i < measureCount; i++) {
            measureView.draw(gc, measures.get(i), systemX, systemY, sp);
        }

        Selectable selectedItem = ScoreStateManager.getInstance().getSelectedItem();
        if (selectedItem instanceof MeasureStaffSelection selection) {
            if (selection.getMeasuresBySystem().containsKey(system)) {
                selectionView.draw(gc, selection, system, systemX, systemY, sp);
            }
        }

        List<TieLayout> ties = system.getTies();
        int tieCount = ties.size();
        for (int i = 0; i < tieCount; i++) {
            tieView.draw(gc, ties.get(i), systemX, systemY, sp);
        }

        List<SlurLayout> slurs = system.getSlurs();
        int slurCount = slurs.size();
        for (int i = 0; i < slurCount; i++) {
            slurView.draw(gc, slurs.get(i), systemX, systemY, sp);
        }
    }
}