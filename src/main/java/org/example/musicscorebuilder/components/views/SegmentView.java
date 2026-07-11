package org.example.musicscorebuilder.components.views;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.util.Util;

public class SegmentView extends ComponentView {

    public void draw(GraphicsContext gc, SegmentLayout segment, double sMeasureX, double sMeasureY, double sp) {
        double absoluteX = sMeasureX + segment.getX() * sp;
        double absoluteY = sMeasureY + segment.getY() * sp;
        double width = segment.getWidth() * sp;
        double height = segment.getHeight() * sp;

        var color = switch (segment.getType()) {
            case CLEF -> Color.RED;
            case KEY_SIG -> Color.LIGHTGREEN;
            case TIME_SIG -> Color.CHOCOLATE;
            default -> Util.generateRandomColor();
        };

//        fillBackground(gc, color, absoluteX, absoluteY, width, height);
    }
}
