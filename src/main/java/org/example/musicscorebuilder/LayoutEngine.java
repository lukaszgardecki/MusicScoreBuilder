package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

import java.util.List;

public class LayoutEngine {
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout compute(Score score) {
        ScoreLayout scoreLayout = new ScoreLayout();
        PageLayout currentPage = new PageLayout(page);
        scoreLayout.addPageLayout(currentPage);


        SystemLayout currentSystem = new SystemLayout();
        currentPage.addSystem(currentSystem);

        for (Measure measure : score.getMeasures()) {
            MeasureLayout ml = new MeasureLayout(measure);
            boolean noSpaceForNextMeasure = currentSystem.getOccupiedWidth() + ml.getMinWidth() > page.getEffectiveWidth();
            boolean noSpaceForNextSystem = currentPage.getOccupiedHeight() + currentSystem.getHeight() > page.getEffectiveHeight();

            if (noSpaceForNextMeasure) {
                justifySystem(currentSystem);

                if (noSpaceForNextSystem) {
                    currentPage = new PageLayout(page);
                    scoreLayout.addPageLayout(currentPage);
                }

                currentSystem = new SystemLayout();
                currentPage.addSystem(currentSystem);
                ml.setFirstInSystem(true);
            }

            for (Part part : score.getParts()) {
                PartLayout partLayout = currentSystem.getOrCreatePart(part);
                for (Staff staff : part.getStaves()) {
                    StaffLayout staffLayout = partLayout.getOrCreateStaff(staff);
                    staffLayout.add(ml);
                }
            }
        }

        justifySystem(currentSystem);
        return scoreLayout;
    }

    private void justifySystem(SystemLayout system) {
        List<MeasureLayout> measures = system.getMeasures();
        if (measures.isEmpty()) return;

        double MIN_FULLNESS_RATIO = 0.5;
        double measuresMinWidthSum = measures.stream().mapToDouble(MeasureLayout::getMinWidth).sum();
        double totalSystemMinWidth = measuresMinWidthSum + system.getBraceWidth();
        if (totalSystemMinWidth < page.getEffectiveWidth() * MIN_FULLNESS_RATIO) return;
        double extraSpace = page.getEffectiveWidth() - totalSystemMinWidth;

        if (extraSpace > 0 && measuresMinWidthSum > 0) {
            for (MeasureLayout m : measures) {
                double share = m.getMinWidth() / measuresMinWidthSum;
                double newWidth = m.getMinWidth() + (extraSpace * share);
                m.setWidth(newWidth);
            }
        }
    }
}
