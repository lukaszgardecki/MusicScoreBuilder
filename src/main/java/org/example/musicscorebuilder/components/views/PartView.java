package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.PMeasureLayout;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.util.Util;

public class PartView extends ComponentView {
    private final PMeasureView pMeasureView = new PMeasureView();
    private final BraceView braceView = new BraceView();

    public void draw(GraphicsContext gc, PartLayout part, double systemX, double systemY, double sp) {
        double partX = part.getX() * sp + systemX;
        double partY = part.getY() * sp + systemY;
        double widthPx = part.getWidth() * sp;
        double heightPx = part.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), partX, partY, widthPx, heightPx);

        for (PMeasureLayout partMeasureLayout : part.getPartMeasures()) {
            pMeasureView.draw(gc, partMeasureLayout, partX, partY, sp);
        }

        braceView.draw(gc, part.getBraceLayout(), partX, partY, sp);
    }
}


