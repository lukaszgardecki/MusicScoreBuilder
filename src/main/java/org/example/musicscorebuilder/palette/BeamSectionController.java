package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.music.BeamType;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.Arrays;
import java.util.List;

public class BeamSectionController extends AbstractPaletteSectionController<BeamAction> {
    private final BeamSectionRenderer renderer = new BeamSectionRenderer();

    public BeamSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() {
        return 5;
    }

    @Override
    protected List<BeamAction> getItems() {
        return Arrays.stream(BeamAction.values()).toList();
    }

    @Override
    protected boolean applyToSelectedElement(BeamAction action) {
        Selectable item = stateManager.getSelectedItem();
        if (item == null) return false;

        if (item instanceof NoteLayout noteLayout) {
            Note note = noteLayout.getNote();
            if (note == null) return false;

            NoteLayout prevLayout = noteLayout.getPrevNoteInVoice();
            NoteLayout nextLayout = noteLayout.getNextNoteInVoice();

            Note prevNote = (prevLayout != null) ? prevLayout.getNote() : null;
            Note nextNote = (nextLayout != null) ? nextLayout.getNote() : null;

            switch (action) {
                case AUTO -> {
                    note.setBeam(null);
                    updatePrevOnCut(prevNote);
                    updateNextOnCut(nextNote);
                }
                case NONE -> {
                    note.setBeam(BeamType.NONE);
                    updatePrevOnCut(prevNote);
                    updateNextOnCut(nextNote);
                }
                case BREAK_LEFT -> {
                    updatePrevOnCut(prevNote);
                    if (canBeam(nextNote)) {
                        if (nextNote.getBeam() == null || nextNote.getBeam() == BeamType.NONE) {
                            nextNote.setBeam(BeamType.END);
                        }
                        note.setBeam(BeamType.BEGIN);
                    } else {
                        note.setBeam(BeamType.NONE);
                    }
                }
                case BREAK_INNER_8TH, BREAK_INNER_16TH -> {
                    updateNextOnCut(nextNote);
                    if (canBeam(prevNote)) {
                        if (prevNote.getBeam() == null || prevNote.getBeam() == BeamType.NONE) {
                            prevNote.setBeam(BeamType.BEGIN);
                        }
                        note.setBeam(BeamType.END);
                    } else {
                        note.setBeam(BeamType.NONE);
                    }
                }
                case CONNECT -> {
                    if (canBeam(prevNote)) {
                        if (prevNote.getBeam() == null || prevNote.getBeam() == BeamType.NONE) {
                            prevNote.setBeam(BeamType.BEGIN);
                        } else if (prevNote.getBeam() == BeamType.END) {
                            prevNote.setBeam(BeamType.CONTINUE);
                        }

                        if (canBeam(nextNote) && nextNote.isBeamed()) {
                            note.setBeam(BeamType.CONTINUE);
                        } else {
                            note.setBeam(BeamType.END);
                        }
                    }
                }
            }

            note.getParent().setDirty(true);
            ScoreStateManager.getInstance().notifyScoreChanged();
            return true;
        }
        return false;
    }

    private boolean canBeam(Note note) {
        return note != null && note.getType() != null && note.getType().hasFlag();
    }

    @Override
    protected Node createButtonGraphic(BeamAction action) {
        Canvas canvas = createBaseCanvas(true, false);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        double width = canvas.getWidth();
        double height = canvas.getHeight();

        switch (action) {
            case AUTO -> renderer.renderAuto(gc, width, height);
            case NONE -> renderer.render8thSingle(gc, width, height);
            case BREAK_LEFT -> renderer.renderBreakInner16th(gc, width, height);
            case BREAK_INNER_8TH -> renderer.renderBreakInner8th(gc, width, height);
            case BREAK_INNER_16TH -> renderer.renderBreakInner32nd(gc, width, height);
            case CONNECT -> renderer.renderConnect16th(gc, width, height);
        }

        return canvas;
    }

    private void updatePrevOnCut(Note prevNote) {
        if (prevNote == null) return;
        if (prevNote.getBeam() == BeamType.BEGIN) {
            prevNote.setBeam(BeamType.NONE);
        } else if (prevNote.getBeam() == BeamType.CONTINUE) {
            prevNote.setBeam(BeamType.END);
        }
    }

    private void updateNextOnCut(Note nextNote) {
        if (nextNote == null) return;
        if (nextNote.getBeam() == BeamType.END) {
            nextNote.setBeam(BeamType.NONE);
        } else if (nextNote.getBeam() == BeamType.CONTINUE) {
            nextNote.setBeam(BeamType.BEGIN);
        }
    }

}