package org.example.musicscorebuilder.palette;

import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.GridPane;
import org.example.musicscorebuilder.components.layout.MeasureStaffSelection;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        return Arrays.stream(BeamAction.values()).collect(Collectors.toList());
    }

    @Override
    protected boolean applyToSelectedElement(BeamAction action) {
        Selectable item = stateManager.getSelectedItem();
        boolean handled = false;
        if (item == null) return false;

        if (item instanceof NoteLayout noteLayout) {
            Note note = noteLayout.getNote();
            if (note == null) return false;

            NoteLayout prevLayout = noteLayout.getPrevNoteInVoice();
            NoteLayout nextLayout = noteLayout.getNextNoteInVoice();

            Note prevNote = (prevLayout != null) ? prevLayout.getNote() : null;
            Note nextNote = (nextLayout != null) ? nextLayout.getNote() : null;

            switch (action) {
                case AUTO -> handleAutoAction(note,  prevNote, nextNote);
                case NONE -> handleNoneAction(note,  prevNote, nextNote);
                case BREAK_LEFT -> handleBreakLeftAction(note, prevNote, nextNote);
                case BREAK_INNER_8TH, BREAK_INNER_16TH -> handleBreakInnerAction(note, prevNote, nextNote);
                case CONNECT -> handleConnectAction(note, prevNote, nextNote);
            }

            note.getParent().setDirty(true);
            handled = true;
        } else if (item instanceof MeasureStaffSelection selection) {
            if (action == BeamAction.AUTO || action == BeamAction.NONE) {
                Measure measure = selection.getMeasure().getMeasure();
                List<Segment> segments = measure.getSegments();
                for (int i = 0; i < segments.size(); i++) {
                    Segment segment = segments.get(i);
                    int staffIndex = selection.getStaff().getStaffIndex();
                    List<Element> staffElements = segment.getElementsByStaff(staffIndex);

                    for (int k = 0; k < staffElements.size(); k++) {
                        Element el = staffElements.get(k);
                        if (el instanceof Note nl) {
                            if (action == BeamAction.AUTO) nl.setBeam(null);
                            else nl.setBeam(BeamType.NONE);
                        }
                    }
                }
                measure.setDirty(true);
                handled = true;
            }
        }

        if (handled) {
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

    private void handleAutoAction(Note note, Note prevNote, Note nextNote) {
        note.setBeam(null);
        updatePrevOnCut(prevNote);
        updateNextOnCut(nextNote);
    }

    private void handleNoneAction(Note note, Note prevNote, Note nextNote) {
        note.setBeam(BeamType.NONE);
        updatePrevOnCut(prevNote);
        updateNextOnCut(nextNote);
    }

    private void handleBreakLeftAction(Note note, Note prevNote, Note nextNote) {
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

    private void handleBreakInnerAction(Note note, Note prevNote, Note nextNote) {
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

    private void handleConnectAction(Note note, Note prevNote, Note nextNote) {
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