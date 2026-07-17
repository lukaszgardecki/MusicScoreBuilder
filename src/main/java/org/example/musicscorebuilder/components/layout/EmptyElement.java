package org.example.musicscorebuilder.components.layout;

public class EmptyElement extends ElementLayout {

    public EmptyElement(ScoreStyle scoreStyle) {
        super(true, scoreStyle);
    }

    @Override public double getWidth() { return 10; }
    @Override public double getHeight() { return 4; }
    @Override public double getBoxY() { return 0; }
}