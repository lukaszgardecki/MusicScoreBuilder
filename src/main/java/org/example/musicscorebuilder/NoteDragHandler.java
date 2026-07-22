package org.example.musicscorebuilder;

import javafx.scene.input.MouseEvent;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.views.BackgroundView;

import java.util.function.Function;
import java.util.function.Supplier;

public class NoteDragHandler {
    private static final double DRAG_THRESHOLD = 0.15;

    private record DragSession(NoteLayout note, double startMouseY, double offsetY) {}

    private final BackgroundView container;
    private final Function<MouseEvent, Selectable> elementFinder;
    private final Supplier<ScoreLayout> layoutProvider;
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private DragSession session = null;
    private boolean isDragActive = false;
    private boolean isDraggingOtherElement = false;

    public NoteDragHandler(
            BackgroundView container,
            Function<MouseEvent, Selectable> elementFinder,
            Supplier<ScoreLayout> layoutProvider) {
        this.container = container;
        this.elementFinder = elementFinder;
        this.layoutProvider = layoutProvider;
    }

    public void handlePressed(MouseEvent event) {
        Selectable clicked = elementFinder.apply(event);
        if (clicked instanceof NoteLayout note) startNoteDragSession(note, event);
        else if (clicked != null) startOtherElementDragSession();
        else reset();
    }

    public void handleDragged(MouseEvent event) {
        ScoreLayout layout = layoutProvider.get();
        if (layout == null) return;

        if (session != null) processNoteDrag(event);
        else if (isDraggingOtherElement) container.updateContent(layout);
    }

    public void handleReleased(MouseEvent event) {
        ScoreLayout layout = layoutProvider.get();
        if (layout == null) {
            reset();
            return;
        }

        if (session != null && isDragActive) {
            stateManager.notifyScoreChanged();
            event.consume();
        } else if (isDraggingOtherElement) {
            stateManager.notifyScoreChanged();
            container.updateContent(layout);
        }

        reset();
    }

    private void startNoteDragSession(NoteLayout note, MouseEvent event) {
        double mouseModelY = container.toModelY(event.getY());

        session = new DragSession(note, mouseModelY, mouseModelY - note.getY());
        isDragActive = false;
        isDraggingOtherElement = false;
        stateManager.clearSelection();
    }

    private void startOtherElementDragSession() {
        session = null;
        isDragActive = false;
        isDraggingOtherElement = true;
    }

    private void processNoteDrag(MouseEvent event) {
        event.consume();
        double currentMouseY = container.toModelY(event.getY());

        boolean userDrags = !isDragActive && Math.abs(currentMouseY - session.startMouseY()) > DRAG_THRESHOLD;
        if (userDrags) isDragActive = true;

        if (isDragActive) {
            double targetNoteY = currentMouseY - session.offsetY();
            double previousNoteY = session.note().getY();

            session.note().updatePitchFromY(targetNoteY);

            if (session.note().getY() != previousNoteY) {
                session.note().setSelected(true);
                container.updateContent(layoutProvider.get());
            }
        }
    }

    private void reset() {
        session = null;
        isDragActive = false;
        isDraggingOtherElement = false;
    }
}