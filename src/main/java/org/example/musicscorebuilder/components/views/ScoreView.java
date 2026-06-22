package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import org.example.musicscorebuilder.components.music.Page;

public class ScoreView extends Pane {

    public ScoreView(Page page) {
        setLayoutX(page.getMarginLeft());
        setLayoutY(page.getMarginTop());
        setPrefSize(page.getEffectiveWidth(), page.getEffectiveHeight());
//        setBackground(Background.fill(Color.RED));
    }
}