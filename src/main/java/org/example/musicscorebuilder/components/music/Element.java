package org.example.musicscorebuilder.components.music;

public abstract class Element {
    protected Measure parent;

    public Element(Measure parent) {
        this.parent = parent;
    }

    public Measure getParent() { return parent; }
    public int getDuration() { return 0; }

    public void setParent(Measure parent) { this.parent = parent; }
    public void hasChanged() {
        if (parent != null) parent.setDirty(true);
    }
}
