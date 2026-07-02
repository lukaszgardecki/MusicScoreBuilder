package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
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
        ObservableList<Node> children = this.getChildren();

        while (children.size() > pages.size()) {
            children.removeLast();
        }

        for (int i = 0; i < pages.size(); i++) {
            if (i < children.size()) {
                ((PageView) children.get(i)).update(pages.get(i));
            } else {
                children.add(new PageView(pages.get(i)));
            }
        }
    }
}