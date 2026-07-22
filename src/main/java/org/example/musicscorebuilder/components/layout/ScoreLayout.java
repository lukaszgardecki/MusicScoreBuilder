package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class ScoreLayout {
    private final ScoreStyle style;
    private final List<PageLayout> pages = new ArrayList<>();

    public ScoreLayout(ScoreStyle style) {
        this.style = style;
    }

    public void addPageLayout(PageLayout pageLayout) {
        pages.add(pageLayout);
    }

    public ScoreStyle getStyle() { return style; }
    public List<PageLayout> getPages() { return pages; }
}