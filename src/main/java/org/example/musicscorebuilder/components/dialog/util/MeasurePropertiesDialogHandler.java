package org.example.musicscorebuilder.components.dialog.util;

import javafx.application.Platform;
import org.example.musicscorebuilder.components.dialog.CustomMeasurePropertiesDialog;
import org.example.musicscorebuilder.components.layout.MeasureStaffSelection;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.music.Measure;
import org.example.musicscorebuilder.components.music.TimeSignature;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.function.Supplier;

public class MeasurePropertiesDialogHandler {

    public static void attach(
            CustomMeasurePropertiesDialog dialog,
            MeasureStaffSelection initialSelection,
            Supplier<ScoreLayout> scoreLayoutSupplier
    ) {
        if (dialog == null || initialSelection == null || initialSelection.getMeasure() == null) return;

        final int staffIndex = initialSelection.getStaff() != null ? initialSelection.getStaff().getStaffIndex() : 0;
        final Measure[] currentMeasureHolder = new Measure[]{ initialSelection.getMeasure().getMeasure() };

        updateDialogState(dialog, currentMeasureHolder[0]);

        dialog.setOnPrevious(() -> {
            Measure current = currentMeasureHolder[0];
            if (current != null && current.getPrev() != null) {
                navigateTo(dialog, scoreLayoutSupplier, currentMeasureHolder, current.getPrev(), staffIndex);
            }
        });
        dialog.setOnNext(() -> {
            Measure current = currentMeasureHolder[0];
            if (current != null && current.getNext() != null) {
                navigateTo(dialog, scoreLayoutSupplier, currentMeasureHolder, current.getNext(), staffIndex);
            }
        });
        dialog.setOnApply(result -> applyChanges(scoreLayoutSupplier, currentMeasureHolder, staffIndex, result));
        dialog.setConfirmButton("OK", result -> applyChanges(scoreLayoutSupplier, currentMeasureHolder, staffIndex, result));
        dialog.setCancelButton("Anuluj", null);
    }

    private static void navigateTo(
            CustomMeasurePropertiesDialog dialog,
            Supplier<ScoreLayout> scoreLayoutSupplier,
            Measure[] currentMeasureHolder,
            Measure targetMeasure,
            int staffIndex
    ) {
        currentMeasureHolder[0] = targetMeasure;
        syncSelection(scoreLayoutSupplier, targetMeasure, staffIndex);
        updateDialogState(dialog, targetMeasure);
    }

    private static void applyChanges(
            Supplier<ScoreLayout> scoreLayoutSupplier,
            Measure[] currentMeasureHolder,
            int staffIndex,
            CustomMeasurePropertiesDialog.MeasureLengthResult result
    ) {
        Measure m = currentMeasureHolder[0];
        if (m == null) return;

        TimeSignature currentTs = m.getTimeSignature();

        int nomNum = (currentTs != null) ? currentTs.getNominalBeat() : 4;
        int nomDen = (currentTs != null) ? currentTs.getNominalBeatType() : 4;
        TimeSignature.Type type = (currentTs != null) ? currentTs.getType() : TimeSignature.Type.FRACTIONAL;

        TimeSignature updatedTs = new TimeSignature(
                nomNum,
                nomDen,
                result.actualNumerator(),
                result.actualDenominator(),
                type,
                m
        );

        m.setTimeSignature(updatedTs, true);
        m.setDirty(true);
        ScoreStateManager.getInstance().notifyScoreChanged();
        Platform.runLater(() -> syncSelection(scoreLayoutSupplier, m, staffIndex));
    }

    private static void syncSelection(Supplier<ScoreLayout> scoreLayoutSupplier, Measure targetMeasure, int staffIndex) {
        if (scoreLayoutSupplier == null) return;

        ScoreLayout freshLayout = scoreLayoutSupplier.get();
        if (freshLayout == null) return;

        MeasureStaffSelection newSelection = LayoutHitTester.findSelectionInScoreLayout(freshLayout, targetMeasure, staffIndex);
        if (newSelection != null) {
            ScoreStateManager.getInstance().setSelected(newSelection);
        }
    }

    private static void updateDialogState(CustomMeasurePropertiesDialog dialog, Measure m) {
        if (m == null) return;
        TimeSignature ts = m.getTimeSignature();

        int nomNum = (ts != null) ? ts.getNominalBeat() : 4;
        int nomDen = (ts != null) ? ts.getNominalBeatType() : 4;
        int actNum = (ts != null) ? ts.getActualBeat() : nomNum;
        int actDen = (ts != null) ? ts.getActualBeatType() : nomDen;

        dialog.setNominalLength(nomNum, nomDen)
                .setActualLength(actNum, actDen)
                .setNavigationState(m.getPrev() != null, m.getNext() != null);
    }
}