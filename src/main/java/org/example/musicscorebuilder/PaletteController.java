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
        System.out.println("Wybrano metrum: " + sig);
    }
}

enum PreDefinedTimeSignature {
    TWO_FOUR(Leland.TIME_2.getCode(), Leland.TIME_4.getCode()),
    THREE_FOUR(Leland.TIME_3.getCode(), Leland.TIME_4.getCode()),
    FOUR_FOUR(Leland.TIME_4.getCode(), Leland.TIME_4.getCode()),
    FIVE_FOUR(Leland.TIME_5.getCode(), Leland.TIME_4.getCode()),
    SIX_FOUR(Leland.TIME_6.getCode(), Leland.TIME_4.getCode()),
    THREE_EIGHT(Leland.TIME_3.getCode(), Leland.TIME_8.getCode()),
    FOUR_EIGHT(Leland.TIME_4.getCode(), Leland.TIME_8.getCode()),
    FIVE_EIGHT(Leland.TIME_5.getCode(), Leland.TIME_8.getCode()),
    SIX_EIGHT(Leland.TIME_6.getCode(), Leland.TIME_8.getCode()),
    SEVEN_EIGHT(Leland.TIME_7.getCode(), Leland.TIME_8.getCode()),
    NINE_EIGHT(Leland.TIME_9.getCode(), Leland.TIME_8.getCode()),

    COMMON_TIME(Leland.TIME_COMMON.getCode()),
    CUT_TIME(Leland.TIME_CUT.getCode()),

    TWO_TWO(Leland.TIME_2.getCode(), Leland.TIME_2.getCode()),
    THREE_TWO(Leland.TIME_3.getCode(), Leland.TIME_2.getCode());

    private final String topGlyph;
    private final String bottomGlyph;

    PreDefinedTimeSignature(String topGlyph, String bottomGlyph) {
        this.topGlyph = topGlyph;
        this.bottomGlyph = bottomGlyph;
    }

    PreDefinedTimeSignature(String singleGlyph) {
        this.topGlyph = singleGlyph;
        this.bottomGlyph = null;
    }

    public String getTopGlyph() { return topGlyph; }
    public String getBottomGlyph() { return bottomGlyph; }
    public boolean isFractional() { return bottomGlyph != null; }
}
