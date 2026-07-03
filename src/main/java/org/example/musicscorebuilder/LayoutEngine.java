package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.music.*;

import java.util.ArrayList;
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
        List<MeasureLayout> currentSystemMeasures = new ArrayList<>();

        for (Measure measure : score.getMeasures()) {
            MeasureLayout ml = new MeasureLayout(measure);
            boolean noSpaceForNextMeasure = currentSystem.getOccupiedWidth() + ml.getMinWidth() > page.getEffectiveWidth();
            boolean noSpaceForNextSystem = currentPage.getOccupiedHeight() + currentSystem.getHeight() > page.getEffectiveHeight();

            if (noSpaceForNextMeasure) {
                justifySystem(currentSystemMeasures);

                if (noSpaceForNextSystem) {
                    currentPage = new PageLayout(page);
                    scoreLayout.addPageLayout(currentPage);
                }

                currentSystem = new SystemLayout();
                currentPage.addSystem(currentSystem);
                currentSystemMeasures.clear();
            }

            currentSystemMeasures.add(ml);

            for (Part part : score.getParts()) {
                PartLayout partLayout = currentSystem.getOrCreatePart(part);

                for (Staff staff : part.getStaves()) {
                    StaffLayout staffLayout = partLayout.getOrCreateStaff(staff);
                    staffLayout.add(ml);
                }
            }
        }

        justifySystem(currentSystemMeasures);

        return scoreLayout;
    }

    private void justifySystem(List<MeasureLayout> measures) {
        double minWidthSum = measures.stream().mapToDouble(MeasureLayout::getMinWidth).sum();
        double extraSpace = page.getEffectiveWidth() - minWidthSum;

        if (extraSpace > 0) {
            for (MeasureLayout m : measures) {
                double newWidth = m.getMinWidth() + (extraSpace * (m.getMinWidth() / minWidthSum));
                m.setFinalWidth(newWidth);
            }
        }
    }
}
