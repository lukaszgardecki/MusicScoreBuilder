package org.example.musicscorebuilder;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;
import org.example.musicscorebuilder.components.views.PageView;
import org.example.musicscorebuilder.components.views.ScoreView;


public class PageAreaController {
    @FXML private ScrollPane scrollPane;
    @FXML private HBox pageContainer;
    private PageView pageView;

    private double lastX;
    private double lastY;
    private double zoom = 1.0;

    @FXML
    public void initialize() {
        scrollPane.setPannable(false);
        enableDrag();
        enableZoom();
        Page page = new Page();

        pageView = new PageView();
        ScoreView contentArea = new ScoreView(page);
        pageView.getChildren().add(contentArea);
        pageContainer.getChildren().add(pageView);

        ScoreLayout scoreLayout = new LayoutEngine(page).computeLayout(createScore());
        new ScoreRenderer(contentArea).render(scoreLayout);
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
            if (!e.isControlDown()) return;

            double zoomFactor = 1.1;

            if (e.getDeltaY() > 0) {
                zoom *= zoomFactor;
            } else {
                zoom /= zoomFactor;
            }

            // ograniczenia
            zoom = Math.max(0.2, Math.min(zoom, 5.0));

            pageView.setScaleX(zoom);
            pageView.setScaleY(zoom);

            e.consume();
        });
    }

    private Score createScore() {
        Score score = new Score("Utwór", "Kompozytor");
        Part piano = new Part("Piano1");
        piano.add(new Staff());
        piano.add(new Staff());

        Part piano2 = new Part("Piano2");
        piano2.add(new Staff());
        piano2.add(new Staff());
        piano2.add(new Staff());

        score.add(piano);
        score.add(piano2);
        return score;
    }
}
