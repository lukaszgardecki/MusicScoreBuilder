package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.ScoreStyle;
import org.example.musicscorebuilder.components.music.Mode;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.PageFormat;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.views.BackgroundView;

public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private BackgroundView container;
    private LayoutEngine layoutEngine;

    @FXML
    public void initialize() {
        container.prefWidthProperty().bind(scrollPane.widthProperty());
        container.prefHeightProperty().bind(scrollPane.heightProperty());
        ScoreService.getInstance().addListener(this::refreshView);
        this.layoutEngine = new LayoutEngine(
                new Page(PageFormat.A4_V),
                new ScoreStyle()
        );
        refreshView();
    }

    private void refreshView() {
        Score score = ScoreService.getInstance().getScore();
        Mode mode = score.getModes().getFirst();
        ScoreLayout scoreLayout = layoutEngine.compute(mode);
        container.updateContent(scoreLayout);
    }
}
