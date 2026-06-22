package org.example.musicscorebuilder;

import javafx.scene.layout.Pane;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffView;
import org.example.musicscorebuilder.components.music.Page;

public class ScoreRenderer {
    private final Page page;
    private final Pane root;

    public ScoreRenderer(Pane root) {
        this.page = new Page();
        this.root = root;
    }

    public void render(ScoreLayout score) {
        root.getChildren().clear();
        score.getStaves().stream()
                .map(StaffView::new)
                .forEach(root.getChildren()::add);
    }
}