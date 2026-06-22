package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.PartLayout;
import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Part;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;

public class LayoutEngine {
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout computeLayout(Score score) {
        ScoreLayout layout = new ScoreLayout();
        double localY = layout.getY();

        for (Part part : score.getParts()) {
            PartLayout sl = computeLayout(part, localY);
            layout.add(sl);

            // odległość między systemami
            localY += sl.getHeight() + score.getPartSpacing();
        }
        return layout;
    }

    private PartLayout computeLayout(Part part, double yStart) {
        PartLayout layout = new PartLayout(part, 0, yStart);
        double localY = 0;

        for (Staff staff : part.getStaves()) {
            StaffLayout sl = new StaffLayout(staff, 0, localY, page.getEffectiveWidth());
            layout.add(sl);

            // odległość między pięcioliniami
            localY += sl.getHeight() + part.getStaffSpacing();
        }
        return layout;
    }
}
