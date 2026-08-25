package org.example.musicscorebuilder.components.layout;

public abstract class NoteRestLayout extends ElementLayout {
    private NoteRestLayout nextInVoice;
    private NoteRestLayout prevInVoice;

    public NoteRestLayout(boolean hasDynamicWidth, SegmentLayout parent, StaffLayout staff) {
        super(hasDynamicWidth, parent, staff);
    }

    public NoteRestLayout getNextInVoice() {
        return nextInVoice;
    }

    public void setNextInVoice(NoteRestLayout nextInVoice) {
        this.nextInVoice = nextInVoice;
    }

    public NoteRestLayout getPrevInVoice() {
        return prevInVoice;
    }

    public void setPrevInVoice(NoteRestLayout prevInVoice) {
        this.prevInVoice = prevInVoice;
    }
}