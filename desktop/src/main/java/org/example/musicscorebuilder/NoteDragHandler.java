package org.example.musicscorebuilder;

import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;
import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.music.util.MeasureNoteInserter;
import org.example.musicscorebuilder.components.music.util.TiedNoteService;
import org.example.musicscorebuilder.components.views.BackgroundView;
import org.example.musicscorebuilder.managers.PianoPlayer;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.function.Function;
import java.util.function.Supplier;

public class NoteDragHandler {
    private static final double DRAG_THRESHOLD = 0.15;

    private record DragSession(NoteLayout note, double startMouseY, double offsetY) {}

    private final BackgroundView container;
    private final Function<MouseEvent, Selectable> elementFinder;
    private final Supplier<ScoreLayout> layoutProvider;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();

    private DragSession session = null;
    private boolean isDragActive = false;

    public NoteDragHandler(
            BackgroundView container,
            Function<MouseEvent, Selectable> elementFinder,
            Supplier<ScoreLayout> layoutProvider) {
        this.container = container;
        this.elementFinder = elementFinder;
        this.layoutProvider = layoutProvider;
    }

    public void handlePressed(MouseEvent event) {
        if (modeManager.isInsertMode()) {
            event.consume();
            handleInsertModeClick(event);
            return;
        }

        Selectable clicked = elementFinder.apply(event);
        if (clicked instanceof NoteLayout note) {
            startNoteDragSession(note, event);
        } else {
            reset();
        }
    }

    public void handleDragged(MouseEvent event) {
        if (modeManager.isInsertMode() || session == null) return;

        ScoreLayout layout = layoutProvider.get();
        if (layout != null) {
            processNoteDrag(event);
        }
    }

    public void handleReleased(MouseEvent event) {
        if (modeManager.isInsertMode()) return;

        if (session != null && isDragActive) {
            stateManager.notifyScoreChanged();
            event.consume();
        }

        reset();
    }

    private void handleInsertModeClick(MouseEvent event) {
        event.consume();
        GhostNoteLayout gN = modeManager.getGhostNote();
        if (gN == null) return;

        Segment segment = gN.getSegment().getSegment();
        Measure measure = segment.getParent();
        int staffId = gN.getStaff().getStaffIndex();
        Note note = gN.getNote();

        PianoPlayer.getInstance().playNote(note.getPitch());
        Segment nextSegment = MeasureNoteInserter.insertNote(measure, segment, staffId, note);

        stateManager.notifyScoreChanged();

        ScoreLayout updatedLayout = layoutProvider.get();
        if (nextSegment != null && updatedLayout != null) {
            Selectable targetSelectable = LayoutHitTester.findSelectableForSegmentAndStaff(
                    updatedLayout.getPages(),
                    nextSegment,
                    staffId,
                    note.getVoice()
            );

            if (targetSelectable != null) {
                ScoreNavigator.getInstance().setCursorLayout(new CursorLayout(targetSelectable));
            }
        }
    }

    private void startNoteDragSession(NoteLayout note, MouseEvent event) {
        double mouseModelY = container.toModelY(event.getY());

        session = new DragSession(note, mouseModelY, mouseModelY - note.getY());
        isDragActive = false;
    }

    private void processNoteDrag(MouseEvent event) {
        event.consume();
        double currentMouseY = container.toModelY(event.getY());

        boolean userDrags = !isDragActive && Math.abs(currentMouseY - session.startMouseY()) > DRAG_THRESHOLD;
        if (userDrags) {
            isDragActive = true;
            boolean isAdditive = event.isShortcutDown() || event.isControlDown() || event.isMetaDown();
            if (!isAdditive && !stateManager.getSelectedItems().contains(session.note())) {
                stateManager.setSelected(session.note(), false);
            }
        }

        if (isDragActive) {
            double targetNoteY = currentMouseY - session.offsetY();
            double previousNoteY = session.note().getY();

            session.note().updatePitchFromY(targetNoteY);

            if (session.note().getY() != previousNoteY) {
                session.note().setSelected(true);

                TiedNoteService.syncTiedNotesPitch(session.note());
                container.updateContent(layoutProvider.get());
                if (session.note().getNote() != null) {
                    PianoPlayer.getInstance().playNote(session.note().getNote().getPitch());
                }
            }
        }
    }

    private void reset() {
        session = null;
        isDragActive = false;
    }
}