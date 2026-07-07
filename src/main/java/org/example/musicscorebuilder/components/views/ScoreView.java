package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;

import java.util.List;

//public class ScoreView extends HBox {
//
//    public ScoreView(ScoreLayout scoreLayout) {
//        this.setSpacing(20);
//        this.setFillHeight(false);
//        this.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//
//        scoreLayout.getPages().stream()
//                .map(PageView::new)
//                .forEach(this.getChildren()::add);
//    }
//
//    public void update(ScoreLayout newLayout) {
//        List<PageLayout> pages = newLayout.getPages();
//        ObservableList<Node> children = this.getChildren();
//
//        while (children.size() > pages.size()) {
//            children.removeLast();
//        }
//
//        for (int i = 0; i < pages.size(); i++) {
//            if (i < children.size()) {
//                ((PageView) children.get(i)).update(pages.get(i));
//            } else {
//                children.add(new PageView(pages.get(i)));
//            }
//        }
//    }
//}

public class ScoreView extends Canvas {
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private double zoom = 1.0;
    private double baseSpatium = 7.0;

    // Definiujemy stały logiczny rozmiar kartki A4 w pikselach (przy 100% zoomu)
    private final double PAGE_CARD_WIDTH = 800;
    private final double PAGE_CARD_HEIGHT = 1200;

    public ScoreView(ScoreLayout layout) {
        // Nasz widok odświeży się automatycznie, gdy okno zmieni rozmiar na starcie aplikacji
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());
    }

    public void update(ScoreLayout newLayout) {
        draw();
    }

    public void setViewportTransform(double offsetX, double offsetY, double zoom) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.zoom = zoom;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();

        // 1. Czyszczenie całego DOSTĘPNEGO OKNA aplikacji
        gc.clearRect(0, 0, getWidth(), getHeight());

        // Rysujemy ciemnoszare tło obszaru roboczego (poza kartką)
        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 2. Rysujemy białą kartkę, której pozycja zależy od offsetX i offsetY
        double cardW = PAGE_CARD_WIDTH * zoom;
        double cardH = PAGE_CARD_HEIGHT * zoom;

        gc.setFill(Color.WHITE);
        gc.fillRect(offsetX, offsetY, cardW, cardH);

        // Ramka/Cień wirtualnej kartki
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(1.0);
        gc.strokeRect(offsetX, offsetY, cardW, cardH);

        // 3. Rysujemy testową pięciolinię WEWNĄTRZ wirtualnej kartki
        double sp = baseSpatium * zoom;
        double logiczneYSpatiom = 20.0; // 20 spatium od góry kartki

        // Pozycja Y linii na ekranie to: pozycja góry kartki + odległość w spatiach
        double finalneYNaEkranie = offsetY + (logiczneYSpatiom * sp);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.0 * zoom); // Linie pogrubiają się przy zoomie

        for (int line = 0; line < 5; line++) {
            double lineY = finalneYNaEkranie + (line * sp);
            // Marginesy wewnętrzne kartki (np. 50px od lewej krawędzi kartki)
            double startX = offsetX + (50 * zoom);
            double endX = offsetX + cardW - (50 * zoom);

            gc.strokeLine(startX, lineY, endX, lineY);
        }

        // 4. Rysujemy testową nutkę
        gc.setFill(Color.BLACK);
        double nutaX = offsetX + (150 * zoom);
        double nutaY = finalneYNaEkranie + (2 * sp); // na środkowej linii

        gc.fillOval(nutaX - (sp * 0.6), nutaY - (sp * 0.4), sp * 1.2, sp * 0.8);
    }
}