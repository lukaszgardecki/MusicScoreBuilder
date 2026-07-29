package org.example.musicscorebuilder.components.layout.edit;

import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.BeamType;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.PitchStep;
import org.example.musicscorebuilder.managers.ModeManager;

public class GhostNoteLayout extends NoteLayout {
    private final ScoreStyle style;
    private final String color;

    public GhostNoteLayout(SegmentLayout segment, StaffLayout staff, double initialY) {
        super(
                new Note(
                        ModeManager.getInstance().getCurrentVoice(),
                        PitchStep.C,
                        0,
                        4,
                        ModeManager.getInstance().getCurrentNoteType(),
                        BeamType.NONE,
                        null
                ),
                staff,
                segment
        );
        this.style = segment.getScoreStyle();
        this.color = style.getEditInsertColor(ModeManager.getInstance().getCurrentVoice());
        updatePitchFromY(initialY);
    }

    public ScoreStyle getStyle() { return style; }
    public String getColor() { return color; }
}