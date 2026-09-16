package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.components.layout.MeasureStaffSelection;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.VoltaSliceLayout;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.VoltaView;

import java.util.Arrays;
import java.util.List;

public class RepeatsSectionController extends AbstractPaletteSectionController<Repeat> {
    private final VoltaView voltaView = new VoltaView();

    public RepeatsSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() { return 2; }

    @Override
    protected List<Repeat> getItems() {
        return Arrays.asList(Repeat.values());
    }

    @Override
    protected boolean applyToSelectedElement(Repeat item) {
        Selectable selectedLayout = stateManager.getSelectedItem();
        if (selectedLayout == null) return false;

        if (selectedLayout instanceof MeasureStaffSelection selection) {
            ScoreMode mode = stateManager.getCurrentMode();

            Measure startMeasure = selection.getFirstMeasure() != null ? selection.getFirstMeasure().getMeasure() : null;
            Measure endMeasure = selection.getLastMeasure() != null ? selection.getLastMeasure().getMeasure() : null;

            if (startMeasure == null) return false;

            if (isVolta(item)) {
                if (endMeasure == null) return false;

                Volta volta = switch (item) {
                    case VOLTA_1, VOLTA_2_OPENED -> new Volta(startMeasure, endMeasure, item.getText(), false);
                    case VOLTA_2_CLOSED, VOLTA_3_CLOSED -> new Volta(startMeasure, endMeasure, item.getText(), true);
                    default -> null;
                };

                if (volta != null) {
                    mode.addVolta(volta);
                    stateManager.notifyScoreChanged();
                    return true;
                }
            } else {
                JumpType jumpType = mapToJumpType(item);
                if (jumpType != null) {
                    JumpMark jumpMark = new JumpMark(jumpType, startMeasure);
                    mode.addJumpMark(jumpMark);
                    stateManager.notifyScoreChanged();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected Node createButtonGraphic(Repeat item) {
        Canvas canvas = createBaseCanvas(false, false);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        switch (item) {
            case SEGNO -> {}
            case CODA -> {}
            case CODA_SQUARE -> {}
            case FINE -> {}
            case TO_CODA -> {}
            case DC -> {}
            case DS -> {}
            case DC_AL_FINE -> {}
            case DS_AL_FINE -> {}
            case DC_AL_CODA -> {}
            case DS_AL_CODA -> {}
            case VOLTA_1, VOLTA_2_OPENED -> drawVoltaIcon(gc, item.getText(), false, w, h);
            case VOLTA_2_CLOSED, VOLTA_3_CLOSED -> drawVoltaIcon(gc, item.getText(), true, w, h);
        }
        return canvas;
    }

    private void drawVoltaIcon(GraphicsContext gc, String text, boolean closedEnd, double w, double h) {
        double sp = 6.5;
        double startX = 6.0;
        double targetVoltaY = h * 0.35;
        double measureY = targetVoltaY + (2.5 * sp);
        VoltaSliceLayout mockSlice = new VoltaSliceLayout(null, text, true, closedEnd);
        voltaView.draw(gc, mockSlice, startX, measureY, sp);
    }

    private boolean isVolta(Repeat item) {
        return item == Repeat.VOLTA_1 || item == Repeat.VOLTA_2_OPENED
                || item == Repeat.VOLTA_2_CLOSED || item == Repeat.VOLTA_3_CLOSED;
    }

    private JumpType mapToJumpType(Repeat item) {
        return switch (item) {
            case SEGNO -> JumpType.SEGNO;
            case CODA -> JumpType.CODA;
            case CODA_SQUARE -> JumpType.CODA_SQUARE;
            case FINE -> JumpType.FINE;
            case TO_CODA -> JumpType.TO_CODA;
            case DC -> JumpType.DC;
            case DS -> JumpType.DS;
            case DC_AL_FINE -> JumpType.DC_AL_FINE;
            case DS_AL_FINE -> JumpType.DS_AL_FINE;
            case DC_AL_CODA -> JumpType.DC_AL_CODA;
            case DS_AL_CODA -> JumpType.DS_AL_CODA;
            default -> null;
        };
    }
}
