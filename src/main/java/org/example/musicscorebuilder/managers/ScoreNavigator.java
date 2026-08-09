package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;
import org.example.musicscorebuilder.controller.util.audio.PianoPlayer;

import java.util.List;

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
        if (cursorLayout == null || cursorLayout.getSegment() == null) return;
        var nextSegment = cursorLayout.getSegment().getNextSameType();
        assignFirstElOfVoiceToCursor(nextSegment, true);
    }

    private void moveCursorPrev() {
        if (cursorLayout == null || cursorLayout.getSegment() == null) return;
        var prevSegment = cursorLayout.getSegment().getPrevSameType();
        assignFirstElOfVoiceToCursor(prevSegment, false);
    }

    private void moveSelection(boolean forward) {
        Selectable selected = scoreStateManager.getSelectedItem();
        if (selected == null) return;

        SegmentLayout currentSegment = selected.getSegment();
        StaffLayout currentStaff = selected.getStaff();
        if (currentSegment == null || currentStaff == null) return;

        int currentVoice = selected.getVoice();
        SegmentLayout startSeg = forward ? currentSegment.getNextSameType() : currentSegment.getPrevSameType();

        ElementLayout targetElement = findAdjacentElement(startSeg, currentStaff, currentVoice, forward);
        if (targetElement instanceof Selectable selectableTarget) {
            scoreStateManager.setSelected(selectableTarget);
            if (selectableTarget instanceof NoteLayout note) {
                PianoPlayer.getInstance().playNote(note.getNote().getPitch());
            }
        }
    }

    private void assignFirstElOfVoiceToCursor(SegmentLayout startSegment, boolean forward) {
        if (cursorLayout == null) return;
        int voice = ModeManager.getInstance().getCurrentVoice();

        ElementLayout targetElement = findAdjacentElement(startSegment, cursorLayout.getStaff(), voice, forward);
        if (targetElement != null) {
            setCursorLayout(new CursorLayout(targetElement));
        }
    }

    private ElementLayout findAdjacentElement(SegmentLayout startSegment, StaffLayout currentStaffLayout, int preferredVoice, boolean forward) {
        SegmentLayout currentSegment = startSegment;
        int staffIndex = currentStaffLayout.getParent().getStaffs().indexOf(currentStaffLayout);

        while (currentSegment != null) {
            var measureLayout = currentSegment.getParent();
            if (staffIndex >= 0 && staffIndex < measureLayout.getStaffs().size()) {
                StaffLayout targetStaffLayout = measureLayout.getStaffs().get(staffIndex);
                List<ElementLayout> elementsOnStaff = currentSegment.getElementsByStaff(targetStaffLayout);

                if (!elementsOnStaff.isEmpty()) {
                    var voiceMatch = elementsOnStaff.stream()
                            .filter(el -> el.getVoice() == preferredVoice)
                            .findFirst();

                    if (voiceMatch.isPresent()) return voiceMatch.get();
                }
            }
            currentSegment = forward ? currentSegment.getNextSameType() : currentSegment.getPrevSameType();
        }
        return null;
    }
}