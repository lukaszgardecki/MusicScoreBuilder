package org.example.musicscorebuilder.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.musicscorebuilder.ScoreService;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.NoteView;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.List;

public class ToolbarController {
    private final ScoreService scoreService = ScoreService.getInstance();
    private final ScoreStateManager stateManager = ScoreStateManager.getInstance();
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();
    private final NoteView noteView = new NoteView();

    @FXML private Button modeButton;
    @FXML private Button btn32, btn16, btn8, btn4, btn2, btn1;

    @FXML private Button voice1Button;
    @FXML private Button voice2Button;

    @FXML
    public void initialize() {
        modeManager.addModeChangeListener(isInsert -> {
            ObservableList<String> modeBtnClasses = modeButton.getStyleClass();
            ObservableList<String> voice1BtnClasses = voice1Button.getStyleClass();
            ObservableList<String> voice2BtnClasses = voice2Button.getStyleClass();

            voice1BtnClasses.remove("active");
            voice2BtnClasses.remove("active");

            if (isInsert) {
                if (!modeBtnClasses.contains("active")) modeBtnClasses.add("active");

                int voice = modeManager.getCurrentVoice();
                switch (voice) {
                    case 1 -> voice1BtnClasses.add("active");
                    case 2 -> voice2BtnClasses.add("active");
                }
            } else {
                modeBtnClasses.remove("active");
            }
        });

        stateManager.addSelectionChangeListener(selectedItem -> {
            NoteType type = switch (selectedItem) {
                case NoteLayout note -> note.getNote().getType();
                case RestLayout rest -> rest.getRest().getType();
                case null, default -> null;
            };
            updateDurationButtonStyles(type);
        });

        setupModeButton();
        setupButton(btn32, NoteType.THIRTY_SECOND);
        setupButton(btn16, NoteType.SIXTEENTH);
        setupButton(btn8, NoteType.EIGHTH);
        setupButton(btn4, NoteType.QUARTER);
        setupButton(btn2, NoteType.HALF);
        setupButton(btn1, NoteType.WHOLE);
        setupVoiceButton(voice1Button, 1);
        setupVoiceButton(voice2Button, 2);
    }

    @FXML
    private void toggleMode() {
        modeManager.toggleInsertMode();
    }

    @FXML
    private void activateVoice1() {
        modeManager.setCurrentVoice(1);
        modeManager.clearGhostNote();
        if (!modeManager.isInsertMode()) {
            modeManager.toggleInsertMode();
        } else {
            scoreNavigator.switchToVoice(1);
        }
        updateVoiceButtonStyles(1);
    }

    @FXML
    private void activateVoice2() {
        modeManager.setCurrentVoice(2);
        modeManager.clearGhostNote();
        if (!modeManager.isInsertMode()) {
            modeManager.toggleInsertMode();
        } else {
            scoreNavigator.switchToVoice(2);
        }
        updateVoiceButtonStyles(2);
    }

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

    private void setupModeButton() {
        if (modeButton == null) return;

        double width = 25;
        double height = 25;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setFont(Font.font("Segoe UI Emoji", 14));
        gc.fillText("✏", 4, 17);
        modeButton.setGraphic(canvas);
    }

    private void updateVoiceButtonStyles(int activeVoice) {
        voice1Button.getStyleClass().remove("active");
        voice2Button.getStyleClass().remove("active");
        if (activeVoice == 1) {
            voice1Button.getStyleClass().add("active");
        } else if (activeVoice == 2) {
            voice2Button.getStyleClass().add("active");
        }
    }

    private void updateDurationButtonStyles(NoteType selectedType) {
        List.of(btn32, btn16, btn8, btn4, btn2, btn1).forEach(b -> {
            if (b != null) b.getStyleClass().remove("active");
        });

        if (selectedType == null) return;

        Button targetButton = switch (selectedType) {
            case THIRTY_SECOND -> btn32;
            case SIXTEENTH -> btn16;
            case EIGHTH -> btn8;
            case QUARTER -> btn4;
            case HALF -> btn2;
            case WHOLE -> btn1;
            default -> null;
        };

        if (targetButton != null && !targetButton.getStyleClass().contains("active")) {
            targetButton.getStyleClass().add("active");
        }
    }

    private void setupButton(Button button, NoteType type) {
        if (button == null) return;

        double width = 25;
        double height = 25;
        double noteScale = 4.75;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().get(0);
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);
        Note dummyNote = new Note(1, PitchStep.F, 0, 4, type, BeamType.NONE, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset(centeredX / noteScale);

        noteView.draw(gc, noteLayout, 0.0, 0.0, noteScale);
        button.setGraphic(canvas);


        button.setOnAction(event -> {
            Selectable selected = stateManager.getSelectedItem();

            if (selected instanceof NoteLayout nl) {
                Measure measure = nl.getParent().getSegment().getParent();
                Staff staff = nl.getStaff().getStaff();
                measure.changeElementDuration(nl.getParent().getSegment(), staff, nl.getNote(), type);
                stateManager.notifyScoreChanged();

            } else if (selected instanceof RestLayout restLayout) {
                Measure measure = restLayout.getParent().getSegment().getParent();
                Staff staff = restLayout.getStaff().getStaff();
                measure.changeElementDuration(restLayout.getParent().getSegment(), staff, restLayout.getRest(), type);
                stateManager.notifyScoreChanged();
            }

            updateDurationButtonStyles(type);
        });
    }

    private void setupVoiceButton(Button button, int voiceNumber) {
        if (button == null) return;

        double width = 25;
        double height = 25;
        double noteScale = 4.75;
        var pitch = voiceNumber % 2 == 1 ? PitchStep.F : PitchStep.H;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().get(0);
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);
        Note dummyNote = new Note(voiceNumber, pitch, 0, 4, NoteType.QUARTER, BeamType.NONE, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);
        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        noteView.draw(gc, noteLayout, 0.0, 0.0, noteScale);

        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(voiceNumber), 10, 10);

        button.setGraphic(canvas);
    }
}
