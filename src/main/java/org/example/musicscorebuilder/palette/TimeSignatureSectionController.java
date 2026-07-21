package org.example.musicscorebuilder.palette;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.TimeSigLayout;
import org.example.musicscorebuilder.components.music.Leland;
import org.example.musicscorebuilder.components.music.TimeSignature;

import java.util.Arrays;
import java.util.List;

public class TimeSignatureSectionController extends AbstractPaletteSectionController<PreDefinedTimeSignature> {

    public TimeSignatureSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected List<PreDefinedTimeSignature> getItems() {
        return Arrays.asList(PreDefinedTimeSignature.values());
    }

    @Override
    protected Node createButtonGraphic(PreDefinedTimeSignature sig) {
        if (sig.isFractional()) {
            VBox container = new VBox();
            container.setAlignment(Pos.CENTER);
            container.setSpacing(0);

            Text topText = new Text(sig.getTopGlyph());
            topText.getStyleClass().addAll("text-glyph", "fraction-text");
            topText.setBoundsType(TextBoundsType.VISUAL);

            Text bottomText = new Text(sig.getBottomGlyph());
            bottomText.getStyleClass().addAll("text-glyph", "fraction-text");
            bottomText.setBoundsType(TextBoundsType.VISUAL);

            container.getChildren().addAll(topText, bottomText);
            return container;
        } else {
            Text singleText = new Text(sig.getTopGlyph());
            singleText.getStyleClass().addAll("text-glyph", "single-text");
            singleText.setBoundsType(TextBoundsType.VISUAL);
            return singleText;
        }
    }

    @Override
    protected boolean applyToSelectedElement(PreDefinedTimeSignature sig) {
        Selectable item = stateManager.getSelectedItem();

        if (item instanceof TimeSigLayout) {
            scoreService.getScore().getModes().forEach(mode -> mode.setTimeSignature(sig.getTimeSignature()));
            return true;
        }
        return false;
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