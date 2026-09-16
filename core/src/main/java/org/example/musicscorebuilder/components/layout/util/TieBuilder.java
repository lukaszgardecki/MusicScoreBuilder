package org.example.musicscorebuilder.components.layout.util;

import org.example.musicscorebuilder.components.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class TieBuilder {
    private final List<NoteLayout> tieStartNotes = new ArrayList<>();

    public void buildTies(List<PageLayout> pages) {
        buildSpanners(
                pages,
                system -> system.getTies().clear(),
                tieStartNotes,
                this::findNextNoteInVoice,
                (system, start, end) -> system.addTie(new TieLayout(system, start, end))
        );
    }

    public void addNoteLayout(NoteLayout noteLayout) {
        tieStartNotes.add(noteLayout);
    }

    public void clearCache() {
        tieStartNotes.clear();
    }

    private void buildSpanners(
            List<PageLayout> pages,
            Consumer<SystemLayout> clearAction,
            List<NoteLayout> startNotes,
            Function<NoteLayout, NoteLayout> endFinder,
            TriConsumer<SystemLayout, NoteLayout, NoteLayout> addSpannerToSystem
    ) {
        for (PageLayout page : pages) {
            for (SystemLayout system : page.getSystems()) {
                clearAction.accept(system);
            }
        }

        for (NoteLayout startNote : startNotes) {
            NoteLayout endNote = endFinder.apply(startNote);
            if (endNote == null) continue;

            SystemLayout startSystem = startNote.getSegment().getParent().getParent();
            SystemLayout endSystem = endNote.getSegment().getParent().getParent();

            if (startSystem == endSystem) {
                addSpannerToSystem.accept(startSystem, startNote, endNote);
            } else {
                addSpannerToSystem.accept(startSystem, startNote, null);
                addSpannerToSystem.accept(endSystem, null, endNote);
            }
        }
    }

    private NoteLayout findNextNoteInVoice(NoteLayout startNote) {
        SegmentLayout current = startNote.getSegment().getNext();
        int staffIndex = startNote.getStaff().getStaffIndex();
        int voice = startNote.getVoice();

        while (current != null) {
            for (ElementLayout el : current.getElements()) {
                if (el.getStaff() != null && el.getStaff().getStaffIndex() == staffIndex && el.getVoice() == voice) {
                    if (el instanceof NoteLayout note) return note;
                    if (el instanceof RestLayout) return null;
                }
            }
            current = current.getNext();
        }
        return null;
    }

    @FunctionalInterface
    private interface TriConsumer<System, StartNote, EndNote> {
        void accept(System system, StartNote startNote, EndNote endNote);
    }
}
