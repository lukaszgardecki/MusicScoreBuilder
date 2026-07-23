package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.SegmentType;

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

    public SegmentLayout findFirstNoteSegment() {
        if (pages.isEmpty()) return null;
        var page = pages.getFirst();
        if (page.getSystems().isEmpty()) return null;
        var system = page.getSystems().getFirst();
        if (system.getMeasures().isEmpty()) return null;
        var measure = system.getMeasures().getFirst();
        for (SegmentLayout segment : measure.getSegments()) {
            if (segment.getType() == SegmentType.NOTEREST) {
                return segment;
            }
        }
        return null;
    }
}