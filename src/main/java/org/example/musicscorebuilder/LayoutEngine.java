package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;

import java.util.List;

public class LayoutEngine {
    private final Page page;

    public LayoutEngine(Page page) {
        this.page = page;
    }

    public ScoreLayout computeLayout(Score score) {
        ScoreLayout layout = new ScoreLayout();
        List<Staff> staves = score.getStaves();
        double currentY = 0;

        for (Staff staff : staves) {
            StaffLayout sl = new StaffLayout(staff, 0, currentY, page.getEffectiveWidth());
            layout.add(sl);

            // odległość między pięcioliniami
            currentY += 100;
        }
        return layout;
    }
}
