package org.example.musicscorebuilder.components.music;

public class TimeSignature extends Element {
    private int beat;
    private int beatType;
    private boolean common;
    private boolean cut;


    public TimeSignature(int beat, int beatType, boolean common, boolean cut) {
        this.beat = beat;
        this.beatType = beatType;
        this.common = common;
        this.cut = cut;
    }

    public int getBeat() { return beat; }
    public int getBeatType() { return beatType; }
    public boolean isCommon() { return common; }
    public boolean isCut() { return cut; }
}
