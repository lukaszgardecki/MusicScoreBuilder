package org.example.musicscorebuilder.components.views;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

public class BackgroundView extends StackPane {
    private ScoreView scoreView;
    private double lastX;
    private double lastY;
    private double zoom = 1.0;

    public BackgroundView(){
        setAlignment(Pos.CENTER);
        enableDrag();
        enableZoom();
    }

    public void updateContent(ScoreLayout newLayout) {
        if (scoreView == null) {
            scoreView = new ScoreView(newLayout);
            getChildren().add(scoreView);
        } else {
            scoreView.update(newLayout);
        }
    }

    private void enableDrag() {
        setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            if (getChildren().isEmpty()) return;
            Node scoreView = getChildren().get(0);
            scoreView.setTranslateX(scoreView.getTranslateX() + dx);
            scoreView.setTranslateY(scoreView.getTranslateY() + dy);

            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });
    }

    private void enableZoom() {
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!e.isControlDown()) return;
            e.consume();

            if (getChildren().isEmpty()) return;
            Node scoreView = getChildren().get(0);
            double delta = 1.1;
            double zoomFactor = (e.getDeltaY() > 0) ? delta : 1 / delta;
            zoom = Math.max(0.2, Math.min(zoom * zoomFactor, 5.0));

            double beforeScaleMouseX = scoreView.sceneToLocal(e.getSceneX(), e.getSceneY()).getX();
            double beforeScaleMouseY = scoreView.sceneToLocal(e.getSceneX(), e.getSceneY()).getY();

            scoreView.setScaleX(zoom);
            scoreView.setScaleY(zoom);

            double afterScaleMouseX = scoreView.localToScene(beforeScaleMouseX, beforeScaleMouseY).getX();
            double afterScaleMouseY = scoreView.localToScene(beforeScaleMouseX, beforeScaleMouseY).getY();

            scoreView.setTranslateX(scoreView.getTranslateX() + (e.getSceneX() - afterScaleMouseX));
            scoreView.setTranslateY(scoreView.getTranslateY() + (e.getSceneY() - afterScaleMouseY));
        });
    }
}
