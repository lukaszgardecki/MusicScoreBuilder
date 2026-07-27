package org.example.musicscorebuilder.palette;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.example.musicscorebuilder.components.layout.Selectable;
import org.example.musicscorebuilder.components.layout.TimeSigLayout;

import java.util.Arrays;
import java.util.List;

public class TimeSignatureSectionController extends AbstractPaletteSectionController<PreDefinedTimeSignature> {

    public TimeSignatureSectionController(GridPane gridPane) {
        super(gridPane);
    }

    @Override
    protected int getColumnsCount() { return 6; }

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
            scoreService.getScore().getModes().forEach(mode -> mode.setTimeSignature(sig));
            return true;
        }
        return false;
    }
}

