package org.example.musicscorebuilder.palette;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.example.musicscorebuilder.components.layout.MeasureLayout;
import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.SegmentLayout;
import org.example.musicscorebuilder.components.layout.StaffLayout;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.*;
import org.example.musicscorebuilder.components.views.NoteView;

import java.util.List;

public class BeamSectionRenderer {
    private final NoteView noteView = new NoteView();

    public void renderAuto(GraphicsContext gc, double width, double height) {
        gc.clearRect(0, 0, width, height);
        gc.setFont(Font.font("System", FontWeight.BOLD, 12));
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText("AUTO", width / 2.0, height / 2.0);
    }

    public void render8thSingle(GraphicsContext gc, double width, double height) {
        double noteScale = 5.5;
        double offsetY = 12.0;

        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);

        Note dummyNote = new Note(1, PitchStep.F, 0, 4, NoteType.EIGHTH, BeamType.NONE, 0, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset((centeredX / noteScale) - segmentLayout.getMarginLeft());

        noteView.draw(gc, noteLayout, 0.0, offsetY, noteScale);
    }

    public void renderBreakInner16th(GraphicsContext gc, double width, double height) {
        double noteScale = 5.5;
        double offsetY = 12.0;

        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);

        Note dummyNote = new Note(1, PitchStep.F, 0, 4, NoteType.QUARTER, BeamType.NONE, 0, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset((centeredX / noteScale) - segmentLayout.getMarginLeft());

        noteView.draw(gc, noteLayout, 0.0, offsetY, noteScale);

        if (noteLayout.getStem() != null) {
            double stemX = noteLayout.getStem().getX() * noteScale;
            double stemY = offsetY + (noteLayout.getStem().getEndY() * noteScale);

            double beamWidth = 10.0;    // Długość "kikuta" belki
            double beamHeight = 2.8;    // Grubość belki
            double beamGap = 2.2;       // Odstęp między belkami

            gc.setFill(Color.BLACK);
            gc.fillRect(stemX, stemY, beamWidth, beamHeight);
            gc.fillRect(stemX, stemY + beamHeight + beamGap, beamWidth, beamHeight);
        }
    }

    public void renderBreakInner8th(GraphicsContext gc, double width, double height) {
        double noteScale = 5.5;
        double offsetY = 12.0;

        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);

        Note dummyNote = new Note(1, PitchStep.F, 0, 4, NoteType.QUARTER, BeamType.NONE, 0, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset((centeredX / noteScale) - segmentLayout.getMarginLeft());

        noteView.draw(gc, noteLayout, 0.0, offsetY, noteScale);

        if (noteLayout.getStem() != null) {
            double stemX = noteLayout.getStem().getX() * noteScale;
            double stemY = offsetY + (noteLayout.getStem().getEndY() * noteScale);

            double beamHeight = 2.8;    // Grubość belki
            double beamGap = 2.2;       // Odstęp w pionie
            double leftExt = 8.0;       // Długość belki w lewo od laski
            double rightExt = 8.0;      // Długość belki w prawo od laski

            gc.setFill(Color.BLACK);
            gc.fillRect(stemX - leftExt, stemY, leftExt + rightExt, beamHeight);
            gc.fillRect(stemX, stemY + beamHeight + beamGap, rightExt, beamHeight);
        }
    }

    public void renderBreakInner32nd(GraphicsContext gc, double width, double height) {
        double noteScale = 5.5;
        double offsetY = 13.0;

        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);

        Note dummyNote = new Note(1, PitchStep.F, 0, 4, NoteType.QUARTER, BeamType.NONE, 0, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset((centeredX / noteScale) - segmentLayout.getMarginLeft());

        noteView.draw(gc, noteLayout, 0.0, offsetY, noteScale);

        if (noteLayout.getStem() != null) {
            double stemX = noteLayout.getStem().getX() * noteScale;
            double stemY = offsetY + (noteLayout.getStem().getEndY() * noteScale);

            double beamHeight = 2.3;    // Grubość pojedynczej belki
            double beamGap = 1.7;       // Odstęp w pionie między belkami
            double leftExt = 8.0;       // Długość w lewo
            double rightExt = 8.0;      // Długość w prawo

            gc.setFill(Color.BLACK);
            gc.fillRect(stemX - leftExt, stemY, leftExt + rightExt, beamHeight);

            double y2 = stemY + beamHeight + beamGap;
            gc.fillRect(stemX - leftExt, y2, leftExt + rightExt, beamHeight);

            double y3 = y2 + beamHeight + beamGap;
            gc.fillRect(stemX, y3, rightExt, beamHeight);
        }
    }

    public void renderConnect16th(GraphicsContext gc, double width, double height) {
        double noteScale = 5.5;
        double offsetY = 12.0;

        gc.clearRect(0, 0, width, height);

        Measure dummyMeasure = new Measure(List.of(new Staff(0, new Clef(ClefType.G))));
        Staff dummyStaff = dummyMeasure.getStaves().getFirst();
        ScoreStyle style = new ScoreStyle();
        MeasureLayout measureLayout = new MeasureLayout(dummyMeasure, 0, style);
        StaffLayout staffLayout = new StaffLayout(dummyStaff, measureLayout, style);

        Note dummyNote = new Note(1, PitchStep.F, 0, 4, NoteType.QUARTER, BeamType.NONE, 0, dummyMeasure);
        Segment dummySegment = new Segment(SegmentType.NOTEREST, dummyMeasure);
        SegmentLayout segmentLayout = new SegmentLayout(dummySegment, measureLayout);

        NoteLayout noteLayout = new NoteLayout(dummyNote, staffLayout, segmentLayout);

        double noteWidth = noteLayout.getBoxWidth() * noteScale;
        double centeredX = (width - noteWidth) / 2.0;
        noteLayout.setXOffset((centeredX / noteScale) - segmentLayout.getMarginLeft());

        noteView.draw(gc, noteLayout, 0.0, offsetY, noteScale);

        if (noteLayout.getStem() != null) {
            double stemX = noteLayout.getStem().getX() * noteScale;
            double stemY = offsetY + (noteLayout.getStem().getEndY() * noteScale);

            double beamHeight = 2.8;    // Grubość belki
            double beamGap = 2.2;       // Odstęp w pionie między belkami
            double leftExt = 8.0;       // Długość w lewo
            double rightExt = 8.0;      // Długość w prawo

            gc.setFill(Color.BLACK);
            gc.fillRect(stemX - leftExt, stemY, leftExt + rightExt, beamHeight);
            gc.fillRect(stemX - leftExt, stemY + beamHeight + beamGap, leftExt + rightExt, beamHeight);
        }
    }
}