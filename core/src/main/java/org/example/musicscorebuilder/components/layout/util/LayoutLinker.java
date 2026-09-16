package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LayoutLinker {

    public void linkAllSegments(List<PageLayout> pages) {
        SegmentLayout prev = null;
        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout current : measure.getSegments()) {
                        current.setPrev(prev);
                        if (prev != null) {
                            prev.setNext(current);
                        }
                        prev = current;
                    }
                }
            }
        }
        if (prev != null) {
            prev.setNext(null);
        }
    }

    public void linkVoiceElements(List<PageLayout> pages) {
        Map<Long, NoteRestLayout> lastElementMap = new HashMap<>();
        Map<Long, NoteLayout> lastNoteMap = new HashMap<>();

        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                for (MeasureLayout measure : system.getMeasures()) {
                    for (SegmentLayout segment : measure.getSegments()) {
                        for (ElementLayout el : segment.getElements()) {

                            if (el instanceof NoteRestLayout current) {
                                int staffIdx = (current.getStaff() != null) ? current.getStaff().getStaffIndex() : 0;
                                int voice = current.getVoice();
                                long key = (((long) staffIdx) << 32) | (voice & 0xFFFFFFFFL);

                                NoteRestLayout prevElement = lastElementMap.get(key);
                                if (prevElement != null) {
                                    prevElement.setNextInVoice(current);
                                    current.setPrevInVoice(prevElement);
                                }
                                lastElementMap.put(key, current);

                                if (current instanceof NoteLayout note) {
                                    NoteLayout prevNote = lastNoteMap.get(key);
                                    if (prevNote != null) {
                                        prevNote.setNextNoteInVoice(note);
                                        note.setPrevNoteInVoice(prevNote);
                                    }
                                    lastNoteMap.put(key, note);
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}
