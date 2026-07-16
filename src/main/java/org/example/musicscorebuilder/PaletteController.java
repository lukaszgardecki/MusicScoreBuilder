package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.TimeSignature;

public class PaletteController {
    private static final int COLUMNS_COUNT = 6;

    @FXML
    private GridPane timeSignatureGrid;
    private Button selectedButton = null;

    @FXML
    public void initialize() {
        int index = 0;

        for (PreDefinedTimeSignature sig : PreDefinedTimeSignature.values()) {
            Button btn = createPaletteButton(sig);

            int col = index % COLUMNS_COUNT;
            int row = index / COLUMNS_COUNT;

            timeSignatureGrid.add(btn, col, row);
            index++;
        }
    }

    private Button createPaletteButton(PreDefinedTimeSignature sig) {
        Button btn = new Button();
        btn.getStyleClass().add("palette-btn");

        if (sig.isFractional()) {
            VBox container = new VBox();
            container.setAlignment(Pos.CENTER);
            container.setPadding(Insets.EMPTY);
            container.setSpacing(0);

            Text topText = new Text(sig.getTopGlyph());
            topText.getStyleClass().addAll("text-glyph", "fraction-text");
            topText.setBoundsType(TextBoundsType.VISUAL);

            Text bottomText = new Text(sig.getBottomGlyph());
            bottomText.getStyleClass().addAll("text-glyph", "fraction-text");
            bottomText.setBoundsType(TextBoundsType.VISUAL);

            VBox.setMargin(topText, Insets.EMPTY);
            VBox.setMargin(bottomText, Insets.EMPTY);

            container.getChildren().addAll(topText, bottomText);
            btn.setGraphic(container);
        } else {
            Text singleText = new Text(sig.getTopGlyph());
            singleText.getStyleClass().addAll("text-glyph", "single-text");
            singleText.setBoundsType(TextBoundsType.VISUAL);

            btn.setGraphic(singleText);
        }

        btn.setOnAction(event -> {
            selectButton(btn);
            handleTimeSignatureSelection(sig);
        });

        return btn;
    }

    private void selectButton(Button btn) {
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("selected");
        }

        selectedButton = btn;
        selectedButton.getStyleClass().add("selected");
    }

    private void handleTimeSignatureSelection(PreDefinedTimeSignature sig) {
        ScoreService scoreService = ScoreService.getInstance();
        Score score = scoreService.getScore();
        score.getModes().forEach(mode -> mode.setTimeSignature(sig.getTimeSignature()));
        scoreService.notifyListeners();
    }
}

enum PreDefinedTimeSignature {
    TWO_FOUR(2, 4, Leland.TIME_2.getCode(), Leland.TIME_4.getCode()),
    THREE_FOUR(3, 4, Leland.TIME_3.getCode(), Leland.TIME_4.getCode()),
    FOUR_FOUR(4, 4, Leland.TIME_4.getCode(), Leland.TIME_4.getCode()),
    FIVE_FOUR(5, 4, Leland.TIME_5.getCode(), Leland.TIME_4.getCode()),
    SIX_FOUR(6, 4, Leland.TIME_6.getCode(), Leland.TIME_4.getCode()),
    THREE_EIGHT(3, 8, Leland.TIME_3.getCode(), Leland.TIME_8.getCode()),
    FOUR_EIGHT(4, 8, Leland.TIME_4.getCode(), Leland.TIME_8.getCode()),
    FIVE_EIGHT(5, 8, Leland.TIME_5.getCode(), Leland.TIME_8.getCode()),
    SIX_EIGHT(6, 8, Leland.TIME_6.getCode(), Leland.TIME_8.getCode()),
    SEVEN_EIGHT(7, 8, Leland.TIME_7.getCode(), Leland.TIME_8.getCode()),
    NINE_EIGHT(9, 8, Leland.TIME_9.getCode(), Leland.TIME_8.getCode()),

    COMMON(Leland.TIME_COMMON.getCode(), TimeSignature.Type.COMMON),
    CUT(Leland.TIME_CUT.getCode(), TimeSignature.Type.CUT),

    TWO_TWO(2, 2, Leland.TIME_2.getCode(), Leland.TIME_2.getCode()),
    THREE_TWO(3, 2, Leland.TIME_3.getCode(), Leland.TIME_2.getCode());

    private final String topGlyph;
    private final String bottomGlyph;
    private final TimeSignature timeSignature;

    PreDefinedTimeSignature(int beat, int beatType, String topGlyph, String bottomGlyph) {
        this.timeSignature = new TimeSignature(beat, beatType);
        this.topGlyph = topGlyph;
        this.bottomGlyph = bottomGlyph;
    }

    PreDefinedTimeSignature(String singleGlyph, TimeSignature.Type type) {
        this.topGlyph = singleGlyph;
        this.bottomGlyph = null;
        this.timeSignature = type == TimeSignature.Type.COMMON ? TimeSignature.commonTime() : TimeSignature.cutTime();
    }

    public TimeSignature getTimeSignature() { return timeSignature; }
    public String getTopGlyph() { return topGlyph; }
    public String getBottomGlyph() { return bottomGlyph; }
    public boolean isFractional() { return timeSignature.isFractional(); }
}
