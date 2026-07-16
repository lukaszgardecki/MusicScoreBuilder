package org.example.musicscorebuilder.components.layout;

public class EmptyElement extends ElementLayout {

    public EmptyElement() {
        super(true);
    }

    @Override public double getWidth() { return 2; }
    @Override public double getHeight() { return 7; }

    @Override public double getBoxY() { return 0; }
}