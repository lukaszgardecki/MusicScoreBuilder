package org.example.musicscorebuilder.components.views;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.edit.GhostNoteLayout;
import org.example.musicscorebuilder.components.music.SegmentType;
import org.example.musicscorebuilder.managers.LayoutHitTester;
import org.example.musicscorebuilder.managers.ModeManager;
import org.example.musicscorebuilder.managers.ScoreNavigator;

import java.util.List;

public class ScoreView extends Canvas {
    private final GraphicsContext gc = getGraphicsContext2D();
    private final PageView pageView = new PageView();
    private ScoreLayout scoreLayout;
    private final ModeManager modeManager = ModeManager.getInstance();
    private final ScoreNavigator scoreNavigator = ScoreNavigator.getInstance();
    private final double baseSpatiumPx;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double zoom = 1.0;

    private boolean needsRedraw = false;
    private final AnimationTimer renderLoop;
    private double lastMouseX = -1;
    private double lastMouseY = -1;

    public ScoreView(ScoreLayout layout) {
        this.scoreLayout = layout;

        double dpi = Screen.getPrimary().getDpi();
        if (dpi <= 0) dpi = 96.0;

        double pixelsPerMm = dpi / 25.4;
        double spatiumMm = (scoreLayout != null && scoreLayout.getStyle() != null)
                ? scoreLayout.getStyle().getSpatiumMm() : 1.75;
        this.baseSpatiumPx = spatiumMm * pixelsPerMm;

        widthProperty().addListener(evt -> requestDraw());
        heightProperty().addListener(evt -> requestDraw());

        renderLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (needsRedraw) {
                    needsRedraw = false;
                    actualDraw();
                }
            }
        };
        renderLoop.start();

        enableGhostNoteTracking();
    }

    private void enableGhostNoteTracking() {
        setOnMouseMoved(e -> {
            if (!modeManager.isInsertMode() || scoreLayout == null) return;

            if (Math.abs(e.getX() - lastMouseX) < 2.0 && Math.abs(e.getY() - lastMouseY) < 2.0) {
                return;
            }
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            double modelX = (e.getX() - offsetX) / getActualSp();
            double modelY = (e.getY() - offsetY) / getActualSp();

            List<PageLayout> pages = scoreLayout.getPages();
            if (pages.isEmpty()) return;

            LayoutHitTester.SegmentStaffAndY target = LayoutHitTester.findSegmentAndStaffAt(
                    pages, modelX, modelY
            );

            if (target != null && target.segment().getType() == SegmentType.NOTEREST) {
                var cursor = scoreNavigator.getLastCursor();
                if (cursor == null || cursor.getElement() == null) return;

                int voice = cursor.getElement().getVoice();
                boolean segmentHasSameVoiceNote = target.segment().hasAnyNoteRestAtStaffByVoice(target.staff().getStaffIndex(), voice);
                if (!segmentHasSameVoiceNote) return;

                GhostNoteLayout currentGhost = modeManager.getGhostNote();

                if (currentGhost == null || !currentGhost.getSegment().equals(target.segment()) || currentGhost.getStaff() != target.staff()) {
                    GhostNoteLayout ghost = new GhostNoteLayout(target.segment(), target.staff(), target.measureY());
                    modeManager.setGhostNote(ghost);
                    requestDraw();
                } else {
                    double oldY = currentGhost.getY();
                    currentGhost.updatePitchFromY(target.measureY());
                    if (currentGhost.getY() != oldY) {
                        requestDraw();
                    }
                }
            }
        });
    }

    public void update(ScoreLayout newLayout) {
        this.scoreLayout = newLayout;
        requestDraw();
    }

    public void setViewportTransform(double offsetX, double offsetY, double zoom) {
        if (this.offsetX == offsetX && this.offsetY == offsetY && this.zoom == zoom) return;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.zoom = zoom;
        requestDraw();
    }

    private void requestDraw() {
        needsRedraw = true;
    }

    private void actualDraw() {
        if (scoreLayout == null) return;
        List<PageLayout> pages = scoreLayout.getPages();
        if (pages == null || pages.isEmpty()) return;

        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) return;

        gc.clearRect(0, 0, width, height);
        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, width, height);
        drawPages(pages, width, height);
    }

    private void drawPages(List<PageLayout> pages, double canvasWidth, double canvasHeight) {
        double sp = zoom * baseSpatiumPx;
        int pageCount = pages.size();

        for (int i = 0; i < pageCount; i++) {
            PageLayout page = pages.get(i);
            double pageX = offsetX + page.getX() * sp;
            double pageY = offsetY + page.getY() * sp;
            double pageWidthPx = page.getWidth() * sp;
            double pageHeightPx = page.getHeight() * sp;

            if (pageY + pageHeightPx < 0 || pageY > canvasHeight ||
                    pageX + pageWidthPx < 0 || pageX > canvasWidth) {
                continue;
            }

            pageView.draw(gc, page, offsetX, offsetY, sp);
        }
    }

    public ScoreLayout getScoreLayout() { return scoreLayout; }
    public double getBaseSpatiumPx() { return baseSpatiumPx; }

    private double getActualSp() {
        return zoom * baseSpatiumPx;
    }
}