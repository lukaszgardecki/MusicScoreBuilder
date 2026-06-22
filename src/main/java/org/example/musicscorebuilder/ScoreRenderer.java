package org.example.musicscorebuilder;

import javafx.scene.layout.Pane;
import org.example.musicscorebuilder.components.views.PartView;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

public class ScoreRenderer {
    private final Pane root;

    public ScoreRenderer(Pane root) {
        this.root = root;
    }

    public void render(ScoreLayout score) {
        root.getChildren().clear();
        score.getParts().stream()
                .map(PartView::new)
                .forEach(root.getChildren()::add);
    }
}