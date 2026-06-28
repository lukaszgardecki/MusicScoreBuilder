package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

public class ScoreView extends HBox {

    public ScoreView(ScoreLayout scoreLayout) {
        this.setSpacing(20);
        this.setFillHeight(false);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        scoreLayout.getPages().stream()
                .map(PageView::new)
                .forEach(this.getChildren()::add);
    }
}