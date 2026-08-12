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
import javafx.scene.text.*;
import javafx.util.Duration;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Lyric;
import org.example.musicscorebuilder.components.music.LyricFragment;
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

    private static class CharStyle {
        boolean bold;
        boolean italic;
        boolean underline;

        CharStyle(boolean bold, boolean italic, boolean underline) {
            this.bold = bold;
            this.italic = italic;
            this.underline = underline;
        }

        boolean matches(CharStyle other) {
            return this.bold == other.bold && this.italic == other.italic && this.underline == other.underline;
        }
    }

    private static LyricEditorManager instance;

    private final StackPane editorContainer = new StackPane();
    private final TextFlow textDisplay = new TextFlow();
    private final TextField inputField = new TextField();

    private final Line customCaret = new Line();
    private final Timeline caretBlink = new Timeline(
            new KeyFrame(Duration.millis(500), e -> customCaret.setVisible(!customCaret.isVisible()))
    );

    private Pane containerPane;
    private CoordinateTransformer transformer;

    private NoteLayout currentNoteLayout;
    private int currentVerse = 1;
    private boolean isEditing = false;
    private boolean isNavigating = false;
    private boolean justStartedEditing = false;
    private boolean isLoadingLyric = false;
    private ScoreLayout currentScoreLayout;

    private final List<CharStyle> charStyles = new ArrayList<>();

    private boolean currentBold = false;
    private boolean currentItalic = false;
    private boolean currentUnderline = false;

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

            Platform.runLater(() -> {
                this.justStartedEditing = false;
            });
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
            charStyles.clear();
            StringBuilder sb = new StringBuilder();

            if (lyric != null && lyric.getFragments() != null && !lyric.getFragments().isEmpty()) {
                for (LyricFragment frag : lyric.getFragments()) {
                    sb.append(frag.getText());
                    for (int i = 0; i < frag.getText().length(); i++) {
                        charStyles.add(new CharStyle(frag.isBold(), frag.isItalic(), frag.isUnderline()));
                    }
                }
                if (!charStyles.isEmpty()) {
                    CharStyle last = charStyles.getLast();
                    currentBold = last.bold;
                    currentItalic = last.italic;
                    currentUnderline = last.underline;
                }
            } else if (lyric != null && lyric.getText() != null && !lyric.getText().isEmpty()) {
                sb.append(lyric.getText());
                for (int i = 0; i < lyric.getText().length(); i++) {
                    charStyles.add(new CharStyle(currentBold, currentItalic, currentUnderline));
                }
            }

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
                syncCharStyles(oldText, newText);
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
            int pos = newPos.intValue();
            if (pos > 0 && pos <= charStyles.size()) {
                CharStyle s = charStyles.get(pos - 1);
                currentBold = s.bold;
                currentItalic = s.italic;
                currentUnderline = s.underline;
            }
            Platform.runLater(this::updateCustomCaretPosition);
        });

        inputField.selectionProperty().addListener((obs, oldSel, newSel) -> {
            Platform.runLater(this::updateCustomCaretPosition);
        });

        inputField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if ("-".equals(event.getCharacter()) || " ".equals(event.getCharacter())) {
                event.consume();
            }
        });

        inputField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.RIGHT) {
                if (inputField.getCaretPosition() == inputField.getText().length() && inputField.getSelectedText().isEmpty()) {
                    if (findNextNoteLayout(currentNoteLayout) != null) {
                        event.consume();
                        commitAndNavigateNext();
                    }
                }
            } else if (event.getCode() == KeyCode.LEFT) {
                if (inputField.getCaretPosition() == 0 && inputField.getSelectedText().isEmpty()) {
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
                commitAndNext(SyllableType.SINGLE);
            } else if (event.getCode() == KeyCode.MINUS || event.getCode() == KeyCode.SUBTRACT) {
                event.consume();
                commitAndNext(SyllableType.BEGIN);
            }
        });
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

        double totalWidth = measureWidth(text, 0, text.length(), baseFont);
        double subWidth = measureWidth(text, 0, caretPos, baseFont);

        double xOffset = -(totalWidth / 2.0) + subWidth;

        double caretHeight = fontSize * 1.1;
        customCaret.setStartY(-caretHeight / 2.0);
        customCaret.setEndY(caretHeight / 2.0);
        customCaret.setTranslateX(xOffset);

        customCaret.setVisible(true);
        caretBlink.playFromStart();
    }

    private double measureWidth(String text, int start, int end, Font baseFont) {
        if (text == null || start >= end || start >= text.length()) return 0.0;
        int actualEnd = Math.min(end, text.length());

        double width = 0.0;
        int i = start;
        while (i < actualEnd) {
            CharStyle current = (i < charStyles.size())
                    ? charStyles.get(i)
                    : new CharStyle(currentBold, currentItalic, currentUnderline);

            int chunkEnd = i;
            while (chunkEnd < actualEnd && (chunkEnd < charStyles.size() ? charStyles.get(chunkEnd).matches(current) : true)) {
                chunkEnd++;
            }

            String chunk = text.substring(i, chunkEnd);
            Text t = new Text(chunk);
            FontWeight weight = current.bold ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = current.italic ? FontPosture.ITALIC : FontPosture.REGULAR;
            t.setFont(Font.font(baseFont.getFamily(), weight, posture, baseFont.getSize()));

            width += t.getLayoutBounds().getWidth();
            i = chunkEnd;
        }
        return width;
    }

    private void toggleStyle(int mode) {
        var selection = inputField.getSelection();
        if (selection.getLength() > 0) {
            for (int i = selection.getStart(); i < selection.getEnd(); i++) {
                if (i < charStyles.size()) {
                    CharStyle s = charStyles.get(i);
                    if (mode == 1) s.bold = !s.bold;
                    if (mode == 2) s.italic = !s.italic;
                    if (mode == 3) s.underline = !s.underline;
                }
            }
        } else {
            if (mode == 1) currentBold = !currentBold;
            if (mode == 2) currentItalic = !currentItalic;
            if (mode == 3) currentUnderline = !currentUnderline;
        }
        rebuildTextFlow();
        updateCustomCaretPosition();
    }

    private void syncCharStyles(String oldText, String newText) {
        if (oldText == null) oldText = "";
        if (newText == null) newText = "";

        int oldLen = oldText.length();
        int newLen = newText.length();

        int prefix = 0;
        while (prefix < oldLen && prefix < newLen && oldText.charAt(prefix) == newText.charAt(prefix)) {
            prefix++;
        }

        int suffix = 0;
        while (suffix < (oldLen - prefix) && suffix < (newLen - prefix)
                && oldText.charAt(oldLen - 1 - suffix) == newText.charAt(newLen - 1 - suffix)) {
            suffix++;
        }

        int removedCount = oldLen - prefix - suffix;
        int insertedCount = newLen - prefix - suffix;

        for (int i = 0; i < removedCount; i++) {
            if (prefix < charStyles.size()) {
                charStyles.remove(prefix);
            }
        }

        for (int i = 0; i < insertedCount; i++) {
            int insertPos = Math.min(prefix + i, charStyles.size());
            charStyles.add(insertPos, new CharStyle(currentBold, currentItalic, currentUnderline));
        }

        while (charStyles.size() > newLen) {
            charStyles.remove(charStyles.size() - 1);
        }
        while (charStyles.size() < newLen) {
            charStyles.add(new CharStyle(currentBold, currentItalic, currentUnderline));
        }
    }

    private void rebuildTextFlow() {
        textDisplay.getChildren().clear();
        if (currentNoteLayout == null) return;

        double scaleY = getScaleY();
        double fontSize = scaleY * currentNoteLayout.getScoreStyle().getNoteLyricFontSize();
        Font baseFont = FontManager.getFreeSerifFont(fontSize);

        inputField.setFont(baseFont);

        String text = inputField.getText();
        if (text == null || text.isEmpty()) return;

        int start = 0;
        while (start < text.length()) {
            CharStyle current = (start < charStyles.size())
                    ? charStyles.get(start)
                    : new CharStyle(currentBold, currentItalic, currentUnderline);

            int end = start;
            while (end < text.length() && (end < charStyles.size() ? charStyles.get(end).matches(current) : true)) {
                end++;
            }

            String chunk = text.substring(start, end);
            Text textNode = new Text(chunk);

            FontWeight weight = current.bold ? FontWeight.BOLD : FontWeight.NORMAL;
            FontPosture posture = current.italic ? FontPosture.ITALIC : FontPosture.REGULAR;

            textNode.setFont(Font.font(baseFont.getFamily(), weight, posture, fontSize));
            textNode.setUnderline(current.underline);
            textNode.setFill(Color.BLACK);

            textDisplay.getChildren().add(textNode);
            start = end;
        }
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
                ? measureWidth("a", 0, 1, baseFont)
                : measureWidth(text, 0, text.length(), baseFont);
        double fieldWidth = Math.max(20.0, exactTextWidth + 8.0);
        double finalX = viewX - (fieldWidth / 2.0);
        double finalY = viewY;

        editorContainer.resizeRelocate(finalX, finalY, fieldWidth, fieldHeight);
        updateCustomCaretPosition();
    }

    private void commitCurrentText(SyllableType type, boolean forceTypeChange) {
        if (currentNoteLayout == null || currentNoteLayout.getNote() == null) return;

        String text = inputField.getText().trim();
        Note note = currentNoteLayout.getNote();

        if (!text.isEmpty()) {
            List<LyricFragment> fragments = exportToFragments();
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
                if (forceTypeChange) lyric.setType(type);
            }
        } else {
            note.removeLyric(currentVerse);
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

    private List<LyricFragment> exportToFragments() {
        List<LyricFragment> result = new ArrayList<>();
        String text = inputField.getText();
        if (text == null || text.isEmpty()) return result;

        int start = 0;
        while (start < text.length()) {
            CharStyle current = (start < charStyles.size())
                    ? charStyles.get(start)
                    : new CharStyle(currentBold, currentItalic, currentUnderline);

            int end = start;
            while (end < text.length()) {
                CharStyle styleAtEnd = (end < charStyles.size())
                        ? charStyles.get(end)
                        : new CharStyle(currentBold, currentItalic, currentUnderline);
                if (!styleAtEnd.matches(current)) {
                    break;
                }
                end++;
            }
            result.add(new LyricFragment(text.substring(start, end), current.bold, current.italic, current.underline));
            start = end;
        }
        return result;
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
                if (justStartedEditing || isNavigating) {
                    return;
                }
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

        double totalWidth = measureWidth(text, 0, text.length(), actualFont);

        LayoutHitTester.Point modelPos = LayoutHitTester.getLyricAbsolutePosition(getScoreLayout(), currentNoteLayout, currentVerse);
        double viewX = modelToViewX(modelPos.x());
        double textLeftX = viewX - (totalWidth / 2.0);

        double clickOffset = clickX - textLeftX;
        if (clickOffset <= 0) return 0;
        if (clickOffset >= totalWidth) return text.length();

        int bestIndex = 0;
        double minDiff = Double.MAX_VALUE;

        for (int i = 0; i <= text.length(); i++) {
            double subWidth = measureWidth(text, 0, i, actualFont);
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
            loadLyricIntoEditor(lyric);

            ScoreStateManager.getInstance().notifyScoreChanged();

            updatePosition();

            inputField.requestFocus();
            if (!inputField.getText().isEmpty()) {
                inputField.selectAll();
            } else {
                inputField.positionCaret(0);
            }
            updateCustomCaretPosition();
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

    private NoteLayout findNextNoteLayout(NoteLayout current) {
        if (current == null || current.getNote() == null || current.getSegment() == null) return null;

        int targetVoice = current.getNote().getVoice();
        int targetStaff = current.getStaff().getStaffIndex();

        SegmentLayout segmentNode = current.getSegment().getNextSameType();

        while (segmentNode != null) {
            for (ElementLayout element : segmentNode.getElements()) {
                if (element instanceof NoteLayout nextNote) {
                    if (nextNote.getNote() != null
                            && nextNote.getNote().getVoice() == targetVoice
                            && nextNote.getStaff().getStaffIndex() == targetStaff) {
                        return nextNote;
                    }
                }
            }
            segmentNode = segmentNode.getNextSameType();
        }
        return null;
    }

    private NoteLayout findPreviousNoteLayout(NoteLayout current) {
        if (current == null || current.getNote() == null || current.getSegment() == null) return null;

        int targetVoice = current.getNote().getVoice();
        int targetStaff = current.getStaff().getStaffIndex();

        SegmentLayout segmentNode = current.getSegment().getPrevSameType();

        while (segmentNode != null) {
            for (ElementLayout element : segmentNode.getElements()) {
                if (element instanceof NoteLayout prevNote) {
                    if (prevNote.getNote() != null
                            && prevNote.getNote().getVoice() == targetVoice
                            && prevNote.getStaff().getStaffIndex() == targetStaff) {
                        return prevNote;
                    }
                }
            }
            segmentNode = segmentNode.getPrevSameType();
        }
        return null;
    }
}