package org.example.musicscorebuilder.managers;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.SyllableType;

import java.util.ArrayList;
import java.util.List;

public class LyricEditorManager {

    public interface CoordinateTransformer {
        double modelToViewX(double modelX);
        double modelToViewY(double modelY);
        double getScaleY();
        ScoreLayout getScoreLayout();
    }

    private static LyricEditorManager instance;

    private final TextField inputField = new TextField();
    private Pane containerPane;
    private CoordinateTransformer transformer;

    private NoteLayout currentNoteLayout;
    private int currentVerse = 1;
    private boolean isEditing = false;
    private boolean justStartedEditing = false;
    private ScoreLayout currentScoreLayout;

    private LyricEditorManager() {
        setupInputField();
    }

    public static synchronized LyricEditorManager getInstance() {
        if (instance == null) {
            instance = new LyricEditorManager();
        }
        return instance;
    }

    public void init(Pane containerPane, CoordinateTransformer transformer) {
        this.containerPane = containerPane;
        this.transformer = transformer;
        attachMouseFilter();
        ensureInputFieldAttached();
    }

    public void startEditing(NoteLayout noteLayout, int verse) {
        startEditing(noteLayout, verse, getScoreLayout(), null);
    }

    public void startEditing(NoteLayout noteLayout, int verse, Double clickX) {
        startEditing(noteLayout, verse, getScoreLayout(), clickX);
    }

    public void startEditing(NoteLayout noteLayout, int verse, ScoreLayout scoreLayout) {
        startEditing(noteLayout, verse, scoreLayout, null);
    }

    public void startEditing(NoteLayout noteLayout, int verse, ScoreLayout scoreLayout, Double clickX) {
        if (noteLayout == null || noteLayout.getNote() == null) return;

        Note targetModel = noteLayout.getNote();

        this.isEditing = true;
        this.justStartedEditing = true;
        this.currentVerse = verse;
        this.currentNoteLayout = noteLayout;
        this.currentScoreLayout = (scoreLayout != null) ? scoreLayout : getScoreLayout();

        ModeManager.getInstance().toggleEditLyricsMode();
        ScoreStateManager.getInstance().notifyScoreChanged();

        Platform.runLater(() -> {
            NoteLayout freshNote = findFreshNoteLayout(targetModel);
            if (freshNote != null) {
                this.currentNoteLayout = freshNote;
            }

            ensureInputFieldAttached();

            Lyric lyric = targetModel.getLyric(verse);
            String existingText = (lyric != null && lyric.getText() != null) ? lyric.getText() : "";

            inputField.setText(existingText);

            updatePosition();

            inputField.setVisible(true);
            inputField.toFront();
            inputField.requestFocus();
            applyCaretThickness();

            if (clickX != null && !existingText.isEmpty()) {
                int caretIndex = calculateCaretIndex(existingText, clickX);
                inputField.positionCaret(caretIndex);
            } else {
                inputField.positionCaret(existingText.length());
            }
            inputField.deselect();
        });
    }

    private int calculateCaretIndex(String text, double clickX) {
        if (text == null || text.isEmpty() || currentNoteLayout == null) return 0;

        ScoreStyle style = currentNoteLayout.getScoreStyle();
        double scaleY = getScaleY();
        double fontSize = scaleY * style.getNoteLyricFontSize();
        Font actualFont = FontManager.getFreeSerifFont(fontSize);

        Text textNode = new Text(text);
        textNode.setFont(actualFont);
        double totalWidth = textNode.getLayoutBounds().getWidth();

        LayoutHitTester.Point modelPos = LayoutHitTester.getLyricAbsolutePosition(getScoreLayout(), currentNoteLayout, currentVerse);
        double viewX = modelToViewX(modelPos.x());
        double textLeftX = viewX - (totalWidth / 2.0);

        double clickOffset = clickX - textLeftX;
        if (clickOffset <= 0) return 0;
        if (clickOffset >= totalWidth) return text.length();

        int bestIndex = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i <= text.length(); i++) {
            String sub = text.substring(0, i);
            Text subNode = new Text(sub);
            subNode.setFont(actualFont);
            double subWidth = subNode.getLayoutBounds().getWidth();

            double diff = Math.abs(subWidth - clickOffset);
            if (diff < minDiff) {
                minDiff = diff;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    public void updatePosition() {
        if (!isEditing || currentNoteLayout == null) return;

        ScoreLayout layout = getScoreLayout();
        if (layout == null) return;

        ensureInputFieldAttached();

        LayoutHitTester.Point modelPos = LayoutHitTester.getLyricAbsolutePosition(layout, currentNoteLayout, currentVerse);
        double viewX = modelToViewX(modelPos.x());
        double viewY = modelToViewY(modelPos.y());

        ScoreStyle style = currentNoteLayout.getScoreStyle();
        double scaleY = getScaleY();
        double fontSize = scaleY * style.getNoteLyricFontSize();
        Font actualFont = FontManager.getFreeSerifFont(fontSize);
        inputField.setFont(actualFont);

        String textToMeasure = inputField.getText().isEmpty() ? "a" : inputField.getText();
        Text textNode = new Text(textToMeasure);
        textNode.setFont(actualFont);

        double exactTextWidth = textNode.getLayoutBounds().getWidth();
        double fieldHeight = textNode.getLayoutBounds().getHeight();

        double fieldWidth = exactTextWidth + scaleY + 6.0;

        double finalX = viewX - (fieldWidth / 2.0);
        double finalY = viewY;

        inputField.resizeRelocate(finalX, finalY, fieldWidth, fieldHeight);
    }

    public boolean isEditingNote(NoteLayout note, int verse) {
        if (!isEditing || currentNoteLayout == null || note == null) return false;
        if (currentVerse != verse) return false;
        Note currentModel = currentNoteLayout.getNote();
        Note targetModel = note.getNote();
        return currentModel != null && currentModel == targetModel;
    }

    private void commitAndHide() {
        if (!isEditing) return;
        commitCurrentText(SyllableType.SINGLE, false);
        hideEditor();
        ScoreStateManager.getInstance().notifyScoreChanged();
    }

    private void hideEditor() {
        boolean wasEditing = this.isEditing;
        this.isEditing = false;
        this.justStartedEditing = false;
        this.currentNoteLayout = null;
        inputField.setVisible(false);

        if (wasEditing) {
            ModeManager.getInstance().toggleEditLyricsMode();
        }
    }

    private void setupInputField() {
        inputField.setVisible(false);
        inputField.setManaged(false);

        inputField.setAlignment(Pos.CENTER);
        inputField.setStyle(
                "-fx-padding: 0 3px 0 3px; " +
                "-fx-background-insets: 0; " +
                "-fx-background-radius: 0; " +
                "-fx-border-radius: 0; " +
                "-fx-background-color: #f8f8f8; " +
                "-fx-border-color: #9e9e9e; " +
                "-fx-border-width: 1px; " +
                "-fx-alignment: center; " +
                "-fx-focus-color: transparent; " +
                "-fx-faint-focus-color: transparent; " +
                "-fx-text-fill: black;"
        );

        inputField.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                Platform.runLater(this::applyCaretThickness);
            }
        });

        inputField.textProperty().addListener((obs, oldText, newText) -> updatePosition());

        inputField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isFocused) {
                applyCaretThickness();
            } else if (isEditing && !justStartedEditing) {
                commitAndHide();
            }
        });

        inputField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if ("-".equals(event.getCharacter()) || " ".equals(event.getCharacter())) {
                event.consume();
            }
        });

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitAndHide();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hideEditor();
            } else if (event.getCode() == KeyCode.SPACE) {
                event.consume();

                if (event.isShortcutDown() || event.isControlDown()) {
                    inputField.insertText(inputField.getCaretPosition(), " ");
                } else {
                    commitAndNext(SyllableType.SINGLE);
                }
            } else if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
                event.consume();
                commitAndNext(SyllableType.BEGIN);
            }
        });
    }

    private void attachMouseFilter() {
        if (containerPane != null) {
            containerPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (justStartedEditing) {
                    justStartedEditing = false;
                    return;
                }
                if (isEditing) {
                    if (inputField.getBoundsInParent().contains(event.getX(), event.getY())) {
                        return;
                    }
                    commitAndHide();
                }
            });
        }
    }

    private void applyCaretThickness() {
        Node caretNode = inputField.lookup(".caret");
        if (caretNode instanceof Path caretPath) {
            caretPath.setStroke(Color.BLACK);
            caretPath.setStrokeWidth(3.0);
        }
    }

    private void ensureInputFieldAttached() {
        if (containerPane != null && !containerPane.getChildren().contains(inputField)) {
            containerPane.getChildren().add(inputField);
        }
    }

    private ScoreLayout getScoreLayout() {
        if (currentScoreLayout != null) return currentScoreLayout;
        return (transformer != null) ? transformer.getScoreLayout() : null;
    }

    private double modelToViewX(double modelX) {
        return (transformer != null) ? transformer.modelToViewX(modelX) : modelX;
    }

    private double modelToViewY(double modelY) {
        return (transformer != null) ? transformer.modelToViewY(modelY) : modelY;
    }

    private double getScaleY() {
        return (transformer != null) ? transformer.getScaleY() : 1.0;
    }

    private void commitAndNext(SyllableType type) {
        if (!isEditing || currentNoteLayout == null) return;

        commitCurrentText(type, true);

        NoteLayout nextNote = findNextNoteLayout(currentNoteLayout);
        ScoreLayout savedScoreLayout = getScoreLayout();

        if (nextNote != null) {
            Note targetNextModel = nextNote.getNote();

            hideEditor();
            ScoreStateManager.getInstance().notifyScoreChanged();

            Platform.runLater(() -> {
                NoteLayout freshNextNote = findFreshNoteLayout(targetNextModel);
                if (freshNextNote != null) {
                    startEditing(freshNextNote, currentVerse, savedScoreLayout);
                }
            });
        } else {
            commitAndHide();
        }
    }

    private void commitCurrentText(SyllableType type, boolean forceTypeChange) {
        if (currentNoteLayout == null || currentNoteLayout.getNote() == null) return;

        String text = inputField.getText().trim();
        Note note = currentNoteLayout.getNote();

        if (!text.isEmpty()) {
            Lyric lyric = note.getLyric(currentVerse);
            if (lyric == null) {
                lyric = new Lyric(text, type, currentVerse, currentNoteLayout.getScoreStyle().getNoteLyricFontSize());
                note.setLyric(currentVerse, lyric);
            } else {
                lyric.setText(text);
                if (forceTypeChange) {
                    lyric.setType(type);
                }
            }
        } else {
            note.removeLyric(currentVerse);
        }
    }

    private NoteLayout findNextNoteLayout(NoteLayout current) {
        List<NoteLayout> allNotes = getAllNotesInScore();
        int idx = allNotes.indexOf(current);
        if (idx != -1 && idx + 1 < allNotes.size()) {
            return allNotes.get(idx + 1);
        }
        return null;
    }

    private List<NoteLayout> getAllNotesInScore() {
        List<NoteLayout> notes = new ArrayList<>();
        ScoreLayout layout = getScoreLayout();
        if (layout == null) return notes;

        for (LayoutHitTester.PositionedNote pn : LayoutHitTester.getAllPositionedNotes(layout.getPages())) {
            notes.add(pn.noteLayout());
        }
        return notes;
    }

    private NoteLayout findFreshNoteLayout(Note targetNote) {
        if (targetNote == null) return null;
        for (NoteLayout nl : getAllNotesInScore()) {
            if (nl.getNote() == targetNote) {
                return nl;
            }
        }
        return null;
    }
}