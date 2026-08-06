package org.example.musicscorebuilder.managers;

import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.music.util.PitchTransposer;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ScoreStateManager {
    private static ScoreStateManager instance;
    private final List<ScoreChangeListener> scoreChangeListeners = new ArrayList<>();
    private final List<Selectable> selectedItems = new ArrayList<>();
    private int currentModeIndex = 0;
    private final List<SelectionChangeListener> selectionChangeListeners = new ArrayList<>();
    private Consumer<ScoreLayout> postRefreshAction;

    private ScoreStateManager() {}

    public static synchronized ScoreStateManager getInstance() {
        if (instance == null) {
            instance = new ScoreStateManager();
        }
        return instance;
    }

    public void setCurrentModeIndex(int index) {
        this.currentModeIndex = index;
        notifyScoreChanged();
    }

    public ScoreMode getCurrentMode(Score score) {
        if (score == null || score.getModes().isEmpty()) return null;
        if (currentModeIndex < 0 || currentModeIndex >= score.getModes().size()) {
            return score.getModes().getFirst();
        }
        return score.getModes().get(currentModeIndex);
    }

    public void setSelected(Selectable item) {
        setSelected(item, false);
    }

    public void setSelected(Selectable item, boolean isAdditive) {
        if (item == null) {
            if (!isAdditive) {
                deselectAll();
            }
            return;
        }

        List<Selectable> itemsToSelect = LayoutHitTester.resolveSelection(item);
        if (itemsToSelect.isEmpty()) return;

        if (isAdditive) {
            boolean allAlreadySelected = new HashSet<>(selectedItems).containsAll(itemsToSelect);
            if (allAlreadySelected) {
                for (Selectable sel : itemsToSelect) {
                    deselect(sel);
                }
            } else {
                for (Selectable sel : itemsToSelect) {
                    select(sel);
                }
            }
        } else {
            deselectAll();
            selectAll(itemsToSelect);
        }

        Selectable currentSelected = getSelectedItem();
        for (SelectionChangeListener listener : selectionChangeListeners) {
            listener.onSelectionChanged(currentSelected);
        }
    }

    public void setPostRefreshAction(Consumer<ScoreLayout> action) {
        this.postRefreshAction = action;
    }

    public void clearSelection() {
        setSelected(null, false);
    }

    public List<Selectable> getSelectedItems() {
        return selectedItems;
    }

    public Selectable getSelectedItem() {
        return selectedItems.isEmpty() ? null : selectedItems.getFirst();
    }

    public Optional<Selectable> getFirstSelectedNoteRest() {
        if (selectedItems.isEmpty()) return Optional.empty();
        return selectedItems.stream()
                .filter(s -> s.getSegment().getType() == SegmentType.NOTEREST)
                .findFirst();
    }

    public void addScoreChangeListener(ScoreChangeListener listener) {
        scoreChangeListeners.add(listener);
    }

    public void addSelectionChangeListener(SelectionChangeListener listener) {
        selectionChangeListeners.add(listener);
    }

    public void notifyScoreChanged() {
        for (ScoreChangeListener l : scoreChangeListeners) {
            l.onScoreChanged();
        }
    }

    private void select(Selectable item) {
        if (item != null && !selectedItems.contains(item)) {
            selectedItems.add(item);
            item.setSelected(true);
        }
    }

    private void deselect(Selectable item) {
        if (item != null && selectedItems.contains(item)) {
            selectedItems.remove(item);
            item.setSelected(false);
        }
    }

    private void selectAll(Collection<? extends Selectable> items) {
        if (items != null) {
            for (Selectable item : items) {
                select(item);
            }
        }
    }

    private void deselectAll() {
        for (Selectable item : selectedItems) {
            if (item != null) {
                item.setSelected(false);
            }
        }
        selectedItems.clear();
    }

    public void applyPostRefreshAction(ScoreLayout layout) {
        if (postRefreshAction != null && layout != null) {
            var action = postRefreshAction;
            postRefreshAction = null;
            action.accept(layout);
        }
    }

    public void convertSelectedNoteToRest() {
        Selectable selected = getSelectedItem();
        if (!(selected instanceof NoteLayout noteLayout)) return;

        Note currentNote = noteLayout.getNote();
        SegmentLayout segLayout = noteLayout.getSegment();
        StaffLayout staffLayout = noteLayout.getStaff();

        if (currentNote == null || segLayout == null || staffLayout == null) return;

        Segment targetSegment = segLayout.getSegment();
        Staff staff = staffLayout.getStaff();
        if (targetSegment == null || staff == null) return;

        Measure measure = targetSegment.getParent();
        if (measure == null) return;

        int targetVoice = currentNote.getVoice();

        if (currentNote.isTieStart()) {
            toggleTieForSelectedNote();
        }

        postRefreshAction = layout -> {
            var staffElements = targetSegment.getElementsByStaff(staff);
            if (staffElements != null) {
                for (Element el : staffElements) {
                    if (el instanceof NoteRestElement nre && nre.getVoice() == targetVoice) {
                        Selectable newLayout = LayoutHitTester.findSelectableForElement(layout.getPages(), targetSegment, staff, nre);
                        if (newLayout != null) {
                            setSelected(newLayout);
                        }
                        break;
                    }
                }
            }
        };

        measure.convertNoteToRest(targetSegment, staff, currentNote);
        notifyScoreChanged();
    }

    public void changeSelectedElementDuration(NoteType type) {
        Selectable selected = getSelectedItem();
        if (selected == null) return;

        SegmentLayout segLayout = selected.getSegment();
        StaffLayout staffLayout = selected.getStaff();
        if (segLayout == null || staffLayout == null) return;

        final Segment targetSegment = segLayout.getSegment();
        if (targetSegment == null) return;

        final Staff staff = staffLayout.getStaff();
        final Measure measure = targetSegment.getParent();

        final NoteRestElement elementToChange;
        final int targetVoice;

        if (selected instanceof NoteLayout nl) {
            elementToChange = nl.getNote();
            targetVoice = nl.getNote().getVoice();
        } else if (selected instanceof RestLayout rl) {
            elementToChange = rl.getRest();
            targetVoice = rl.getRest().getVoice();
        } else {
            return;
        }

        if (elementToChange == null || measure == null || staff == null) return;
        if (elementToChange.getType() == type) return;

        postRefreshAction = layout -> {
            var staffElements = targetSegment.getElementsByStaff(staff);
            if (staffElements != null) {
                for (Element el : staffElements) {
                    if (el instanceof NoteRestElement nre && nre.getVoice() == targetVoice) {
                        Selectable newLayout = LayoutHitTester.findSelectableForElement(layout.getPages(), targetSegment, staff, nre);
                        if (newLayout != null) {
                            setSelected(newLayout);
                        }
                        break;
                    }
                }
            }
        };

        measure.changeElementDuration(targetSegment, staff, elementToChange, type);
        notifyScoreChanged();
    }

    public void changeSelectedElementDots(int dots) {
        Selectable selected = getSelectedItem();
        if (selected == null) return;

        SegmentLayout segLayout = selected.getSegment();
        StaffLayout staffLayout = selected.getStaff();
        if (segLayout == null || staffLayout == null) return;

        final Segment targetSegment = segLayout.getSegment();
        if (targetSegment == null) return;

        final Staff staff = staffLayout.getStaff();

        final NoteRestElement elementToChange;
        final int targetVoice;

        if (selected instanceof NoteLayout nl) {
            elementToChange = nl.getNote();
            targetVoice = nl.getNote().getVoice();
        } else if (selected instanceof RestLayout rl) {
            elementToChange = rl.getRest();
            targetVoice = rl.getRest().getVoice();
        } else {
            return;
        }

        if (elementToChange == null || staff == null) return;
        if (elementToChange.getDots() == dots) return;

        postRefreshAction = layout -> {
            var staffElements = targetSegment.getElementsByStaff(staff);
            if (staffElements != null) {
                for (Element el : staffElements) {
                    if (el instanceof NoteRestElement nre && nre.getVoice() == targetVoice) {
                        Selectable newLayout = LayoutHitTester.findSelectableForElement(layout.getPages(), targetSegment, staff, nre);
                        if (newLayout != null) {
                            setSelected(newLayout);
                        }
                        break;
                    }
                }
            }
        };

        elementToChange.setDots(dots);
        notifyScoreChanged();
    }

    public void toggleTieForSelectedNote() {
        Selectable selected = getSelectedItem();
        if (!(selected instanceof NoteLayout noteLayout)) return;

        Note currentNote = noteLayout.getNote();
        SegmentLayout segLayout = noteLayout.getSegment();
        StaffLayout staffLayout = noteLayout.getStaff();

        if (currentNote == null || segLayout == null || staffLayout == null) return;

        Staff staff = staffLayout.getStaff();

        if (currentNote.isTieStart()) {
            currentNote.setTieStart(false);
            NoteLayout nextNoteLayout = findNextNoteInVoice(segLayout, staff, currentNote.getVoice(), false);
            if (nextNoteLayout != null && isSamePitch(currentNote, nextNoteLayout.getNote())) {
                nextNoteLayout.getNote().setTieStop(false);
            }
        } else {
            NoteLayout nextNoteLayout = findNextNoteInVoice(segLayout, staff, currentNote.getVoice(), false);
            if (nextNoteLayout != null && isSamePitch(currentNote, nextNoteLayout.getNote())) {
                currentNote.setTieStart(true);
                nextNoteLayout.getNote().setTieStop(true);
            }
        }

        notifyScoreChanged();
    }

    public void toggleSlurForSelectedNote() {
        Score score = ScoreService.getInstance().getScore();
        ScoreMode mode = getCurrentMode(score);
        if (mode == null) return;

        List<NoteLayout> selectedNotes = selectedItems.stream()
                .filter(NoteLayout.class::isInstance)
                .map(NoteLayout.class::cast)
                .toList();

        Note startNote = null;
        Note endNote = null;

        if (selectedNotes.size() == 2) {
            NoteLayout n1 = selectedNotes.get(0);
            NoteLayout n2 = selectedNotes.get(1);

            if (isSegmentBefore(n2.getSegment(), n1.getSegment())) {
                startNote = n2.getNote();
                endNote = n1.getNote();
            } else {
                startNote = n1.getNote();
                endNote = n2.getNote();
            }
        } else if (getSelectedItem() instanceof NoteLayout startLayout) {
            startNote = startLayout.getNote();
            NoteLayout endLayout = findNextNoteInVoice(
                    startLayout.getSegment(),
                    startLayout.getStaff().getStaff(),
                    startNote.getVoice(),
                    true // ignoruj pauzy
            );
            if (endLayout != null) {
                endNote = endLayout.getNote();
            }
        }

        if (startNote == null || endNote == null) return;

        final Note finalStart = startNote;
        final Note finalEnd = endNote;

        Optional<Slur> existingSlur = mode.getSlurs().stream()
                .filter(s -> s.getStartNote() == finalStart && s.getEndNote() == finalEnd)
                .findFirst();

        if (existingSlur.isPresent()) {
            mode.removeSlur(existingSlur.get());
        } else {
            mode.addSlur(new Slur(finalStart, finalEnd));
        }

        notifyScoreChanged();
    }

    public void transposeSelectedNoteUp() {
        Selectable selected = getSelectedItem();
        if (!(selected instanceof NoteLayout noteLayout)) return;

        Note note = noteLayout.getNote();
        if (note == null || note.getPitch() == null) return;

        Clef clef = noteLayout.getStaff().getStaff().getDefaultClef();
        int maxLedgers = noteLayout.getScoreStyle().getNoteMaxLedgerLines();

        Pitch currentPitch = note.getPitch();
        Pitch candidate = new Pitch(currentPitch.getStep(), currentPitch.getAlter(), currentPitch.getOctave());

        PitchTransposer.transposeUp(candidate);

        if (isPitchWithinLedgerBounds(candidate, clef, maxLedgers)) {
            currentPitch.setStep(candidate.getStep());
            currentPitch.setAlter(candidate.getAlter());
            currentPitch.setOctave(candidate.getOctave());
            noteLayout.refresh();
            notifyScoreChanged();
        }
    }

    public void transposeSelectedNoteDown() {
        Selectable selected = getSelectedItem();
        if (!(selected instanceof NoteLayout noteLayout)) return;

        Note note = noteLayout.getNote();
        if (note == null || note.getPitch() == null) return;

        Clef clef = noteLayout.getStaff().getStaff().getDefaultClef();
        int maxLedgers = noteLayout.getScoreStyle().getNoteMaxLedgerLines();

        Pitch currentPitch = note.getPitch();
        Pitch candidate = new Pitch(currentPitch.getStep(), currentPitch.getAlter(), currentPitch.getOctave());

        PitchTransposer.transposeDown(candidate);

        if (isPitchWithinLedgerBounds(candidate, clef, maxLedgers)) {
            currentPitch.setStep(candidate.getStep());
            currentPitch.setAlter(candidate.getAlter());
            currentPitch.setOctave(candidate.getOctave());
            noteLayout.refresh();
            notifyScoreChanged();
        }
    }

    public boolean isNoteInSlur(Note note) {
        if (note == null) return false;

        Score score = ScoreService.getInstance().getScore();
        ScoreMode mode = getCurrentMode(score);
        if (mode == null) return false;

        return mode.getSlurs().stream()
                .anyMatch(slur -> slur.getStartNote() == note || slur.getEndNote() == note);
    }

    private boolean isSamePitch(Note n1, Note n2) {
        return n1.getStep() == n2.getStep()
                && n1.getAlter() == n2.getAlter()
                && n1.getOctave() == n2.getOctave();
    }

    private NoteLayout findNextNoteInVoice(SegmentLayout startSegment, Staff staff, int voice, boolean ignoreRests, Predicate<NoteLayout> filter) {
        SegmentLayout current = startSegment.getNext();

        while (current != null) {
            for (ElementLayout el : current.getElements()) {
                if (el.getStaff() != null && el.getStaff().getStaff() == staff && el.getVoice() == voice) {
                    if (el instanceof NoteLayout note) {
                        if (filter.test(note)) {
                            return note;
                        }
                    } else if (!ignoreRests && el instanceof RestLayout) {
                        return null;
                    }
                }
            }
            current = current.getNext();
        }

        return null;
    }

    private NoteLayout findNextNoteInVoice(SegmentLayout startSegment, Staff staff, int voice, boolean ignoreRests) {
        return findNextNoteInVoice(startSegment, staff, voice, ignoreRests, note -> true);
    }

    private boolean isPitchWithinLedgerBounds(Pitch pitch, Clef clef, int maxLedgerLines) {
        ClefType clefType = clef.getType();
        int stepDifference = pitch.getAbsoluteDiatonicStep() - clefType.getDiatonicStep();
        double relativeYInSpaces = clefType.getOffsetY() - (stepDifference * 0.5);
        double minAllowedRelativeY = -maxLedgerLines;
        double maxAllowedRelativeY = 4.0 + maxLedgerLines;
        return relativeYInSpaces >= minAllowedRelativeY && relativeYInSpaces <= maxAllowedRelativeY;
    }

    private boolean isSegmentBefore(SegmentLayout seg1, SegmentLayout seg2) {
        if (seg1 == null || seg2 == null || seg1 == seg2) return false;
        SegmentLayout curr = seg1;
        while (curr != null) {
            if (curr == seg2) return true;
            curr = curr.getNext();
        }
        return false;
    }
}