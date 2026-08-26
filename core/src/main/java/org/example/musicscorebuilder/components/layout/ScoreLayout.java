package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.SegmentType;

import java.util.ArrayList;
import java.util.List;

public class ScoreLayout {
    private final Score score;
    private final ScoreStyle style;
    private final List<PageLayout> pages = new ArrayList<>();

    public ScoreLayout(Score score, ScoreStyle style) {
        this.score =  score;
        this.style = style;
    }

    public void addPageLayout(PageLayout pageLayout) {
        pages.add(pageLayout);
    }

    public Score getScore() { return score; }
    public ScoreStyle getStyle() { return style; }
    public List<PageLayout> getPages() { return pages; }

    public Selectable findFirstNoteElement() {
        if (pages.isEmpty()) return null;
        var page = pages.get(0);
        if (page.getSystems().isEmpty()) return null;
        var system = page.getSystems().get(0);
        if (system.getMeasures().isEmpty()) return null;
        var measure = system.getMeasures().get(0);

        for (SegmentLayout segment : measure.getSegments()) {
            if (segment.getType() == SegmentType.NOTEREST) {
                List<ElementLayout> elements = segment.getElements();
                if (!elements.isEmpty()) {
                    return elements.get(0);
                }
            }
        }
        return null;
    }
}