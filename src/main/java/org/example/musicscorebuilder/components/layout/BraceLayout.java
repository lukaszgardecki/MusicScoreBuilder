package org.example.musicscorebuilder.components.layout;

import org.example.musicscorebuilder.components.music.BraceType;
import org.example.musicscorebuilder.components.music.Leland;

public class BraceLayout {
    private final Leland fontData;
    private final SystemLayout parent;

    public BraceLayout(BraceType braceType, SystemLayout parent) {
        fontData = switch (braceType) {
            case NONE -> null;
            case BRACE -> Leland.BRACE;
            case BRACKET -> Leland.BRACKET;
        };
        this.parent = parent;
    }

    public double getHeight() { return parent.getHeight() - parent.getSpaceBelow(); }
    public double getWidth() { return 1.2; }
    public String getCode() { return fontData.getCode(); }
    public double getX() { return 0; }
    public double getY() { return 0; }
}