package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;

public class LayoutEngine {
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout compute(Score score) {
        ScoreLayout layout = new ScoreLayout();
        int numberOfPages = 1;

        for (int i = 0; i < numberOfPages; i++) {
            PageLayout pageLayout = new PageLayout(page);
            SystemLayout systemLayout = new SystemLayout();

            for (Part part : score.getParts()) {
                PartLayout partLayout = new PartLayout(part);
                for (Staff staff : part.getStaves()) {
                    StaffLayout sl = new StaffLayout(staff, page.getEffectiveWidth());
                    partLayout.add(sl);
                }
                systemLayout.add(partLayout);
            }
            pageLayout.addSystem(systemLayout);
            layout.addPageLayout(pageLayout);
        }
        return layout;
    }
}
