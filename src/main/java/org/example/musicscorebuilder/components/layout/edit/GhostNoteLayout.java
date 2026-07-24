package org.example.musicscorebuilder.components.layout.edit;

import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.managers.ModeManager;

public class GhostNoteLayout extends NoteLayout {
    private final SegmentLayout segment;
    private final StaffLayout staff;
    private final ScoreStyle style;
    private final String color;

    public GhostNoteLayout(SegmentLayout segment, StaffLayout staff, double initialModelY) {
        // tu trzeba dostarczyć też typ nuty
        super(new Note(1, PitchStep.C, 0, 4, NoteType.QUARTER, BeamType.NONE), staff, segment);
        this.segment = segment;
        this.staff = staff;
        this.style = segment.getScoreStyle();
        this.color = style.getEditInsertColor(ModeManager.getInstance().getLastCursor().getElement());
        updatePitchFromY(initialModelY);
    }

    @Override public double getX() { return 0; }

    public SegmentLayout getSegment() { return segment; }
    public StaffLayout getStaff() { return staff; }
    public ScoreStyle getStyle() { return style; }
    public String getColor() { return color; }
}