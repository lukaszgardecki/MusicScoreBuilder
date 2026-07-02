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
        this.pageContentView = new PageContentView(page);
        this.getChildren().add(pageContentView);
    }

    public void update(PageLayout pageLayout) {
        setPrefSize(pageLayout.getWidth(), pageLayout.getHeight());

        List<SystemLayout> systems = pageLayout.getSystems();
        ObservableList<Node> systemNodes = pageContentView.getChildren();

        for (int i = 0; i < systems.size(); i++) {
            if (i < systemNodes.size()) {
                ((SystemView) systemNodes.get(i)).update(systems.get(i));
            } else {
                pageContentView.getChildren().add(new SystemView(systems.get(i)));
            }
        }

        while (systemNodes.size() > systems.size()) {
            systemNodes.removeLast();
        }
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