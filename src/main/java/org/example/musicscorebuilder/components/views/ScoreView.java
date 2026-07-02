package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

import java.util.List;

public class ScoreView extends HBox {

    public ScoreView(ScoreLayout scoreLayout) {
        this.setSpacing(20);
        this.setFillHeight(false);
        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        scoreLayout.getPages().stream()
                .map(PageView::new)
                .forEach(this.getChildren()::add);
    }

    public void update(ScoreLayout newLayout) {
        List<PageLayout> pages = newLayout.getPages();

        for (int i = this.getChildren().size(); i < pages.size(); i++) {
            this.getChildren().add(new PageView(pages.get(i)));
        }

        while (this.getChildren().size() > pages.size()) {
            this.getChildren().removeLast();
        }

        for (int i = 0; i < this.getChildren().size(); i++) {
            ((PageView) this.getChildren().get(i)).update(pages.get(i));
        }
    }
}