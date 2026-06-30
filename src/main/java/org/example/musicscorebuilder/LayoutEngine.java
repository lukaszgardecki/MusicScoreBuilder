package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

public class LayoutEngine {
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout compute(Score score) {
        ScoreLayout scoreLayout = new ScoreLayout();
        double pageWidth = page.getEffectiveWidth();
        double pageHeight = page.getEffectiveHeight();

        PageLayout currentPage = new PageLayout(page);
        scoreLayout.addPageLayout(currentPage);

        SystemLayout currentSystem = new SystemLayout();
        currentPage.addSystem(currentSystem);

        for (Measure measure : score.getMeasures()) {
            MeasureLayout ml = new MeasureLayout(measure);

            if (pageWidth - currentSystem.getOccupiedWidth() < ml.getWidth()) {
                if (pageHeight - currentPage.getOccupiedHeight() < currentSystem.getHeight()) {
                    currentPage = new PageLayout(page);
                    scoreLayout.addPageLayout(currentPage);
                }

                currentSystem = new SystemLayout();
                currentPage.addSystem(currentSystem);
            }

            for (Part part : score.getParts()) {
                PartLayout pl = currentSystem.getOrCreatePart(part);

                for (Staff staff : part.getStaves()) {
                    StaffLayout sl = pl.getOrCreateStaff(new StaffLayout(staff, pageWidth));
                    sl.add(new MeasureLayout(measure));
                }
            }
        }

        return scoreLayout;
    }
}
