package org.example.musicscorebuilder.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.controller.util.ToolbarIconRenderer;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.Map;

public class ToolbarController {
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();
    private final ToolbarIconRenderer iconRenderer = new ToolbarIconRenderer();

    @FXML private Button modeButton;
    @FXML private Button btn32, btn16, btn8, btn4, btn2, btn1;
    @FXML private Button btnDot, btnRest, btnTie;
    @FXML private Button btnDoubleFlat, btnFlat, btnNatural, btnSharp, btnDoubleSharp;
    @FXML private Button voice1Button, voice2Button, voice3Button, voice4Button;

    private Map<NoteType, Button> durationButtons;
    private Map<Integer, Button> accidentalButtons;

    @FXML
    public void initialize() {
        initDurationButtonsMap();
        initAccidentalButtonsMap();
        registerListeners();
        setupToolbarUI();
    }

    // ----------------------------------------------------
    // ----------------- FXML Actions ---------------------
    // ----------------------------------------------------

    @FXML
    private void toggleMode() {
        modeManager.toggleInsertMode();
    }

    @FXML
    private void toggleDot() {
        if (modeManager.isInsertMode()) {
            modeManager.toggleDot();
        } else {
            Selectable selected = stateManager.getSelectedItem();
            if (selected instanceof NoteLayout noteLayout) {
                int currentDots = noteLayout.getNote().getDots();
                stateManager.changeSelectedElementDots(currentDots > 0 ? 0 : 1);
            } else if (selected instanceof RestLayout restLayout) {
                int currentDots = restLayout.getRest().getDots();
                stateManager.changeSelectedElementDots(currentDots > 0 ? 0 : 1);
            }
        }
        updateDotButtonState();
    }

    @FXML
    private void createRest() {
        if (modeManager.isInsertMode()) {

        } else {
            stateManager.convertSelectedNoteToRest();
        }
        updateRestButtonState();
    }

    @FXML private void toggleDoubleFlat()  { toggleAccidental(-2); }
    @FXML private void toggleFlat()        { toggleAccidental(-1); }
    @FXML private void toggleNatural()     { toggleAccidental(0); }
    @FXML private void toggleSharp()       { toggleAccidental(1); }
    @FXML private void toggleDoubleSharp() { toggleAccidental(2); }

    @FXML
    private void addTie() {
        if (modeManager.isInsertMode()) {

        } else {
            stateManager.toggleTieForSelectedNote();
        }
        updateTieButtonState();
    }

    @FXML private void activateVoice1() { activateVoice(1); }
    @FXML private void activateVoice2() { activateVoice(2); }
    @FXML private void activateVoice3() { activateVoice(3); }
    @FXML private void activateVoice4() { activateVoice(4); }

    @FXML
    private void addMeasure() {
        scoreService.getScore().addNewMeasure();
        stateManager.notifyScoreChanged();
    }

    @FXML
    private void removeMeasure() {
        scoreService.getScore().removeLastMeasure();
        stateManager.notifyScoreChanged();
    }

    // ----------------------------------------------------
    // ------------ Initialization & Setup ----------------
    // ----------------------------------------------------

    private void initDurationButtonsMap() {
        durationButtons = Map.of(
                NoteType.THIRTY_SECOND, btn32,
                NoteType.SIXTEENTH, btn16,
                NoteType.EIGHTH, btn8,
                NoteType.QUARTER, btn4,
                NoteType.HALF, btn2,
                NoteType.WHOLE, btn1
        );
    }

    private void initAccidentalButtonsMap() {
        accidentalButtons = Map.of(
                -2, btnDoubleFlat,
                -1, btnFlat,
                0, btnNatural,
                1, btnSharp,
                2, btnDoubleSharp
        );
    }

    private void registerListeners() {
        modeManager.addModeChangeListener(this::handleModeChange);
        modeManager.addNoteTypeChangeListener(this::handleNoteTypeChange);
        stateManager.addSelectionChangeListener(this::handleSelectionChange);
        stateManager.addScoreChangeListener(this::handleScoreChange);
    }

    private void setupToolbarUI() {
        iconRenderer.renderModeIcon(modeButton);
        iconRenderer.renderNoteDottedIcon(btnDot);
        iconRenderer.renderRestIcon(btnRest);
        iconRenderer.renderAccidentalIcon(btnDoubleFlat, -2);
        iconRenderer.renderAccidentalIcon(btnFlat, -1);
        iconRenderer.renderAccidentalIcon(btnNatural, 0);
        iconRenderer.renderAccidentalIcon(btnSharp, 1);
        iconRenderer.renderAccidentalIcon(btnDoubleSharp, 2);
        iconRenderer.renderTieIcon(btnTie);
        iconRenderer.renderVoiceIcon(voice1Button, 1);
        iconRenderer.renderVoiceIcon(voice2Button, 2);
        iconRenderer.renderVoiceIcon(voice3Button, 3);
        iconRenderer.renderVoiceIcon(voice4Button, 4);

        durationButtons.forEach((type, button) -> {
            if (button != null) {
                iconRenderer.renderNoteTypeIcon(button, type, false);
                button.setOnAction(event -> handleDurationButtonClick(type));
            }
        });

        updateDotButtonState();
        updateRestButtonState();
        updateTieButtonState();
        updateAccidentalButtonState();
    }

    // ----------------------------------------------------
    // ----------------- Event Handlers -------------------
    // ----------------------------------------------------

    private void handleModeChange(boolean isInsert) {
        updateModeAndVoiceButtons(isInsert);

        NoteType typeToHighlight = isInsert
                ? modeManager.getCurrentNoteType()
                : extractNoteType(stateManager.getSelectedItem());

        updateDurationButtonStyles(typeToHighlight);
        updateDotButtonState();
        updateRestButtonState();
        updateTieButtonState();
    }

    private void handleNoteTypeChange(NoteType noteType) {
        if (modeManager.isInsertMode()) {
            updateDurationButtonStyles(noteType);
            updateDotButtonState();
            updateRestButtonState();
            updateTieButtonState();
            updateAccidentalButtonState();
        }
    }

    private void handleSelectionChange(Selectable selectedItem) {
        if (!modeManager.isInsertMode()) {
            updateDurationButtonStyles(extractNoteType(selectedItem));
            updateDotButtonState();
            updateRestButtonState();
            updateTieButtonState();
            updateAccidentalButtonState();
        }
    }

    private void handleScoreChange() {
        if (!modeManager.isInsertMode()) {
            Selectable selected = stateManager.getSelectedItem();
            updateDurationButtonStyles(extractNoteType(selected));
            updateDotButtonState();
            updateRestButtonState();
            updateTieButtonState();
            updateAccidentalButtonState();
        }
    }

    private void handleDurationButtonClick(NoteType type) {
        if (modeManager.isInsertMode()) {
            modeManager.setCurrentNoteType(type);
        } else {
            stateManager.changeSelectedElementDuration(type);
        }
        updateDurationButtonStyles(type);
    }

    // ----------------------------------------------------
    // ----------------- Helper Methods -------------------
    // ----------------------------------------------------

    private void updateDotButtonState() {
        boolean isDotted;

        if (modeManager.isInsertMode()) {
            isDotted = modeManager.isDotted();
        } else {
            Selectable selected = stateManager.getSelectedItem();
            isDotted = switch (selected) {
                case NoteLayout note -> note.getNote().isDotted();
                case RestLayout rest -> rest.getRest().isDotted();
                case null, default -> false;
            };
        }

        setButtonActive(btnDot, isDotted);
    }

    private void updateRestButtonState() {
        boolean isRest;

        if (modeManager.isInsertMode()) {
            isRest = false;
        } else {
            Selectable selected = stateManager.getSelectedItem();
            isRest = selected instanceof RestLayout;
        }

        setButtonActive(btnRest, isRest);
    }

    private void updateTieButtonState() {
        boolean isTied;

        if (modeManager.isInsertMode()) {
            isTied = false;
        } else {
            Selectable selected = stateManager.getSelectedItem();
            isTied = switch (selected) {
                case NoteLayout noteLayout -> noteLayout.getNote().hasTie();
                case null, default -> false;
            };
        }

        setButtonActive(btnTie, isTied);
    }

    private void updateAccidentalButtonState() {
        Integer activeAlter = null;

        if (!modeManager.isInsertMode()) {
            Selectable selected = stateManager.getSelectedItem();
            if (selected instanceof NoteLayout noteLayout) {
                AccidentalLayout acc = noteLayout.getAccidental();
                if (acc != null && acc.isVisible()) {
                    activeAlter = noteLayout.getNote().getPitch().getAlter();
                }
            } else if (selected instanceof AccidentalLayout accLayout && accLayout.isVisible()) {
                String code = accLayout.getCode();
                if (Leland.ACC_DOUBLE_FLAT.getCode().equals(code)) activeAlter = -2;
                else if (Leland.ACC_FLAT.getCode().equals(code)) activeAlter = -1;
                else if (Leland.ACC_NATURAL.getCode().equals(code)) activeAlter = 0;
                else if (Leland.ACC_SHARP.getCode().equals(code)) activeAlter = 1;
                else if (Leland.ACC_DOUBLE_SHARP.getCode().equals(code)) activeAlter = 2;
            }
        }

        final Integer selectedAlter = activeAlter;
        accidentalButtons.forEach((alter, button) ->
                setButtonActive(button, selectedAlter != null && selectedAlter.equals(alter))
        );
    }

    private void activateVoice(int voiceNumber) {
        modeManager.setCurrentVoice(voiceNumber);
        modeManager.clearGhostNote();
        if (!modeManager.isInsertMode()) {
            modeManager.toggleInsertMode();
        } else {
            scoreNavigator.switchToVoice(voiceNumber);
        }
        updateModeAndVoiceButtons(modeManager.isInsertMode());
    }

    private void updateModeAndVoiceButtons(boolean isInsert) {
        setButtonActive(modeButton, isInsert);
        int activeVoice = isInsert ? modeManager.getCurrentVoice() : 0;
        setButtonActive(voice1Button, activeVoice == 1);
        setButtonActive(voice2Button, activeVoice == 2);
        setButtonActive(voice3Button, activeVoice == 3);
        setButtonActive(voice4Button, activeVoice == 4);
    }

    private void updateDurationButtonStyles(NoteType selectedType) {
        durationButtons.forEach((type, button) -> setButtonActive(button, type == selectedType));
    }

    private NoteType extractNoteType(Selectable item) {
        return switch (item) {
            case NoteLayout note -> note.getNote().getType();
            case RestLayout rest -> rest.getRest().getType();
            case null, default -> null;
        };
    }

    private void setButtonActive(Button button, boolean active) {
        if (button == null) return;
        ObservableList<String> classes = button.getStyleClass();
        if (active) {
            if (!classes.contains("active")) classes.add("active");
        } else {
            classes.remove("active");
        }
    }

    private void toggleAccidental(int targetAlter) {
        if (modeManager.isInsertMode()) return;

        NoteLayout noteLayout = switch (stateManager.getSelectedItem()) {
            case NoteLayout nl -> nl;
            case AccidentalLayout al -> al.getSegment().getElements().stream()
                    .filter(NoteLayout.class::isInstance)
                    .map(NoteLayout.class::cast)
                    .filter(n -> n.getAccidental() == al)
                    .findFirst()
                    .orElse(null);
            case null, default -> null;
        };

        if (noteLayout == null || noteLayout.getNote() == null) return;
        Pitch pitch = noteLayout.getNote().getPitch();
        if (pitch == null) return;

        SegmentLayout targetSegLayout = noteLayout.getParent();
        Measure measure = (targetSegLayout != null && targetSegLayout.getSegment() != null)
                ? targetSegLayout.getSegment().getParent()
                : null;

        int oldAlter = pitch.getAlter();
        int keyAlter = (measure != null) ? measure.getKeySignatureAlterForStep(pitch.getStep()) : 0;
        int newAlter = (oldAlter == targetAlter) ? keyAlter : targetAlter;

        pitch.setAlter(newAlter);
        noteLayout.refreshMeasureAccidentals();
        stateManager.notifyScoreChanged();
    }
}