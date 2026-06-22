package org.example.musicscorebuilder;

import javafx.scene.Group;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffView;

public class ScoreRenderer {
    private final Group root;

    public ScoreRenderer(Group root) {
        this.root = root;
    }

    public void render(ScoreLayout score) {
        root.getChildren().clear();
        score.getStaves().stream()
                .map(StaffView::new)
                .forEach(root.getChildren()::add);
    }
}