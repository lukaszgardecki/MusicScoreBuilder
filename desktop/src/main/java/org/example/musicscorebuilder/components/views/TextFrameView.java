package org.example.musicscorebuilder.components.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.musicscorebuilder.components.dialog.CustomVerseEditDialog;
import org.example.musicscorebuilder.components.frames.TextFrameLayout;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.Verse;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;
import org.example.musicscorebuilder.components.music.frames.TextLine;
import org.example.musicscorebuilder.components.music.frames.VerseTextMapper;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.Objects;

public class TextFrameView extends ScrollPane {
    private int lastHash = Integer.MIN_VALUE;
    private final Runnable onRequestDraw;

    public TextFrameView(Runnable onRequestDraw) {
        this.onRequestDraw = onRequestDraw;
        setHbarPolicy(ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setFocusTraversable(false);
    }

    public void update(TextFrameLayout textFrame, double screenX, double screenY, double frameW, double frameH, double sp, int selectedVerseNum) {
        setLayoutX(screenX);
        setLayoutY(screenY);
        setPrefSize(frameW, frameH);
        setMinSize(frameW, frameH);
        setMaxSize(frameW, frameH);

        int currentHash = calculateFrameHash(textFrame, sp);

        if (getContent() instanceof VBox existingBox && currentHash == lastHash) {
            updateVerseHighlights(existingBox, selectedVerseNum, textFrame, sp);
        } else {
            double savedV = getVvalue();
            VBox contentBox = buildVersesContent(textFrame, sp, selectedVerseNum);
            setContent(contentBox);
            lastHash = currentHash;
            setVvalue(savedV);
        }
    }

    private int calculateFrameHash(TextFrameLayout frame, double sp) {
        int verseHash = 0;
        if (frame.getVerses() != null) {
            for (TextFrameVerse verse : frame.getVerses()) {
                int lineHash = 0;
                if (verse.getLines() != null) {
                    for (TextLine line : verse.getLines()) {
                        int fragHash = 0;
                        if (line.getFragments() != null) {
                            for (LyricFragment f : line.getFragments()) {
                                fragHash = Objects.hash(fragHash, f.getText(), f.isBold(), f.isItalic(), f.isUnderline());
                            }
                        }
                        lineHash = Objects.hash(lineHash, line.getFontSize(), fragHash);
                    }
                }
                verseHash = Objects.hash(verseHash, verse.getNumber(), lineHash);
            }
        }

        return Objects.hash(
                sp,
                frame.getVerseSpacing(),
                frame.getPadding(),
                frame.getVerseFontSize(),
                frame.getVersePaddingX(),
                frame.getVersePaddingY(),
                frame.getVerseCornerRadius(),
                verseHash
        );
    }

    private void updateVerseHighlights(VBox versesBox, int selectedVerseNum, TextFrameLayout frame, double sp) {
        double radius = frame.getVerseCornerRadius() * sp;
        for (var node : versesBox.getChildren()) {
            if (node instanceof Region box && box.getUserData() instanceof Integer vNum) {
                String bg = (vNum == selectedVerseNum) ? "#c8def8" : "transparent";
                box.setStyle(String.format(
                        java.util.Locale.US,
                        "-fx-background-color: %s; -fx-background-radius: %.1fpx; -fx-cursor: hand;",
                        bg, radius
                ));
            }
        }
    }

    private VBox buildVersesContent(TextFrameLayout frame, double sp, int selectedVerseNum) {
        VBox versesBox = new VBox();
        versesBox.setSpacing(frame.getVerseSpacing() * sp);
        versesBox.setPadding(new Insets(frame.getPadding() * sp));

        double defaultFontSizePx = frame.getVerseFontSize() * sp;
        double padX = frame.getVersePaddingX() * sp;
        double padY = frame.getVersePaddingY() * sp;
        double radius = frame.getVerseCornerRadius() * sp;

        for (TextFrameVerse verse : frame.getVerses()) {
            StackPane verseContainer = new StackPane();
            verseContainer.setUserData(verse.getNumber());
            verseContainer.setFocusTraversable(false);

            String bgColor = (verse.getNumber() == selectedVerseNum) ? "#c8def8" : "transparent";
            verseContainer.setStyle(String.format(
                    java.util.Locale.US,
                    "-fx-background-color: %s; -fx-background-radius: %.1fpx; -fx-cursor: hand;",
                    bgColor, radius
            ));

            VBox linesBox = new VBox();
            linesBox.setPadding(new Insets(padY, padX + 22.0, padY, padX));

            for (TextLine line : verse.getLines()) {
                TextFlow lineFlow = new TextFlow();
                lineFlow.setMouseTransparent(true);

                double lineFontSize = (line.getFontSize() != null ? line.getFontSize() * sp : defaultFontSizePx);

                for (LyricFragment fragment : line.getFragments()) {
                    Text textNode = new Text(fragment.getText());
                    textNode.setMouseTransparent(true);

                    FontWeight weight = fragment.isBold() ? FontWeight.BOLD : FontWeight.NORMAL;
                    FontPosture posture = fragment.isItalic() ? FontPosture.ITALIC : FontPosture.REGULAR;

                    textNode.setFont(Font.font("Times New Roman", weight, posture, lineFontSize));
                    textNode.setUnderline(fragment.isUnderline());
                    lineFlow.getChildren().add(textNode);
                }
                linesBox.getChildren().add(lineFlow);
            }

            Button editBtn = createEditButton();
            StackPane.setAlignment(editBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(editBtn, new Insets(4, 4, 0, 0));

            editBtn.setOnAction(e -> {
                e.consume();
                openEditDialog(verse);
            });

            verseContainer.setOnMousePressed(e -> {
                e.consume();
                int verseNum = verse.getNumber();
                ScoreStateManager.getInstance().setSelectedVerseNumber(verseNum);

                updateVerseHighlights(versesBox, verseNum, frame, sp);

                double contentHeight = versesBox.getHeight();
                double viewportHeight = getViewportBounds().getHeight();
                double scrollableHeight = contentHeight - viewportHeight;

                if (scrollableHeight > 0) {
                    double verseY = verseContainer.getBoundsInParent().getMinY();
                    double targetVvalue = Math.max(0.0, Math.min(1.0, verseY / scrollableHeight));
                    setVvalue(targetVvalue);
                }

                if (onRequestDraw != null) {
                    onRequestDraw.run();
                }
            });

            verseContainer.getChildren().addAll(linesBox, editBtn);
            versesBox.getChildren().add(verseContainer);
        }

        return versesBox;
    }

    private Button createEditButton() {
        SVGPath icon = new SVGPath();
        icon.setContent("M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        icon.setStyle("-fx-fill: #6b7280;");

        Button btn = new Button();
        btn.setGraphic(icon);
        btn.setFocusTraversable(false);
        btn.setStyle(
                "-fx-background-color: #e5e7eb; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 3px 5px; " +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #d1d5db; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 3px 5px; " +
                        "-fx-cursor: hand;"
        ));

        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: #e5e7eb; " +
                        "-fx-background-radius: 4px; " +
                        "-fx-padding: 3px 5px; " +
                        "-fx-cursor: hand;"
        ));

        return btn;
    }

    private void openEditDialog(TextFrameVerse frameVerse) {
        int verseNumber = frameVerse.getNumber();
        ScoreStateManager stateMgr = ScoreStateManager.getInstance();
        if (stateMgr.getCurrentMode() == null) return;

        Verse targetVerse = stateMgr.getCurrentMode().getVerses().get(verseNumber);

        if (targetVerse != null) {
            CustomVerseEditDialog dialog = new CustomVerseEditDialog();
            dialog.setTitle("Edycja zwrotki")
                    .setHeader("Edytuj treść zwrotki " + verseNumber)
                    .setContent("Naciśnij Enter, aby podzielić treść zwrotki na osobne linie.")
                    .setVerseText(VerseTextMapper.toEditorText(frameVerse))
                    .setConfirmButton("Zapisz", (editedText, applyToAll) -> {
                        if (applyToAll) {
                            VerseTextMapper.applyLineBreaksToAllVerses(
                                    stateMgr.getCurrentMode().getVerses().values(),
                                    targetVerse,
                                    editedText
                            );
                        } else {
                            VerseTextMapper.applyLineBreaksFromText(targetVerse, editedText);
                        }
                        stateMgr.notifyScoreChanged();
                    })
                    .showAndWait();
        }
    }
}