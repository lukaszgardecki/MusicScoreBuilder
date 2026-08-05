package org.example.musicscorebuilder.components.layout.edit;

import org.example.musicscorebuilder.components.layout.AccidentalLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;
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
                        ModeManager.getInstance().isDotted() ? 1 : 0,
                        segment.getParent().getMeasure()
                ),
                staff,
                segment
        );
        this.style = segment.getScoreStyle();
        this.color = style.getEditInsertColor(ModeManager.getInstance().getCurrentVoice());
        updatePitchFromY(initialY);
    }

    @Override public AccidentalLayout getAccidental() { return null; }
    @Override
    public void updatePitchFromY(double newY) {
        super.updatePitchFromY(newY);
        Pitch pitch = getNote().getPitch();
        Measure measure = parent.getSegment().getParent();
        int keyAlter = measure != null ? measure.getKeySignatureAlterForStep(pitch.getStep()) : 0;

        if (pitch.getAlter() != keyAlter) {
            pitch.setAlter(keyAlter);
            refresh();
        }
    }

    public ScoreStyle getStyle() { return style; }
    public String getColor() { return color; }
}