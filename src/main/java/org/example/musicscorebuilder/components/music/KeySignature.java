package org.example.musicscorebuilder.components.music;

public class KeySignature extends Element {
    private KeySigType type;

    public KeySignature(KeySigType type) {
        this.type = type;
    }

    public  KeySigType getType() { return type; }
}