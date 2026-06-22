package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.music.Page;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.Staff;

import java.util.List;

public class LayoutEngine {

    public ScoreLayout computeLayout(Score score, Page page) {
        ScoreLayout layout = new ScoreLayout();
        List<Staff> staves = score.getStaves();
        double currentY = page.getMarginLeft();

        for (Staff staff : staves) {
            StaffLayout sl = new StaffLayout(staff, page.getMarginLeft(), currentY, page.getEffectiveWidth());
            layout.add(sl);

            // odległość między pięcioliniami
            currentY += 100;
        }
        return layout;
    }
}
