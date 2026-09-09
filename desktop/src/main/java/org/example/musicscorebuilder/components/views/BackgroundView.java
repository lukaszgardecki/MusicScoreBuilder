package org.example.musicscorebuilder.components.views;

import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import org.example.musicscorebuilder.components.frames.TextFrameLayout;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.managers.LyricEditorManager;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreStateManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BackgroundView extends Pane {
    private final ModeManager modeManager = ModeManager.getInstance();
    private final LyricEditorManager lyricEditorManager = LyricEditorManager.getInstance();

    private final Map<String, TextFrameView> textFrameOverlays = new HashMap<>();

    private ScoreView scoreView;
    private double lastX;
    private double lastY;
    private double zoom = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private boolean wasDragged = false;

    public BackgroundView(){
        this.lyricEditorManager.init(this, new LyricEditorManager.CoordinateTransformer() {
            @Override
            public double modelToViewX(double modelX) {
                return toScreenX(modelX);
            }

            @Override
            public double modelToViewY(double modelY) {
                return toScreenY(modelY);
            }

            @Override
            public double getScaleY() {
                return getActualSp();
            }
            @Override
            public ScoreLayout getScoreLayout() {
                return (scoreView != null) ? scoreView.getScoreLayout() : null;
            }
        });

        enableDrag();
        enableZoom();
        centerFirstPage();

        modeManager.addModeChangeListener(isInsert -> {
            if (scoreView != null) {
                scoreView.update(scoreView.getScoreLayout());
                scoreView.setViewportTransform(offsetX, offsetY, zoom);
                updateTextFrameOverlays();
            }
        });
    }

    public void updateContent(ScoreLayout newLayout) {
        if (newLayout == null) {
            clearOverlays();
            getChildren().clear();
            scoreView = null;
            return;
        }

        if (scoreView == null) {
            scoreView = new ScoreView(newLayout);
            getChildren().add(scoreView);

            scoreView.widthProperty().bind(this.widthProperty());
            scoreView.heightProperty().bind(this.heightProperty());
            scoreView.widthProperty().addListener((obs, oldVal, newVal) -> {
                centerFirstPage();
                updateTextFrameOverlays();
            });
            scoreView.heightProperty().addListener((obs, oldVal, newVal) -> {
                centerFirstPage();
                updateTextFrameOverlays();
            });

            scoreView.setViewportTransform(offsetX, offsetY, zoom);
        } else {
            scoreView.update(newLayout);
        }

        updateTextFrameOverlays();
    }

    private void centerFirstPage() {
        if (scoreView == null || scoreView.getScoreLayout() == null) return;
        if (scoreView.getScoreLayout().getPages().isEmpty()) return;

        var page = scoreView.getScoreLayout().getPages().getFirst();

        double sp = getActualSp();
        double pageWidthPx = page.getWidth() * sp;
        double pageHeightPx = page.getHeight() * sp;

        double canvasWidth = scoreView.getWidth();
        double canvasHeight = scoreView.getHeight();

        offsetX = (canvasWidth - pageWidthPx) / 2.0;
        offsetY = (canvasHeight - pageHeightPx) / 2.0;

        scoreView.setViewportTransform(offsetX, offsetY, zoom);

        updateLyricPosition();
        updateTextFrameOverlays();
    }

    private void enableDrag() {
        setOnMousePressed(e -> {
            if (modeManager.isInsertMode()) return;
            lastX = e.getSceneX();
            lastY = e.getSceneY();
            wasDragged = false;
        });

        setOnMouseDragged(e -> {
            if (modeManager.isInsertMode()) return;

            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            if (Math.hypot(dx, dy) > 2.0) {
                wasDragged = true;
            }

            if (dx != 0 || dy != 0) {
                offsetX += dx;
                offsetY += dy;

                lastX = e.getSceneX();
                lastY = e.getSceneY();

                if (scoreView != null) {
                    scoreView.setViewportTransform(offsetX, offsetY, zoom);
                }

                updateLyricPosition();
                updateTextFrameOverlays();
            }
        });
    }

    private void enableZoom() {
        addEventFilter(ScrollEvent.SCROLL, e -> {
            if (!e.isControlDown()) return;
            e.consume();

            if (scoreView == null) return;

            double maxZoom = 15.0;
            double minZoom = 0.1;
            double delta = 1.12;
            double zoomFactor = (e.getDeltaY() > 0) ? delta : 1 / delta;

            double oldZoom = this.zoom;
            this.zoom = Math.max(minZoom, Math.min(this.zoom * zoomFactor, maxZoom));

            if (this.zoom == oldZoom) return;
            double actualFactor = this.zoom / oldZoom;

            double mouseX = e.getX();
            double mouseY = e.getY();

            offsetX = mouseX - (mouseX - offsetX) * actualFactor;
            offsetY = mouseY - (mouseY - offsetY) * actualFactor;

            scoreView.setViewportTransform(offsetX, offsetY, zoom);

            updateLyricPosition();
            updateTextFrameOverlays();
        });
    }

    public void updateTextFrameOverlays() {
        if (scoreView == null || scoreView.getScoreLayout() == null) {
            clearOverlays();
            return;
        }

        ScoreLayout layout = scoreView.getScoreLayout();
        double sp = getActualSp();
        int selectedVerseNum = ScoreStateManager.getInstance().getSelectedVerseNumber();

        Set<String> activeKeys = new HashSet<>();

        for (int pIdx = 0; pIdx < layout.getPages().size(); pIdx++) {
            PageLayout page = layout.getPages().get(pIdx);
            if (page.getBlocks() == null) continue;

            for (int bIdx = 0; bIdx < page.getBlocks().size(); bIdx++) {
                var block = page.getBlocks().get(bIdx);
                if (block instanceof TextFrameLayout textFrame) {
                    String frameKey = "p" + pIdx + "_b" + bIdx;
                    activeKeys.add(frameKey);

                    TextFrameView textFrameView = textFrameOverlays.computeIfAbsent(frameKey, key -> {
                        TextFrameView view = new TextFrameView(() -> {
                            if (scoreView != null) scoreView.requestDraw();
                        });
                        getChildren().add(view);
                        return view;
                    });

                    double screenX = toScreenX(page.getX() + textFrame.getX());
                    double screenY = toScreenY(page.getY() + textFrame.getContentY());
                    double frameW = textFrame.getWidth() * sp;
                    double frameH = textFrame.getContentHeight() * sp;

                    textFrameView.update(textFrame, screenX, screenY, frameW, frameH, sp, selectedVerseNum);
                }
            }
        }

        textFrameOverlays.keySet().removeIf(key -> {
            if (!activeKeys.contains(key)) {
                getChildren().remove(textFrameOverlays.get(key));
                return true;
            }
            return false;
        });
    }

    private void clearOverlays() {
        textFrameOverlays.values().forEach(this.getChildren()::remove);
        textFrameOverlays.clear();
    }

    public boolean wasLastMousePressJustClick() { return !wasDragged; }
    public double toModelX(double screenX) { return (screenX - offsetX) / getActualSp(); }
    public double toModelY(double screenY) { return (screenY - offsetY) / getActualSp(); }
    public double toScreenX(double modelX) { return offsetX + (modelX * getActualSp()); }
    public double toScreenY(double modelY) { return offsetY + (modelY * getActualSp()); }
    public ScoreView getScoreView() { return scoreView; }
    public double getActualSp() {
        if (scoreView == null) return 10.0;
        return zoom * scoreView.getBaseSpatiumPx();
    }

    private void updateLyricPosition() {
        if (lyricEditorManager != null) lyricEditorManager.updatePosition();
    }
}