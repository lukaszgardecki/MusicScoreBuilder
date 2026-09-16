package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.JumpMarkView;
import org.example.musicscorebuilder.components.views.VoltaView;

import java.util.Arrays;
import java.util.List;

public class RepeatsSectionController extends AbstractPaletteSectionController<Repeat> {
    private final VoltaView voltaView = new VoltaView();
    private final JumpMarkView jumpMarkView = new JumpMarkView();

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

                Volta existingVolta = mode.getVoltas().stream()
                        .filter(v -> v.getStartMeasure().equals(startMeasure))
                        .findFirst()
                        .orElse(null);

                if (existingVolta != null) {
                    mode.removeVolta(existingVolta);
                    stateManager.notifyScoreChanged();
                    return true;
                }

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
                    JumpMark existingJump = mode.getJumpMarks().stream()
                            .filter(j -> j.getMeasure().equals(startMeasure) && j.getType() == jumpType)
                            .findFirst()
                            .orElse(null);

                    if (existingJump != null) {
                        mode.removeJumpMark(existingJump);
                        stateManager.notifyScoreChanged();
                        return true;
                    }

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

        if (isVolta(item)) {
            drawVoltaIcon(gc, item, w, h);
        } else {
            drawJumpMarkIcon(gc, item, w, h);
        }
        return canvas;
    }

    private void drawJumpMarkIcon(GraphicsContext gc, Repeat item, double w, double h) {
        JumpType type = mapToJumpType(item);
        if (type == null) return;

        JumpMark jumpMark = new JumpMark(type, null);

        Measure dummyMeasure = new Measure(List.of());
        ScoreStyle scoreStyle = new ScoreStyle();
        ScoreLayout scoreLayout = new ScoreLayout(new Score(), scoreStyle);
        PageLayout pageLayout = new PageLayout(scoreLayout, 0);
        SystemLayout systemLayout = new SystemLayout(pageLayout, BraceType.BRACE);
        MeasureLayout dummyMeasureLayout = new MeasureLayout(dummyMeasure, systemLayout, scoreStyle);
        dummyMeasureLayout.getSegments().add(new SegmentLayout(SegmentType.NOTEREST, dummyMeasureLayout));
        StaffLayout dummyStaff = new StaffLayout(new Staff(0, new Clef(ClefType.C)), dummyMeasureLayout, scoreStyle);

        double targetY = 0.0;
        JumpMarkLayout layout = type.isSymbol()
                ? new JumpSignLayout(jumpMark, dummyMeasureLayout, dummyStaff, targetY)
                : new JumpTextLayout(jumpMark, dummyMeasureLayout, dummyStaff, targetY);

        double sp = 5.5;
        double margin = 4.0;
        double maxAllowedHeight = h - (2 * margin);
        if (layout.getHeight() > 0 && (layout.getHeight() * sp) > maxAllowedHeight) {
            sp = maxAllowedHeight / layout.getHeight();
        }
        double layoutWidthPx = layout.getWidth() * sp;
        double measureX = (layout.getPosition() == JumpType.Position.END_OF_MEASURE)
                ? (w + layoutWidthPx) / 2.0
                : (w - layoutWidthPx) / 2.0;
        double boxCenterSp = layout.getBoxY() + (layout.getHeight() / 2.0);
        double measureY = (h / 2.0) - (boxCenterSp * sp);

        jumpMarkView.draw(gc, layout, measureX, measureY, sp);
    }

    private void drawVoltaIcon(GraphicsContext gc, Repeat item, double w, double h) {
        boolean closedEnd = (item == Repeat.VOLTA_2_CLOSED || item == Repeat.VOLTA_3_CLOSED);
        double sp = 6.5;
        double startX = 6.0;
        double targetVoltaY = h * 0.35;
        double measureY = targetVoltaY + (2.5 * sp);
        VoltaSliceLayout mockSlice = new VoltaSliceLayout(null, item.getText(), true, closedEnd);
        voltaView.draw(gc, mockSlice, startX, measureY, sp);
    }

    private boolean isVolta(Repeat item) {
        return item == Repeat.VOLTA_1 || item == Repeat.VOLTA_2_OPENED
                || item == Repeat.VOLTA_2_CLOSED || item == Repeat.VOLTA_3_CLOSED;
    }

    private JumpType mapToJumpType(Repeat item) {
        return switch (item) {
            case SEGNO -> JumpType.SEGNO;
            case SEGNO_SERPENT_1 -> JumpType.SEGNO_SERPENT_1;
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