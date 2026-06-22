package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;


public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private HBox pageContainer;
    @FXML private Pane page;
    @FXML private Group staffLayer;

    private LayoutEngine layoutEngine = new LayoutEngine();
    private ScoreRenderer renderer;

    private double lastX;
    private double lastY;
    private double zoom = 1.0;

    @FXML
    public void initialize() {
        scrollPane.setPannable(false);
        enableDrag();
        enableZoom();

        renderer = new ScoreRenderer(staffLayer);
        Score score = createScore();
        ScoreLayout scoreLayout = layoutEngine.computeLayout(score, new Page());
        renderer.render(scoreLayout);
    }

    private void enableDrag() {
        pageContainer.setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        pageContainer.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            pageContainer.setTranslateX(pageContainer.getTranslateX() + dx);
            pageContainer.setTranslateY(pageContainer.getTranslateY() + dy);

            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });
    }

    private void enableZoom() {
        scrollPane.addEventFilter(ScrollEvent.SCROLL, e -> {

            // Zoom tylko gdy CTRL wciśnięty
            if (!e.isControlDown()) return;

            double zoomFactor = 1.1;

            if (e.getDeltaY() > 0) {
                zoom *= zoomFactor;
            } else {
                zoom /= zoomFactor;
            }

            // ograniczenia
            zoom = Math.max(0.2, Math.min(zoom, 5.0));

            page.setScaleX(zoom);
            page.setScaleY(zoom);

            e.consume();
        });
    }

    private Score createScore() {
        Score score = new Score();
        score.add(new Staff());
        score.add(new Staff());
        return score;
    }
}
