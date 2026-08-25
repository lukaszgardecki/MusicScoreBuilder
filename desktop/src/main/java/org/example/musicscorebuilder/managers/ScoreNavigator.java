package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;

public class ScoreNavigator {
    private static ScoreNavigator instance;
    private CursorLayout cursorLayout;
    private final ScoreStateManager scoreStateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();

    private ScoreNavigator() {}

    public static synchronized ScoreNavigator getInstance() {
        if (instance == null) {
            instance = new ScoreNavigator();
        }
        return instance;
    }

    public void moveNext() {
        if (modeManager.isInsertMode()) {
            moveCursorNext();
        } else {
            moveSelection(true);
        }
    }

    public void movePrev() {
        if (modeManager.isInsertMode()) {
            moveCursorPrev();
        } else {
            moveSelection(false);
        }
    }

    public void clearCursor() {
        if (this.cursorLayout != null) {
            if (this.cursorLayout.getElement() != null) {
                this.cursorLayout.getElement().setSelected(false);
            }
            if (this.cursorLayout.getSegment() != null) {
                this.cursorLayout.getSegment().setCursor(null);
            }
            this.cursorLayout = null;
            scoreStateManager.notifyScoreChanged();
        }
    }

    public CursorLayout getLastCursor() { return cursorLayout; }

    public void setCursorLayout(CursorLayout newCursorLayout) {
        setCursorLayout(newCursorLayout, true);
    }

    public void setCursorLayoutQuietly(CursorLayout newCursorLayout) {
        setCursorLayout(newCursorLayout, false);
    }

    public void setCursorLayout(CursorLayout newCursorLayout, boolean notify) {
        if (this.cursorLayout != null) {
            if (this.cursorLayout.getElement() != null) {
                this.cursorLayout.getElement().setSelected(false);
            }
            if (this.cursorLayout.getSegment() != null) {
                this.cursorLayout.getSegment().setCursor(null);
            }
        }

        this.cursorLayout = newCursorLayout;

        if (this.cursorLayout != null && this.cursorLayout.getSegment() != null) {
            this.cursorLayout.getSegment().setCursor(newCursorLayout);
        }

        if (notify) {
            scoreStateManager.notifyScoreChanged();
        }
    }

    public void switchToVoice(int targetVoice) {
        if (cursorLayout == null || cursorLayout.getElement() == null) return;

        var currentSegmentLayout = cursorLayout.getSegment();
        var currentStaffLayout = cursorLayout.getStaff();

        var elementsOnStaff = currentSegmentLayout.getElementsByStaff(currentStaffLayout);

        var targetElement = elementsOnStaff.stream()
                .filter(el -> el.getVoice() == targetVoice)
                .findFirst();

        if (targetElement.isPresent()) {
            setCursorLayout(new CursorLayout(targetElement.get()));
        } else {
            setCursorLayout(new CursorLayout(cursorLayout.getElement()));
        }
    }

    private void moveCursorNext() {
        if (cursorLayout == null || !(cursorLayout.getElement() instanceof NoteRestLayout current)) return;

        NoteRestLayout next = current.getNextInVoice();
        if (next != null) {
            setCursorLayout(new CursorLayout(next));
        }
    }

    private void moveCursorPrev() {
        if (cursorLayout == null || !(cursorLayout.getElement() instanceof NoteRestLayout current)) return;

        NoteRestLayout prev = current.getPrevInVoice();
        if (prev != null) {
            setCursorLayout(new CursorLayout(prev));
        }
    }

    private void moveSelection(boolean forward) {
        Selectable selected = scoreStateManager.getSelectedItem();
        if (selected instanceof NoteRestLayout current) {
            NoteRestLayout target = forward ? current.getNextInVoice() : current.getPrevInVoice();
            if (target != null) {
                scoreStateManager.setSelected(target);
                if (target instanceof NoteLayout note) {
                    PianoPlayer.getInstance().playNote(note.getNote().getPitch());
                }
            }
        }
    }
}