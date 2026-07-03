package org.example.musicscorebuilder.components.views;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;

import java.util.List;

public class PageView extends Pane {
    private final PageContentView pageContentView;

    public PageView(PageLayout page) {
        getStyleClass().add("page-a4");
        setPrefSize(page.getWidth(), page.getHeight());
        setMinSize(page.getWidth(), page.getHeight());
        setMaxSize(page.getWidth(), page.getHeight());
        setSnapToPixel(false);
        this.pageContentView = new PageContentView(page);
        this.getChildren().add(pageContentView);
    }

    public void update(PageLayout pageLayout) {
        List<SystemLayout> systems = pageLayout.getSystems();
        ObservableList<Node> systemNodes = pageContentView.getChildren();

        while (systemNodes.size() > systems.size()) {
            systemNodes.removeLast();
        }

        for (int i = 0; i < systems.size(); i++) {
            if (i < systemNodes.size()) {
                ((SystemView) systemNodes.get(i)).update(systems.get(i));
            } else {
                systemNodes.add(new SystemView(systems.get(i)));
            }
        }
    }

    private class PageContentView extends VBox {
        public PageContentView(PageLayout page) {
            setSpacing(page.getSystemSpacing());
            setLayoutX(page.getMarginLeft());
            setLayoutY(page.getMarginTop());
            setPrefSize(page.getEffectiveWidth(), page.getEffectiveHeight());
            setSnapToPixel(false);

            page.getSystems().stream()
                    .map(SystemView::new)
                    .forEach(this.getChildren()::add);
        }
    }
}