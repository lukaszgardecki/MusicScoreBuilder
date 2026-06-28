package org.example.musicscorebuilder;

import org.example.musicscorebuilder.components.layout.ScoreLayout;
import org.example.musicscorebuilder.components.views.ScoreView;

public class ScoreRenderer {

    public ScoreView render(ScoreLayout layout) {
        return new ScoreView(layout);
    }
}