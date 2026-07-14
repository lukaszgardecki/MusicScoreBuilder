package org.example.musicscorebuilder.components.music;

public class Clef extends Element {
    private ClefType type;

    public Clef(ClefType type) {
        this.type = type;
    }

    public  ClefType getType() {
        return type;
    }
    public void setType(ClefType type) {
        this.type = type;
    }
}
