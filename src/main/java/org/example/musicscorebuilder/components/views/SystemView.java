package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.util.Util;

public class SystemView extends ComponentView {
    private final MeasureView measureView = new MeasureView();
    private final BraceView braceView = new BraceView();

    public void draw(GraphicsContext gc, SystemLayout system, double pageX, double pageY, double sp) {
        double systemX = system.getX() * sp + pageX;
        double systemY = system.getY() * sp + pageY;
        double widthPx = system.getWidth() * sp;
        double heightPx = system.getHeight() * sp;

//        fillBackground(gc, Util.generateRandomColor(), systemX, systemY, widthPx, heightPx);

        system.getBraceLayout().ifPresent(brace -> braceView.draw(gc, brace, systemX, systemY, sp));

        for (MeasureLayout measure : system.getMeasures()) {
            measureView.draw(gc, measure, systemX, systemY, sp);
        }
    }
}
