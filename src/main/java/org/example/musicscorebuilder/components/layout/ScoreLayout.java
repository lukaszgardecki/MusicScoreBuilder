package org.example.musicscorebuilder.components.layout;

import java.util.ArrayList;
import java.util.List;

public class ScoreLayout {
    private final List<PageLayout> pages = new ArrayList<>();
    private final double spacing = 4.0;

    public void addPageLayout(PageLayout pageLayout) {
        pages.add(pageLayout);
    }

    public List<PageLayout> getPages() { return pages; }
    public double getSpacing() { return spacing; }
}