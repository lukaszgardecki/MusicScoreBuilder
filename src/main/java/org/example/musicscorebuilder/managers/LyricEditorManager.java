package org.example.musicscorebuilder.managers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.layout.util.LyricStyleHelper;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.SyllableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LyricEditorManager {

    public interface CoordinateTransformer {
        double modelToViewX(double modelX);
        double modelToViewY(double modelY);
        double getScaleY();
        ScoreLayout getScoreLayout();
    }

    private static LyricEditorManager instance;

    private final StackPane editorContainer = new StackPane();
    private final TextFlow textDisplay = new TextFlow();
    private final TextField inputField = new TextField();

    private final Line customCaret = new Line();
    private final Timeline caretBlink = new Timeline(
            new KeyFrame(Duration.millis(500), e -> customCaret.setVisible(!customCaret.isVisible()))
    );

    private final LyricStyleHelper styleHelper = new LyricStyleHelper();
    private Pane containerPane;
    private CoordinateTransformer transformer;

    private NoteLayout currentNoteLayout;
    private int currentVerse = 1;
    private boolean isEditing = false;
    private boolean isNavigating = false;
    private boolean justStartedEditing = false;
    private boolean isLoadingLyric = false;
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

        Platform.runLater(() -> {
            ensureInputFieldAttached();

            Lyric lyric = targetModel.getLyric(verse);
            loadLyricIntoEditor(lyric);

            updatePosition();

            editorContainer.setVisible(true);
            editorContainer.toFront();

            inputField.requestFocus();

            if (clickX != null) {
                int caretIndex = !inputField.getText().isEmpty() ? calculateCaretIndex(inputField.getText(), clickX) : 0;
                inputField.positionCaret(caretIndex);
            } else {
                if (!inputField.getText().isEmpty()) {
                    inputField.selectAll();
                } else {
                    inputField.positionCaret(0);
                }
            }

            updateCustomCaretPosition();

            Platform.runLater(() -> this.justStartedEditing = false);
        });
    }

    public boolean isEditingNote(NoteLayout note, int verse) {
        if (!isEditing || currentNoteLayout == null || note == null) return false;
        if (currentVerse != verse) return false;
        Note currentModel = currentNoteLayout.getNote();
        Note targetModel = note.getNote();
        return currentModel != null && currentModel == targetModel;
    }

    private void loadLyricIntoEditor(Lyric lyric) {
        isLoadingLyric = true;
        try {
            StringBuilder sb = new StringBuilder();
            styleHelper.loadLyric(lyric, sb);
            inputField.setText(sb.toString());
        } finally {
            isLoadingLyric = false;
        }

        rebuildTextFlow();
    }

    private void setupInputField() {
        editorContainer.setVisible(false);
        editorContainer.setManaged(false);

        textDisplay.setTextAlignment(TextAlignment.CENTER);
        textDisplay.setMouseTransparent(true);

        customCaret.setStroke(Color.BLACK);
        customCaret.setStrokeWidth(2.0);
        customCaret.setMouseTransparent(true);
        caretBlink.setCycleCount(Animation.INDEFINITE);

        StackPane.setAlignment(textDisplay, Pos.CENTER);
        StackPane.setAlignment(inputField, Pos.CENTER);
        StackPane.setAlignment(customCaret, Pos.CENTER);

        inputField.setAlignment(Pos.CENTER);
        inputField.setStyle(
                "-fx-padding: 0; " +
                "-fx-background-insets: 0; " +
                "-fx-background-radius: 0; " +
                "-fx-border-radius: 0; " +
                "-fx-background-color: transparent; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 1.5px; " +
                "-fx-text-fill: transparent; " +
                "-fx-caret-color: transparent; " +
                "-fx-highlight-fill: #3390ff66; " +
                "-fx-highlight-text-fill: transparent;"
        );

        editorContainer.getChildren().addAll(textDisplay, inputField, customCaret);

        inputField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isLoadingLyric) {
                styleHelper.syncCharStyles(oldText, newText);
                if (isEditing && currentNoteLayout != null) {
                    Lyric existing = currentNoteLayout.getNote().getLyric(currentVerse);
                    SyllableType currentType = (existing != null && existing.getType() != null)
                            ? existing.getType()
                            : SyllableType.SINGLE;
                    commitCurrentText(currentType, false);
                    ScoreStateManager.getInstance().notifyScoreChanged();
                }
            }
            rebuildTextFlow();
            updatePosition();
            updateCustomCaretPosition();
        });

        inputField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (isNavigating) return;
            if (isFocused) {
                updateCustomCaretPosition();
            } else if (isEditing && !justStartedEditing) {
                commitAndHide();
            }
        });

        inputField.caretPositionProperty().addListener((obs, oldPos, newPos) -> {
            styleHelper.updateActiveStyleForCaret(newPos.intValue());
            updateCustomCaretPosition();
        });

        inputField.selectionProperty().addListener((obs, oldSel, newSel) ->
            updateCustomCaretPosition()
        );

        inputField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if ("-".equals(event.getCharacter()) || " ".equals(event.getCharacter())) {
                event.consume();
            }
        });

        inputField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.RIGHT) {
                if (inputField.getCaretPosition() == inputField.getText().length() && inputField.getSelection().getLength() == 0) {
                    if (findNextNoteLayout(currentNoteLayout) != null) {
                        event.consume();
                        commitAndNavigateNext();
                    }
                }
            } else if (event.getCode() == KeyCode.LEFT) {
                if (inputField.getCaretPosition() == 0 && inputField.getSelection().getLength() == 0) {
                    if (findPreviousNoteLayout(currentNoteLayout) != null) {
                        event.consume();
                        commitAndPrevious();
                    }
                }
            }
        });

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitAndHide();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hideEditor();
            } else if (event.isShortcutDown() || event.isControlDown()) {
                if (event.getCode() == KeyCode.B) {
                    event.consume();
                    toggleStyle(1);
                } else if (event.getCode() == KeyCode.I) {
                    event.consume();
                    toggleStyle(2);
                } else if (event.getCode() == KeyCode.U) {
                    event.consume();
                    toggleStyle(3);
                } else if (event.getCode() == KeyCode.SPACE) {
                    event.consume();
                    inputField.insertText(inputField.getCaretPosition(), " ");
                }
            } else if (event.getCode() == KeyCode.SPACE) {
                event.consume();
                SyllableType spaceType = isPreviousLyricConnected() ? SyllableType.END : SyllableType.SINGLE;
                commitAndNext(spaceType);
            } else if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
                event.consume();
                SyllableType hyphenType = isPreviousLyricConnected() ? SyllableType.MIDDLE : SyllableType.BEGIN;
                commitAndNext(hyphenType);
            }
        });
    }

    private void toggleStyle(int mode) {
        styleHelper.toggleStyle(mode, inputField.getSelection());
        rebuildTextFlow();
        updateCustomCaretPosition();
    }

    private boolean isPreviousLyricConnected() {
        if (currentNoteLayout == null) return false;
        NoteLayout prevLayout = findPreviousNoteLayout(currentNoteLayout);
        if (prevLayout != null && prevLayout.getNote() != null) {
            Lyric prevLyric = prevLayout.getNote().getLyric(currentVerse);
            if (prevLyric != null) {
                SyllableType type = prevLyric.getType();
                return type == SyllableType.BEGIN || type == SyllableType.MIDDLE;
            }
        }
        return false;
    }

    private void updateCustomCaretPosition() {
        if (!isEditing || inputField.getSelection().getLength() > 0) {
            caretBlink.stop();
            customCaret.setVisible(false);
            return;
        }

        String text = inputField.getText();
        if (text == null) text = "";
        int caretPos = Math.min(inputField.getCaretPosition(), text.length());

        double scaleY = getScaleY();
        double fontSize = (currentNoteLayout != null)
                ? scaleY * currentNoteLayout.getScoreStyle().getNoteLyricFontSize()
                : 12.0;

        Font baseFont = FontManager.getFreeSerifFont(fontSize);

        double totalWidth = styleHelper.measureWidth(text, 0, text.length(), baseFont);
        double subWidth = styleHelper.measureWidth(text, 0, caretPos, baseFont);

        double xOffset = -(totalWidth / 2.0) + subWidth;

        double caretHeight = fontSize * 1.1;
        customCaret.setStartY(-caretHeight / 2.0);
        customCaret.setEndY(caretHeight / 2.0);
        customCaret.setTranslateX(xOffset);

        customCaret.setVisible(true);
        caretBlink.playFromStart();
    }

    private void rebuildTextFlow() {
        textDisplay.getChildren().clear();
        if (currentNoteLayout == null) return;

        double scaleY = getScaleY();
        double fontSize = scaleY * currentNoteLayout.getScoreStyle().getNoteLyricFontSize();
        Font baseFont = FontManager.getFreeSerifFont(fontSize);

        inputField.setFont(baseFont);
        styleHelper.rebuildTextFlow(textDisplay, inputField.getText(), baseFont);
    }

    public void updatePosition() {
        if (!isEditing || currentNoteLayout == null) return;

        ScoreLayout layout = getScoreLayout();
        if (layout == null) return;

        ensureInputFieldAttached();
        rebuildTextFlow();

        LayoutHitTester.Point modelPos = LayoutHitTester.getLyricAbsolutePosition(layout, currentNoteLayout, currentVerse);
        double viewX = modelToViewX(modelPos.x());
        double viewY = modelToViewY(modelPos.y());

        double scaleY = getScaleY();
        double fontSize = scaleY * currentNoteLayout.getScoreStyle().getNoteLyricFontSize();

        Font baseFont = FontManager.getFreeSerifFont(fontSize);

        Text heightNode = new Text("Mg");
        heightNode.setFont(baseFont);
        double fieldHeight = heightNode.getLayoutBounds().getHeight() + 4.0;

        String text = inputField.getText();
        double exactTextWidth = (text == null || text.isEmpty())
                ? styleHelper.measureWidth("a", 0, 1, baseFont)
                : styleHelper.measureWidth(text, 0, text.length(), baseFont);
        double fieldWidth = Math.max(20.0, exactTextWidth + 8.0);
        double finalX = viewX - (fieldWidth / 2.0);

        editorContainer.resizeRelocate(finalX, viewY, fieldWidth, fieldHeight);
        updateCustomCaretPosition();
    }

    private void commitCurrentText(SyllableType type, boolean forceTypeChange) {
        if (currentNoteLayout == null || currentNoteLayout.getNote() == null) return;

        String rawText = inputField.getText();
        String text = (rawText != null) ? rawText.trim() : "";
        Note note = currentNoteLayout.getNote();

        if (!forceTypeChange && type == SyllableType.SINGLE && isPreviousLyricConnected()) {
            type = SyllableType.END;
        }

        boolean isConnectedType = (type == SyllableType.BEGIN || type == SyllableType.MIDDLE || type == SyllableType.END);
        Lyric resultingLyric = null;

        if (!text.isEmpty() || isConnectedType) {
            List<LyricFragment> fragments = styleHelper.exportToFragments(rawText);

            // KLUCZOWA POPRAWKA: Pusta lista fragmentów uniemożliwiała utworzenie LyricLayout.
            // Dla typów połączonych (np. END) musimy zachować przynajmniej pusty fragment, aby utworzyć layout do rysowania myślnika.
            if (fragments.isEmpty() && isConnectedType) {
                fragments.add(new LyricFragment());
            }

            Lyric lyric = note.getLyric(currentVerse);

            double activeEditorSize = currentNoteLayout.getScoreStyle().getNoteLyricFontSize();
            Double prevSize = getPreviousSyllableFontSize(currentVerse);
            Double fontSizeToSave = null;
            if (prevSize == null || Math.abs(activeEditorSize - prevSize) > 0.01) {
                fontSizeToSave = activeEditorSize;
            }

            if (lyric == null) {
                lyric = new Lyric(fragments, type, currentVerse, fontSizeToSave);
                note.setLyric(currentVerse, lyric);
            } else {
                lyric.setFragments(fragments);
                lyric.setFontSize(fontSizeToSave);
                if (forceTypeChange || lyric.getType() == null) {
                    lyric.setType(type);
                }
            }
            resultingLyric = lyric;
        } else {
            note.removeLyric(currentVerse);
        }

        updateNoteLayoutLyrics(currentNoteLayout, currentVerse, resultingLyric);
    }

    private void updateNoteLayoutLyrics(NoteLayout noteLayout, int verse, Lyric lyric) {
        if (noteLayout == null) return;
        List<LyricLayout> layouts = noteLayout.getLyrics();
        LyricLayout existing = null;
        for (LyricLayout l : layouts) {
            if (l.getVerse() == verse) {
                existing = l;
                break;
            }
        }

        if (lyric != null) {
            if (existing == null) {
                layouts.add(new LyricLayout(lyric, noteLayout));
            } else {
                existing.refresh();
            }
        } else {
            if (existing != null) {
                layouts.remove(existing);
            }
        }
    }

    private Double getPreviousSyllableFontSize(int verse) {
        if (currentNoteLayout == null) return null;

        NoteLayout prev = findPreviousNoteLayout(currentNoteLayout);
        while (prev != null) {
            Note prevNote = prev.getNote();
            if (prevNote != null) {
                Lyric prevLyric = prevNote.getLyric(verse);
                if (prevLyric != null && prevLyric.getFontSize() != null && prevLyric.getFontSize() > 0.0) {
                    return prevLyric.getFontSize();
                }
            }
            prev = findPreviousNoteLayout(prev);
        }
        return null;
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
        this.currentScoreLayout = null;

        caretBlink.stop();
        customCaret.setVisible(false);
        editorContainer.setVisible(false);

        if (wasEditing) {
            ModeManager.getInstance().toggleEditLyricsMode();
        }
    }

    private void ensureInputFieldAttached() {
        if (containerPane != null && !containerPane.getChildren().contains(editorContainer)) {
            containerPane.getChildren().add(editorContainer);
        }
    }

    private void attachMouseFilter() {
        if (containerPane != null) {
            containerPane.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (justStartedEditing || isNavigating) return;
                if (isEditing) {
                    if (editorContainer.getBoundsInParent().contains(event.getX(), event.getY())) {
                        return;
                    }
                    commitAndHide();
                }
            });
        }
    }

    private int calculateCaretIndex(String text, double clickX) {
        if (text == null || text.isEmpty() || currentNoteLayout == null) return 0;

        ScoreStyle style = currentNoteLayout.getScoreStyle();
        double scaleY = getScaleY();
        double fontSize = scaleY * style.getNoteLyricFontSize();
        Font actualFont = FontManager.getFreeSerifFont(fontSize);

        double totalWidth = styleHelper.measureWidth(text, 0, text.length(), actualFont);

        LayoutHitTester.Point modelPos = LayoutHitTester.getLyricAbsolutePosition(getScoreLayout(), currentNoteLayout, currentVerse);
        double viewX = modelToViewX(modelPos.x());
        double textLeftX = viewX - (totalWidth / 2.0);

        double clickOffset = clickX - textLeftX;
        if (clickOffset <= 0) return 0;
        if (clickOffset >= totalWidth) return text.length();

        int bestIndex = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i <= text.length(); i++) {
            double subWidth = styleHelper.measureWidth(text, 0, i, actualFont);
            double diff = Math.abs(subWidth - clickOffset);
            if (diff < minDiff) {
                minDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    private ScoreLayout getScoreLayout() {
        if (transformer != null && transformer.getScoreLayout() != null) {
            return transformer.getScoreLayout();
        }
        return currentScoreLayout;
    }

    private double modelToViewX(double modelX) { return (transformer != null) ? transformer.modelToViewX(modelX) : modelX; }
    private double modelToViewY(double modelY) { return (transformer != null) ? transformer.modelToViewY(modelY) : modelY; }
    private double getScaleY() { return (transformer != null) ? transformer.getScaleY() : 1.0; }

    private void navigateTo(NoteLayout targetNoteLayout, SyllableType commitType, boolean forceTypeChange) {
        if (!isEditing || currentNoteLayout == null) return;

        if (targetNoteLayout == null) {
            commitCurrentText(commitType, forceTypeChange);
            commitAndHide();
            return;
        }

        this.isNavigating = true;

        try {
            commitCurrentText(commitType, forceTypeChange);

            Note targetModel = targetNoteLayout.getNote();
            if (targetModel == null) return;

            this.currentNoteLayout = targetNoteLayout;

            Lyric lyric = targetModel.getLyric(currentVerse);
            boolean isConnecting = (commitType == SyllableType.BEGIN || commitType == SyllableType.MIDDLE);

            if (isConnecting) {
                List<LyricFragment> defaultFrags = new ArrayList<>();
                defaultFrags.add(new LyricFragment());

                if (lyric == null) {
                    lyric = new Lyric(defaultFrags, SyllableType.END, currentVerse, null);
                    targetModel.setLyric(currentVerse, lyric);
                } else {
                    lyric.setType(SyllableType.END);
                    if (lyric.getFragments() == null || lyric.getFragments().isEmpty()) {
                        lyric.setFragments(defaultFrags);
                    }
                }
                updateNoteLayoutLyrics(targetNoteLayout, currentVerse, lyric);
            }

            loadLyricIntoEditor(lyric);

            // Wymuś natychmiastowe przeliczenie widoku nut oraz myślników
            ScoreStateManager.getInstance().notifyScoreChanged();

            Platform.runLater(() -> {
                updatePosition();

                inputField.requestFocus();
                if (!inputField.getText().isEmpty()) {
                    inputField.selectAll();
                } else {
                    inputField.positionCaret(0);
                }
                updateCustomCaretPosition();
            });
        } finally {
            Platform.runLater(() -> this.isNavigating = false);
        }
    }

    private void commitAndNext(SyllableType type) {
        navigateTo(findNextNoteLayout(currentNoteLayout), type, true);
    }

    private void commitAndNavigateNext() {
        navigateTo(findNextNoteLayout(currentNoteLayout), SyllableType.SINGLE, false);
    }

    private void commitAndPrevious() {
        navigateTo(findPreviousNoteLayout(currentNoteLayout), SyllableType.SINGLE, false);
    }

    private List<NoteLayout> getScoreNoteLayouts() {
        ScoreLayout score = getScoreLayout();
        if (score == null) return Collections.emptyList();

        List<NoteLayout> list = new ArrayList<>();
        for (PageLayout page : score.getPages()) {
            if (page.getSystems() == null) continue;
            for (SystemLayout sys : page.getSystems()) {
                if (sys.getMeasures() == null) continue;
                for (MeasureLayout m : sys.getMeasures()) {
                    if (m.getSegments() == null) continue;
                    for (SegmentLayout seg : m.getSegments()) {
                        for (ElementLayout el : seg.getElements()) {
                            if (el instanceof NoteLayout nl && nl.getNote() != null) {
                                list.add(nl);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }

    private NoteLayout findNextNoteLayout(NoteLayout current) {
        if (current == null || current.getNote() == null) return null;
        List<NoteLayout> notes = getScoreNoteLayouts();
        int idx = notes.indexOf(current);
        if (idx != -1 && idx + 1 < notes.size()) {
            return notes.get(idx + 1);
        }
        return null;
    }

    private NoteLayout findPreviousNoteLayout(NoteLayout current) {
        if (current == null || current.getNote() == null) return null;
        List<NoteLayout> notes = getScoreNoteLayouts();
        int idx = notes.indexOf(current);
        if (idx > 0) {
            return notes.get(idx - 1);
        }
        return null;
    }
}