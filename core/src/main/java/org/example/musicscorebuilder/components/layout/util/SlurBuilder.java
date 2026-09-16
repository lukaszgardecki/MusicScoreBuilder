package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.NoteLayout;
import org.example.musicscorebuilder.components.layout.PageLayout;
import org.example.musicscorebuilder.components.layout.SlurLayout;
import org.example.musicscorebuilder.components.layout.SystemLayout;
import org.example.musicscorebuilder.components.music.Note;
import org.example.musicscorebuilder.components.music.Slur;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class SlurBuilder {
    private final Map<Note, NoteLayout> noteToLayoutMap = new IdentityHashMap<>();

    public void buildSlurs(List<Slur> slurs, List<PageLayout> pages) {
        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                system.clearSlurs();
            }
        }

        for (Slur slur : slurs) {
            NoteLayout startLayout = noteToLayoutMap.get(slur.getStartNote());
            NoteLayout endLayout = noteToLayoutMap.get(slur.getEndNote());

            if (startLayout == null || endLayout == null) continue;

            SystemLayout startSystem = startLayout.getSegment().getParent().getParent();
            SystemLayout endSystem = endLayout.getSegment().getParent().getParent();

            if (startSystem == endSystem) {
                startSystem.addSlur(new SlurLayout(startSystem, startLayout, endLayout));
            } else {
                startSystem.addSlur(new SlurLayout(startSystem, startLayout, null));
                endSystem.addSlur(new SlurLayout(endSystem, null, endLayout));
            }
        }
    }

    public void putNote(Note note, NoteLayout noteLayout) {
        noteToLayoutMap.put(note, noteLayout);
    }

    public void clearCache() {
        noteToLayoutMap.clear();
    }
}
