package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.frames.FrameLayout;
import org.example.musicscorebuilder.components.frames.HeaderFrameLayout;
import org.example.musicscorebuilder.components.frames.TextFrameLayout;
import org.example.musicscorebuilder.components.layout.*;
import org.example.musicscorebuilder.components.layout.engine.ScoreStyle;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.Verse;
import org.example.musicscorebuilder.components.music.frames.Frame;
import org.example.musicscorebuilder.components.music.frames.HeaderFrame;
import org.example.musicscorebuilder.components.music.frames.TextFrame;
import org.example.musicscorebuilder.components.music.frames.TextFrameVerse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FrameBuilder {
    private final Map<Note, Integer> noteToSystemMap = new HashMap<>();

    public FrameLayout createFrameLayout(PageLayout parent, ScoreStyle style, Frame frameData) {
        return switch (frameData) {
            case HeaderFrame headerFrame -> new HeaderFrameLayout(parent, style, headerFrame);
            case TextFrame textFrame -> new TextFrameLayout(parent, style, textFrame);
            default -> throw new IllegalArgumentException("Nieobsługiwany typ ramki: " + frameData.getClass().getName());
        };
    }

    public void updateTextFrames(Map<Integer, Verse> verses, List<Frame> frames, List<PageLayout> pages) {
        if (verses == null || verses.isEmpty()) return;

        List<TextFrameVerse> updatedVerses = new ArrayList<>();
        buildNoteToSystemMap(pages);

        for (Verse verse : verses.values()) {
            updatedVerses.add(verse.toTextFrameVerse(noteToSystemMap));
        }

        for (Frame frame : frames) {
            if (frame instanceof TextFrame textFrame) {
                textFrame.setVerses(updatedVerses);
            }
        }
    }

    public void clearCache() {
        noteToSystemMap.clear();
    }

    private void buildNoteToSystemMap(List<PageLayout> pages) {
        noteToSystemMap.clear();
        int systemIndex = 0;
        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout element : segment.getElements()) {
                            if (element instanceof NoteLayout noteLayout && noteLayout.getNote() != null) {
                                noteToSystemMap.put(noteLayout.getNote(), systemIndex);
                            }
                        }
                    }
                }
                systemIndex++;
            }
        }
    }
}
