package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.util.Util;

public class SystemView extends ComponentView {
    private final PartView partView = new PartView();

    public void draw(GraphicsContext gc, SystemLayout system, double pageX, double pageY, double sp) {
        double systemX = system.getX() * sp + pageX;
        double systemY = system.getY() * sp + pageY;
        double widthPx = system.getWidth() * sp;
        double heightPx = system.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), systemX, systemY, widthPx, heightPx);

        for (PartLayout part : system.getParts()) {
            partView.draw(gc, part, systemX, systemY, sp);
        }
    }
}
