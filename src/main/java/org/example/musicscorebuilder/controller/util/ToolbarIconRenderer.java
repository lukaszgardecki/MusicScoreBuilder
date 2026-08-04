package org.example.musicscorebuilder.controller.util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.NoteView;
import org.example.musicscorebuilder.components.views.TieView;

import java.util.List;

public class ToolbarIconRenderer {
    private final NoteView noteView;
    private final TieView tieView;

    public ToolbarIconRenderer() {
        this.noteView = new NoteView();
        this.tieView = new TieView();
    }

    public void renderModeIcon(Button button) {
        if (button == null) return;

        double width = 25;
        double height = 25;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setFont(Font.font("Segoe UI Emoji", 14));
        gc.fillText("✏", 4, 17);
        button.setGraphic(canvas);
    }

    public void renderNoteTypeIcon(Button button, NoteType type, boolean dotted) {
        if (button == null) return;

        double width = 25;
        double height = 25;
        double noteScale = 4.75;
        int dots = dotted ? 1 : 0;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);
        Note dummyNote = new Note(1, PitchStep.F, 0, 4, type, BeamType.NONE, dots, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset(centeredX / noteScale);

        noteView.draw(gc, noteLayout, 0.0, 0.0, noteScale);
        button.setGraphic(canvas);
    }

    public void renderNoteDottedIcon(Button button) {
        renderNoteTypeIcon(button, NoteType.QUARTER, true);
    }

    public void renderVoiceIcon(Button button, int voiceNumber) {
        if (button == null) return;

        double width = 25;
        double height = 25;
        double noteScale = 4.75;
        var pitch = voiceNumber % 2 == 1 ? PitchStep.F : PitchStep.H;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().get(0);
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);
        Note dummyNote = new Note(voiceNumber, pitch, 0, 4, NoteType.QUARTER, BeamType.NONE, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);
        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        noteView.draw(gc, noteLayout, 0.0, 0.0, noteScale);

        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.BLACK);
        gc.fillText(String.valueOf(voiceNumber), 10, 10);

        button.setGraphic(canvas);
    }

    public void renderTieIcon(Button button) {
        if (button == null) return;

        double width = 40;
        double height = 25;
        double noteScale = 4.75;
        double offsetY = -6.0;

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        ScoreStyle style = new ScoreStyle();
        Page dummyPage = new Page(PageFormat.A4_V);
        PageLayout dummyPageLayout = new PageLayout(dummyPage, style, 0);
        SystemLayout dummySystem = new SystemLayout(dummyPageLayout, BraceType.BRACE);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, dummySystem, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);

        Note dummyNote1 = new Note(1, PitchStep.F, 0, 4, NoteType.QUARTER, BeamType.NONE, dummyMeasure);
        Segment dummySegment1 = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout1 = new SegmentLayout(dummySegment1, measureLayout);
        NoteLayout noteLayout1 = new NoteLayout(dummyNote1, staffLayout, segmentLayout1);

        Note dummyNote2 = new Note(1, PitchStep.F, 0, 4, NoteType.QUARTER, BeamType.NONE, dummyMeasure);
        Segment dummySegment2 = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout2 = new SegmentLayout(dummySegment2, measureLayout);
        NoteLayout noteLayout2 = new NoteLayout(dummyNote2, staffLayout, segmentLayout2);

        segmentLayout1.setX(0);
        segmentLayout2.setX(0);

        double startX = 4;
        double endX = 24;
        noteLayout1.setX(startX / noteScale);
        noteLayout1.setXOffset(startX / noteScale);

        noteLayout2.setX(endX / noteScale);
        noteLayout2.setXOffset(endX / noteScale);

        TieLayout tieLayout = new TieLayout(dummySystem, noteLayout1, noteLayout2);
        double tieOffsetX = -measureLayout.getX() * noteScale;
        double tieOffsetY = 1.5;

        gc.save();
        gc.translate(0, offsetY);

        noteView.draw(gc, noteLayout1, 0.0, 0.0, noteScale);
        noteView.draw(gc, noteLayout2, 0.0, 0.0, noteScale);
        tieView.draw(gc, tieLayout, tieOffsetX, tieOffsetY, noteScale);

        gc.restore();

        button.setGraphic(canvas);
    }
}