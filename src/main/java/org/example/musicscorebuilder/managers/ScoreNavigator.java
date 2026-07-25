package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.edit.CursorLayout;

public class ScoreNavigator {
    private static ScoreNavigator instance;
    private CursorLayout cursorLayout;
    private final ScoreStateManager scoreStateManager = ScoreStateManager.getInstance();

    private ScoreNavigator() {}

    public static synchronized ScoreNavigator getInstance() {
        if (instance == null) {
            instance = new ScoreNavigator();
        }
        return instance;
    }

    public void moveCursorNext() {
        if (cursorLayout == null) return;
        var currentSegment = cursorLayout.getSegment();
        if (currentSegment == null) return;

        var nextSegment = currentSegment.getNextSameType();
        assignFirstElOfVoiceToCursor(nextSegment, true);
    }

    public void moveCursorPrev() {
        if (cursorLayout == null) return;
        var currentSegment = cursorLayout.getSegment();
        if (currentSegment == null) return;

        var prevSegment = currentSegment.getPrevSameType();
        assignFirstElOfVoiceToCursor(prevSegment, false);
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
        scoreStateManager.notifyScoreChanged();
    }

    private void assignFirstElOfVoiceToCursor(SegmentLayout startSegment, boolean forward) {
        SegmentLayout currentSegment = startSegment;
        var currentStaffLayout = cursorLayout.getStaff();
        var voice = cursorLayout.getElement().getVoice();

        int staffIndex = currentStaffLayout.getParent().getStaffs().indexOf(currentStaffLayout);

        while (currentSegment != null) {
            var measureLayout = currentSegment.getParent();
            if (staffIndex < 0 || staffIndex >= measureLayout.getStaffs().size()) {
                currentSegment = forward ? currentSegment.getNextSameType() : currentSegment.getPrevSameType();
                continue;
            }
            var targetStaffLayout = measureLayout.getStaffs().get(staffIndex);

            var elementsOnStaff = currentSegment.getElementsByStaff(targetStaffLayout);

            var targetElement = elementsOnStaff.stream()
                    .filter(el -> el.getVoice() == voice)
                    .findFirst();

            if (targetElement.isPresent()) {
                setCursorLayout(new CursorLayout(targetElement.get()));
                return;
            }

            currentSegment = forward ? currentSegment.getNextSameType() : currentSegment.getPrevSameType();
        }
    }
}