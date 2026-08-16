package org.example.musicscorebuilder.components.music.util;

import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.ModeType;
import org.example.musicscorebuilder.components.music.Score;
import org.example.musicscorebuilder.components.music.ScoreMode;
import org.example.musicscorebuilder.data.StorageService;

public class ScoreFactory {
    private static final int DEFAULT_MEASURES_COUNT = 32;

    public static ScoreMode createSoloMode() {
        ScoreStyle style = new ScoreStyle();
        style.setStaffSpacingScale(1.3);
        return attachNewMode(getScore(), ModeType.SOLO, style);
    }

    public static ScoreMode createHarmonyMode() {
        ScoreStyle style = new ScoreStyle();
        style.setStaffSpacingScale(1.0);
        return attachNewMode(getScore(), ModeType.HARMONY, style);
    }

    public static Score createEmptySoloTemplate() {
        Score score = createDefaultScore();
        ScoreStyle style = new ScoreStyle();
        style.setStaffSpacingScale(1.3);
        attachNewMode(score, ModeType.SOLO, style);
        return score;
    }

    public static ScoreMode createMode(ModeType type) {
        return switch (type) {
            case SOLO, TEXT -> createSoloMode();
            case HARMONY -> createHarmonyMode();
        };
    }

    private static ScoreMode attachNewMode(Score score, ModeType type, ScoreStyle style) {
        ScoreMode mode = new ScoreMode(score, type, style);
        mode.appendMeasures(DEFAULT_MEASURES_COUNT);
        score.getModes().add(mode);
        return mode;
    }

    private static Score getScore() {
        Score score = StorageService.getInstance().getScore();
        return score != null ? score : createDefaultScore();
    }

    private static Score createDefaultScore() {
        return new Score("000", "000", "Tytuł", "Podtytuł", "Kompozytor");
    }
}