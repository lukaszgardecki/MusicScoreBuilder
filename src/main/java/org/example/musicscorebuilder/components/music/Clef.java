package org.example.musicscorebuilder.components.music;

public class Clef extends Element {
    private ClefType type;

    @SuppressWarnings("unused")
    private Clef() {
        super(null);
    }

    public Clef(ClefType type) {
        super(null);
        this.type = type;
    }

    public  ClefType getType() { return type; }
    public void setType(ClefType type) { this.type = type; }
}
