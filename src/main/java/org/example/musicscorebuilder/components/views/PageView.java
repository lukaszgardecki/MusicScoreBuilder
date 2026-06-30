package org.example.musicscorebuilder.components.views;

import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PageLayout;

public class PageView extends Pane {

    public PageView(PageLayout page) {
        getStyleClass().add("page-a4");
        setPrefSize(page.getWidth(), page.getHeight());
        setMinSize(page.getWidth(), page.getHeight());
        setMaxSize(page.getWidth(), page.getHeight());
        this.getChildren().add(new PageContentView(page));
    }

    private class PageContentView extends VBox {
        public PageContentView(PageLayout page) {
            setSpacing(page.getSystemSpacing());
            setLayoutX(page.getMarginLeft());
            setLayoutY(page.getMarginTop());
            setPrefSize(page.getEffectiveWidth(), page.getEffectiveHeight());

            page.getSystems().stream()
                    .map(SystemView::new)
                    .forEach(this.getChildren()::add);
        }
    }
}