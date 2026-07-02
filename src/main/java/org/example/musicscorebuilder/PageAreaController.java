package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.views.BackgroundView;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;

    @FXML
    public void initialize() {
        container.prefWidthProperty().bind(scrollPane.widthProperty());
        container.prefHeightProperty().bind(scrollPane.heightProperty());
        ScoreService.getInstance().addListener(this::refreshView);
        refreshView();
    }

    private void refreshView() {
        ScoreLayout layout = new LayoutEngine(new Page()).compute(ScoreService.getInstance().getScore());
        container.updateContent(layout);
    }
}
