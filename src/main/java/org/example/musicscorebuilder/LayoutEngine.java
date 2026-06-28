package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Score;

public class LayoutEngine {
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout compute(Score score) {
        ScoreLayout layout = new ScoreLayout();
        PageLayout page1 = new PageLayout(page);
        PageLayout page2 = new PageLayout(page);
        PageLayout page3 = new PageLayout(page);
        PageLayout page4 = new PageLayout(page);
        SystemLayout systemLayout = new SystemLayout();

        for (Part part : score.getParts()) {
            PartLayout partLayout = new PartLayout(part);
            systemLayout.add(partLayout);
        }

        page1.addSystem(systemLayout);
        page2.addSystem(systemLayout);
        page3.addSystem(systemLayout);
        page4.addSystem(systemLayout);
        layout.addPageLayout(page1);
        layout.addPageLayout(page2);
        layout.addPageLayout(page3);
        layout.addPageLayout(page4);







//        double localY = 0;
//
//        for (Part part : score.getParts()) {
//            PartLayout partLayout = compute(part, localY);
//            layout.addPageLayout(partLayout);
//
//            // odległość między systemami
//            localY += partLayout.getHeight() + score.getPartSpacing();
//        }
        return layout;
    }

//    private PartLayout compute(Part part) {
//        PartLayout layout = new PartLayout(part, 0, yStart);
//        double localY = 0;
//
//        for (Staff staff : part.getStaves()) {
//            StaffLayout sl = new StaffLayout(staff, 0, localY, page.getEffectiveWidth());
//            layout.add(sl);
//
//            // odległość między pięcioliniami
//            localY += sl.getHeight() + part.getStaffSpacing();
//        }
//        return layout;
//    }
}
