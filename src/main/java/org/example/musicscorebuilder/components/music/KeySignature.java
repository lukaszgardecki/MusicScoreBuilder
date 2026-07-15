package org.example.musicscorebuilder.components.music;

public class KeySignature extends Element {
    private KeySigType type;

    public KeySignature(int value) {
        this.type = KeySigType.of(value);
    }

    public  KeySigType getType() { return type; }
}