package org.example.musicscorebuilder.components.views;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.example.musicscorebuilder.components.frames.TextFrameLayout;
import org.example.musicscorebuilder.components.music.LyricFragment;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;
import org.example.musicscorebuilder.components.music.frames.TextLine;
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
            if (node instanceof VBox box && box.getUserData() instanceof Integer vNum) {
                String bg = (vNum == selectedVerseNum) ? "#c8def8" : "transparent";
                box.setStyle(String.format(
                        "-fx-background-color: %s; -fx-background-radius: %fpx; -fx-cursor: hand;",
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
            VBox verseBox = new VBox();
            verseBox.setPadding(new Insets(padY, padX, padY, padX));
            verseBox.setUserData(verse.getNumber());
            verseBox.setFocusTraversable(false);

            String bgColor = (verse.getNumber() == selectedVerseNum) ? "#c8def8" : "transparent";

            verseBox.setStyle(String.format(
                    java.util.Locale.US,
                    "-fx-background-color: %s; -fx-background-radius: %.1fpx; -fx-cursor: hand;",
                    bgColor, radius
            ));

            verseBox.setOnMousePressed(e -> {
                e.consume();
                int verseNum = verse.getNumber();
                ScoreStateManager.getInstance().setSelectedVerseNumber(verseNum);

                updateVerseHighlights(versesBox, verseNum, frame, sp);

                double contentHeight = versesBox.getHeight();
                double viewportHeight = getViewportBounds().getHeight();
                double scrollableHeight = contentHeight - viewportHeight;

                if (scrollableHeight > 0) {
                    double verseY = verseBox.getBoundsInParent().getMinY();
                    double targetVvalue = Math.max(0.0, Math.min(1.0, verseY / scrollableHeight));
                    setVvalue(targetVvalue);
                }

                if (onRequestDraw != null) {
                    onRequestDraw.run();
                }
            });

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
                verseBox.getChildren().add(lineFlow);
            }
            versesBox.getChildren().add(verseBox);
        }

        return versesBox;
    }
}